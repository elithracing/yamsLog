/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2012 Philipp Koschorrek, 2014  Emil Berg
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

#include "GpsSensor.h"

#include <thread>
#include <chrono>
#include <iostream>

#include <boost/lexical_cast.hpp>

static const int SERIAL_TIMEOUT = 6;
const int MAX_ATTRIBUTES = 5;
const int SLEEP_TIME_MS = 200;



GpsSensor::GpsSensor(int id, CommunicationServer& comm_server)
    : AbstractSensor(id, MAX_ATTRIBUTES, comm_server),
      gps_receiver_(0){
}

bool GpsSensor::initialize() {
  try {
    gps_receiver_ = new TimeoutSerial(get_port_name(), 115200);
    gps_receiver_->setTimeout(boost::posix_time::seconds(SERIAL_TIMEOUT));
    std::string inputgps = gps_receiver_->readStringUntil("\r\n") + "\r\n";
    if(inputgps.size() < 1)return false;
    return gps_receiver_->isOpen();
  } catch (...) {
    return false;
  }
}

void GpsSensor::idle() {
  try {
    if(gps_receiver_ != nullptr){
      std::vector<float> values;
      if(read_one_data(&values)){
        set_working(true);
        comm_server_.broadcast(*(create_gen_data_msg(create_data_message(get_time_diff(),&values ,true))));
      }
    }else{
      throw "Nullpointer!";
    }
  } catch (...) {
    if(gps_receiver_ != nullptr){
      finalize();
    }
    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
    set_working(initialize());
  }
}

void GpsSensor::execute() {
  double time = get_time_diff();
  std::vector<float> values;
  if(read_one_data(&values)){
    //Add padding if needed
//    if(values.size() != static_cast<size_t>(MAX_ATTRIBUTES)){
////      for(int i = values.size(); i < MAX_ATTRIBUTES; i++){
////        values.push_back(0);
////      }
//    }
    add_to_fifos(create_data_message(time, &values,true),create_data_message(time, &values,false), create_text_data_message(time,&values));
  }
}

bool GpsSensor::read_one_data(std::vector<float>* values){
  std::string buf;
      try {
        if(gps_receiver_ != nullptr){
          std::string inputgps = gps_receiver_->readStringUntil("\r\n") + "\r\n";
          set_working(true);
          if (inputgps.find("GPRMC") != (size_t) -1
              || inputgps.find("PUBX") != (size_t) -1) {
            // if GPRMC-Message
            if (inputgps.find("GPRMC") != (size_t) -1) {
              std::stringstream lineStream(inputgps);
              int i = 0;
              std::string cell;
              while (getline(lineStream, cell, ',')) {
                switch (i) {
                  case 1: {  // UTC
                    float utc = boost::lexical_cast<float>(cell);
                    //add_value(utc);
                    values->push_back(utc);

                    break;
                  }
                  case 3: {  // Lat
                    buf = cell;
                    break;
                  }
                  case 4: {
                    float lat = boost::lexical_cast<float>(buf);
                    if (cell == "S") {
                      lat = -lat;
                    }
                    //add_value(lat);
                    values->push_back(lat);
                    break;
                  }
                  case 5: {  // Lon
                    buf = cell;
                    break;
                  }
                  case 6: {
                    float lon = boost::lexical_cast<float>(buf);
                    if (cell == "W") {
                      lon = -lon;
                    }
                    //add_value(lon);
                    values->push_back(lon);
                    break;
                  }
                  case 7: {  // SOG
                    float speed;
                    try {
                      speed = boost::lexical_cast<float>(cell) * 0.514444;
                    } catch (boost::bad_lexical_cast& exc) {
                      // if there is a failure while converting, speed = 0
                      speed = 0;
                    }
                    //add_value(speed);
                    values->push_back(speed);
                    break;
                  }
                  case 8: {  // COG
                    float course;
                    if (cell == "") {
                      course = 0.0f;
                    } else {
                      course = boost::lexical_cast<float>(cell);
                    }
                    values->push_back(course);
                   //add_value(course);
                    break;
                  }
                  default:
                    break;
                }
                i++;
              }
              return true;
            }

            // if PUBX-Message
            if (inputgps.find("PUBX") != (size_t) -1) {
              std::stringstream lineStream(inputgps);
              int i = 0;
              std::string cell;
              while (getline(lineStream, cell, ',')) {
                switch (i) {
                  case 2: {  // UTC
                    float utc = boost::lexical_cast<float>(cell);
                    values->push_back(utc);
                    //add_value(utc);
                    break;
                  }
                  case 3: {  // Lat
                    buf = cell;
                    break;
                  }
                  case 4: {
                    float lat = boost::lexical_cast<float>(buf);
                    if (cell == "S") {
                      lat = -lat;
                    }
                    values->push_back(lat);
                    //add_value(lat);
                    break;
                  }
                  case 5: {  // Lon
                    buf = cell;
                    break;
                  }
                  case 6: {
                    float lon = boost::lexical_cast<float>(buf);
                    if (cell == "W") {
                      lon = -lon;
                    }
                    values->push_back(lon);

                    //add_value(lon);
                    break;
                  }
                  case 11: {  // SOG
                    float speed;
                    try {
                      speed = boost::lexical_cast<float>(cell) / 3.6;
                    } catch (boost::bad_lexical_cast& exc) {
                      // if there is a failure while converting, speed = 0
                      speed = 0;
                    }
                    values->push_back(speed);
                    //add_value(speed);
                    break;
                  }
                  case 12: {  // COG
                    float course;
                    if (cell == "") {
                      course = 0.0f;
                    } else {
                      course = boost::lexical_cast<float>(cell);
                    }
                    values->push_back(course);
                    //add_value(course);
                    break;
                  }
                  default:
                    break;
                }
                i++;
              }
              return true;
            }
          }
          return false;
        }else{
          std::cout << "gps_reciever = Nullptr" << std::endl;
          throw "Nullpointer!";
        }
      } catch (...) {
        if(gps_receiver_ != nullptr){
          finalize();
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
        set_working(initialize());
      }
      return false;
}


void GpsSensor::finalize() {
    gps_receiver_->close();
}
