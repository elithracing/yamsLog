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

#include "VirtualSensor.h"
#include <iostream>

#include <thread>
#include <chrono>

const int SLEEP_TIME_MS = 500;
const int MAX_ATTRIBUTES = 12;

VirtualSensor::VirtualSensor(int id, Mode mode, CommunicationServer& comm_server)
  : AbstractSensor(id, MAX_ATTRIBUTES, comm_server), sampleTime(1.0/50.0), mode_(mode) { }

bool VirtualSensor::initialize()
{
  std::cout << "VirtualSensor: Initializing" << std::endl;
#ifndef NDEBUG
  std::cout << "VirtualSensor: DEBUG Initializing" << std::endl;
#endif
  return true;
}

void VirtualSensor::idle() 
{
#ifndef NDEBUG
  std::cout << "VirtualSensor: IDLING " << std::endl;
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

void VirtualSensor::execute() {
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
VirtualSensor::read_one_data(std::vector<float>* values)
{
  usleep( sampleTime*1e6 );
  printf(".");
  set_working(true);
  float sensorValue = sin(2*M_PI*1/5.0*currentTime);
  values->push_back( sensorValue );
  
  if (mode_ == Mode::ABSTIME) {
    double abstime = get_time_diff() + get_time_stamp().tv_sec
        + (get_time_stamp().tv_nsec) / 1000000000.0;
    values->push_back(abstime);
  }
  return true;
}

void VirtualSensor::finalize() 
{

}
