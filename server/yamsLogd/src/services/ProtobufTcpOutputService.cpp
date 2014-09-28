/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2014  Emil Berg
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

/*
 * This is the class that calls the communication server to send the messages in all sensors
 * protobuf fifos. The class is executed in a separate thread and the thread executes the
 * execute function which sends first message in the fifo.
 */

#include "ProtobufTcpOutputService.h"

#include <thread>
#include <chrono>
#include <iostream>

#include "OutputServiceConstants.h"

ProtobufTcpOutputService::ProtobufTcpOutputService(
    const std::vector<boost::shared_ptr<AbstractSensor>>& sensors,
    const CommunicationServer& comm_server,
    StatusObserver& status_observer)
    : sensors_(sensors),
      comm_server_(comm_server),
      status_observer_(status_observer){
}

bool ProtobufTcpOutputService::initialize() {
  return true;
}

void ProtobufTcpOutputService::send_msg(
  const boost::shared_ptr<AbstractSensor>& sensor) {
  std::unique_ptr<protobuf::GeneralMsg> general_msg(sensor->protobuf_tcp_fifo_pop());
  if (general_msg->sub_type() == protobuf::GeneralMsg::DATA_T){
    if(status_observer_.get_status() == protobuf::StatusMsg::DATA_COLLECTION){
      comm_server_.broadcast(*general_msg);
    }
  }else{
    comm_server_.broadcast(*general_msg);
  }
}

void ProtobufTcpOutputService::execute() {
  bool wrote = false;
  for (auto& sensor : sensors_) {
    //TODO CHeck if working
    if(!sensor->protobuf_tcp_fifo_is_empty()){
      for (int i = 0;
          sensor->protobuf_tcp_fifo_is_empty() == false
              && i < MAX_READINGS_PER_STEP; i++) {
        wrote = true;
        send_msg(sensor);
      }
    }

  }
  if (!wrote) {
    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
  }
}

void ProtobufTcpOutputService::finalize() {
  // Save all
  for (auto& sensor : sensors_) {
    while (sensor->protobuf_tcp_fifo_is_empty() == false) {
      send_msg(sensor);
    }
  }
}
