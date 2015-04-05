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

#include "ERDataLogger.h"

#include <thread>
#include <chrono>
#include <iostream>

#include <boost/lexical_cast.hpp>

#ifndef ER_DATALOGGER_DEBUG
#define ER_DATALOGGER_DEBUG 0
#endif

static const int SERIAL_TIMEOUT       = 6;
static const int MAX_ATTRIBUTES       = 29; 
static const int SLEEP_TIME_MS        = 200;
static const int MAX_MESSAGE_SIZE     = 128;
static const int LOOP_TIMEOUT         = 1000;

// Special bytes for communication with logging unit
static const uint8_t ER_START         = 0xAA;
static const uint8_t ER_ESC           = 0xBB;
static const uint8_t ER_STOP          = 0xCC;

ERDataLogger::ERDataLogger(int id, CommunicationServer& comm_server)
  : AbstractSensor(id, MAX_ATTRIBUTES, comm_server),
  er_receiver_(0){
  }

bool ERDataLogger::initialize() {
  try {
    time = 0;
    er_receiver_ = new TimeoutSerial(get_port_name(), 115200);
    er_receiver_->setTimeout(boost::posix_time::seconds(SERIAL_TIMEOUT));
    return er_receiver_->isOpen();
  } catch (...) {
    return false;
  }
}

void ERDataLogger::idle() {
  try {
    if(er_receiver_ != nullptr){
      std::vector<float> values;
      if(read_one_data(&values)){
        set_working(true);
        comm_server_.broadcast(*(create_gen_data_msg(create_data_message(get_time_diff(),&values ,true))));
      }
    }else{
      throw "Nullpointer!";
    }
  } catch (...) {
    if(er_receiver_ != nullptr){
      finalize();
    }
    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
    set_working(initialize());
  }
}

void ERDataLogger::execute() {
  std::vector<float> values;
  if(read_one_data(&values)){
    add_to_fifos(create_data_message(time, &values,true), 
        create_data_message(time, &values,false), 
        create_text_data_message(time,&values));
  }
}

bool ERDataLogger::read_one_data(std::vector<float>* values){
  uint8_t byte, last_byte;
  uint16_t value;
  float floatval;
  bool in_packet = false;
  bool escape = false;
  bool second_byte = false;
  bool got_time_low = false;
  bool got_time_high = false;

  try {
    if(er_receiver_ != nullptr) {
      for(int i=0; i<LOOP_TIMEOUT ; ++i) {
        er_receiver_->read((char*)&byte, 1);
        set_working(true);

        if(!in_packet) {
          in_packet = (byte == ER_START);
#if (ER_DATALOGGER_DEBUG)
          if(in_packet) {
            printf("\nERDataLogger: byte #%d: START BYTE FOUND \n", i);
          }
#endif
        } 
        else if (byte == ER_ESC) {
          escape = true;
        }
        else if (byte == ER_STOP) {
#if (ER_DATALOGGER_DEBUG)
          printf("\nERDataLogger: byte #%d: STOP BYTE FOUND\n", i);
#endif
          return true;
        }
        else {
          if(escape) {
            byte = byte ^ ER_ESC;
            escape = false;
          }
          if(second_byte) {
            if(!got_time_low) {
              value = (last_byte << 8) + byte;
              got_time_low = true;
            }
            else if(!got_time_high) {
              // Received both time fields
              time = (double) ((uint32_t)(last_byte << 8) + byte) + ((uint32_t) value);
              got_time_high = true;
#if (ER_DATALOGGER_DEBUG)
              printf("ERDataLogger: byte #%d: Got time: %f\n", i, time);
#endif
            }
            else {
              // Received one data field
              floatval = (float) (last_byte << 8) + byte;
              values->push_back(floatval);
#if (ER_DATALOGGER_DEBUG)
              printf("ERDateLogger: byte #%d: Got value: %f \n", i, floatval);
#endif
            }
            second_byte = false;
          }
          else {
            last_byte = byte;
            second_byte = true;
          }
        }
      }
#if (ER_DATALOGGER_DEBUG)
      std::cout << "ERDataLogger: Too large packet or no stop byte" << std::endl;
#endif
    }
  } catch (...) {
#if (ER_DATALOGGER_DEBUG)
    std::cout << "ERDataLogger: I/O error" << std::endl;
#endif
    if(er_receiver_ != nullptr) {
      finalize();
    }
    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
    set_working(initialize());
  }
  return false;
}


void ERDataLogger::finalize() {
  er_receiver_->close();
}
