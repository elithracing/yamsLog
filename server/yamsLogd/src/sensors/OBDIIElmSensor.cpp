/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2014  Per Öberg, Erik Frisk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

#include "OBDIIElmSensor.h"
#include "OBDII-ELM327/OBDReader.h"
#include <iostream>

#include <thread>
#include <chrono>

const int SLEEP_TIME_MS = 500;
const int MAX_ATTRIBUTES = 12;

OBDIIElmSensor::OBDIIElmSensor(int id, CommunicationServer& comm_server)
  : AbstractSensor(id, MAX_ATTRIBUTES, comm_server), sampleTime(1.0/50.0) {obdreader = new OBDReader(); }

OBDIIElmSensor::~OBDIIElmSensor()
{
  delete obdreader;
}

bool OBDIIElmSensor::initialize()
{
#ifndef NDEBUG
  std::cout << "OBDIIElmSensor: DEBUG Initializing" << std::endl;
#endif
  OBDReader::argStruct argStruct;
  argStruct.s = get_port_name().c_str();
  argStruct.sampleRate = 0;
  obdreader->argsFromStruct(&argStruct);
  obdreader->initConnection();
  return true;
}

void OBDIIElmSensor::idle() 
{
 #ifndef NDEBUG
  //   std::cout << "OBDIIElmSensor: IDLING " << std::endl;
 #endif
  std::vector<float> values;
  if(read_one_data(&values)){
    set_working(true);
    double time = get_time_diff();
    currentTime = time;
    comm_server_.broadcast(*(create_gen_data_msg(create_data_message(time,&values ,true))));
    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
  } else{
    finalize();
    set_working(initialize());
    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
  }
}

void OBDIIElmSensor::execute() {
  double time = get_time_diff();
  
  currentTime = time;
  std::vector<float> values;
  if(read_one_data(&values)) {
    if(values.size() != static_cast<size_t>(MAX_ATTRIBUTES)){
      for(int i = values.size(); i < MAX_ATTRIBUTES; i++){
        values.push_back(0);
      }
    }
    add_to_fifos(create_data_message(time, &values,true),create_data_message(time, &values,false), create_text_data_message(time,&values));
  }
}

bool 
OBDIIElmSensor::read_one_data(std::vector<float>* values)
{
  //usleep( sampleTime*1e6 );
  std::cout << "_";
  fflush(stdout); // Will now print everything in the stdout buffer
  set_working(true);
  
  obdreader->readLoop(&measDataQueue);     
  OBDReader::measData* measData;


  // Go through list of attributes in order of attr_index
  auto iterators = get_attribute_iterators();
  auto selected = iterators.first;
  for(int ii = 1; ii < MAX_ATTRIBUTES ; ii = ii+2)
  {
    for (auto itr = iterators.first; itr != iterators.second; ++itr) {
      if(itr->first == ii)
      {
	selected = itr;
	break;
      }
    }
    if(selected->first == ii)
    {
      // Now we have the name of the attribute that we should write to  (selected->second.attr_name)
      //printf("Property name: %s \n",selected->second.attr_name.c_str());
      for(auto itr = measDataQueue.begin(); itr != measDataQueue.end(); itr++ )
      {
	measData = *itr;
	if(measData->name == selected->second.attr_name)
	{
	  values->push_back(get_time_diff_us(&(measData->time)));
	  values->push_back(measData->val);
#ifndef NDEBUG
	  printf("t=%f,%s=%f\n", measData->time.tv_sec+(double)measData->time.tv_usec/1000000.0f,measData->name.c_str(), measData->val);
#endif
	}
	
      }
    }
  }
  measDataQueue.clear();
  
  return true;
}

void OBDIIElmSensor::finalize() 
{

}
