/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2012 Per Öberg, Philipp Koschorrek, 2014  Emil Berg
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

#include "CanSensor.h"
#include <iostream>

#include <thread>
#include <chrono>
#include <errno.h>
#include <sys/ioctl.h>

const int MAX_ATTRIBUTES = 10;

//static const int ID_CS_CAN = 30;
static const int SLEEP_TIME_MS = 500;
static const int INIT_SLEEP_TIME_MS = 1000;

CanSensor::CanSensor(int id, CommunicationServer& comm_server)
    : AbstractSensor(id, MAX_ATTRIBUTES, comm_server),
      can_sock_(0) {
}

CanSensor::CanSensor(int id, int max_attributes, CommunicationServer& comm_server)
    : AbstractSensor(id, max_attributes,comm_server),
      can_sock_(0) {
}

bool CanSensor::initialize() {
  std::string returnstring;
  init_can(const_cast<char*>(get_port_name().c_str()), can_sock_, returnstring);
  if(!init_can(const_cast<char*>(get_port_name().c_str()), can_sock_, returnstring)){
    return false;
  }else {
    int bytes_read = recv(can_sock_, &can_frame_, sizeof(can_frame_), 0);
    return bytes_read != -1;
  }
}

void CanSensor::idle() {
  std::vector<float> values;
  if (read_one_data(&values)) {
    false_counter_ = 0;
    ioctl(can_sock_, SIOCGSTAMP, &ts_);
    double time = get_time_diff_us(&ts_);
    comm_server_.broadcast(*(create_gen_data_msg(create_data_message(time,&values ,true))));
//    Uncomment this to set a different data-pace when in idle-mode
//    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
  }else{
    std::this_thread::sleep_for(std::chrono::milliseconds(INIT_SLEEP_TIME_MS));
  }
}


void CanSensor::execute() {
//  double time = get_time_diff();
  /* Get timestamp of recieved message */
  ioctl(can_sock_, SIOCGSTAMP, &ts_);
  double time = get_time_diff_us(&ts_);
  std::vector<float> values;
  if(read_one_data(&values))
    if(values.size() != static_cast<size_t>(MAX_ATTRIBUTES)){
      for(int i = values.size(); i < MAX_ATTRIBUTES; i++){
        values.push_back(0);
      }
    }
    add_to_fifos(create_data_message(time,&values , true),create_data_message(time,&values ,false), create_text_data_message(time,&values));
}


bool CanSensor::read_one_data(std::vector<float>* values){
  if (read_can_struct()) {
      // Dump as raw CAN
      values->push_back(can_frame_.can_id & CAN_EFF_MASK);
      values->push_back(can_frame_.can_dlc);
      for (int i = 0; i < can_frame_.can_dlc; i++) {
        if(i > MAX_ATTRIBUTES)break;
        values->push_back(can_frame_.data[i]);
      }
      //Add padding so every message is the same length
      for(int i = can_frame_.can_dlc; i < MAX_ATTRIBUTES -2; i++){
        //MAX_ATTRIBUTES - 2 to account for attributes CAN_ID and CAN_MESSAGE_LENGTH
        //wich will always be in a message regarding of length
        values->push_back(0);
      }
      return true;
    }
  return false;
}

bool CanSensor::read_can_struct(){
  //int bytes_read = recv(can_sock_, &can_frame_, sizeof(can_frame_), 0);
  int bytes_read = read(can_sock_, &can_frame_, sizeof(can_frame_));
  if (bytes_read == -1) {
    false_counter_++;
    set_working(false);
    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
    finalize();
    set_working(initialize());
    return false;
  } else {
    set_working(true);
    return true;
  }
}

void CanSensor::finalize() {
  //Shutdown socket to not read or write anymore
  //shutdown(can_sock_, 2);
}
