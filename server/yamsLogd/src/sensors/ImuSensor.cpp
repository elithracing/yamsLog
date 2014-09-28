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

#include "ImuSensor.h"
#include <iostream>

#include <thread>
#include <chrono>

const int SLEEP_TIME_MS = 500;
const int MAX_ATTRIBUTES = 12;

ImuSensor::ImuSensor(int id, Mode mode, CommunicationServer& comm_server)
    : AbstractSensor(id, MAX_ATTRIBUTES, comm_server),
      output_mode_(OUTPUTMODE_CALIB | OUTPUTMODE_ORIENT),
      output_settings_(OUTPUTSETTINGS_ORIENTMODE_EULER | OUTPUTSETTINGS_TIMESTAMP_SAMPLECNT),
      mode_(mode),
      num_devices_(0),
      read_fail_(0){
}

bool ImuSensor::initialize() {
  std::string returnstring;
  int rc = init_imu(const_cast<char*>(get_port_name().c_str()), mtcomm_imu_,
                    num_devices_, output_mode_, output_settings_, returnstring);
  if (returnstring.find("Cannot open") == (size_t) -1) {
    return true;
  } else {
    return rc == 1;
  }
}

void ImuSensor::idle() {
  std::vector<float> values;
  if(read_one_data(&values)){
    set_working(true);
    comm_server_.broadcast(*(create_gen_data_msg(create_data_message(get_time_diff(),&values ,true))));
//    Uncomment this to set a different data-pace when in idle-mode
//    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
  }else{
    finalize();
    set_working(initialize());
    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
  }
}

void ImuSensor::execute() {
  double time = get_time_diff();
    std::vector<float> values;
    if(read_one_data(&values)){
      if(values.size() != static_cast<size_t>(MAX_ATTRIBUTES)){
        for(int i = values.size(); i < MAX_ATTRIBUTES; i++){
          values.push_back(0);
        }
      }
      add_to_fifos(create_data_message(time, &values,true),create_data_message(time, &values,false), create_text_data_message(time,&values));
    }

}

bool ImuSensor::read_one_data(std::vector<float>* values){
  short datalen;
  bool added_data = false;
    if (mtcomm_imu_.readDataMessage(data_, datalen) == MTRV_OK) {
      set_working(true);

      unsigned short samplecounter;
      mtcomm_imu_.getValue(VALUE_SAMPLECNT, samplecounter, data_,
      BID_MASTER);

      for (int i = 0; i < num_devices_; i++) {
        if ((output_mode_ & OUTPUTMODE_CALIB) != 0) {
          // Output Calibrated data
          mtcomm_imu_.getValue(VALUE_CALIB_ACC, fdata_, data_, BID_MT + i);
          values->push_back(fdata_[0]);
          values->push_back(fdata_[1]);
          values->push_back(fdata_[2]);
          mtcomm_imu_.getValue(VALUE_CALIB_GYR, fdata_, data_, BID_MT + i);
          values->push_back(fdata_[0]);
          values->push_back(fdata_[1]);
          values->push_back(fdata_[2]);
          mtcomm_imu_.getValue(VALUE_CALIB_MAG, fdata_, data_, BID_MT + i);
          values->push_back(fdata_[0]);
          values->push_back(fdata_[1]);
          values->push_back(fdata_[2]);
        }

        if ((output_mode_ & OUTPUTMODE_ORIENT) != 0) {
          switch (output_settings_ & OUTPUTSETTINGS_ORIENTMODE_MASK) {
            case OUTPUTSETTINGS_ORIENTMODE_QUATERNION: {
              // Output: quaternion
              mtcomm_imu_.getValue(VALUE_ORIENT_QUAT, fdata_, data_, BID_MT + i);
              values->push_back(fdata_[0]);
              values->push_back(fdata_[1]);
              values->push_back(fdata_[2]);
              values->push_back(fdata_[3]);
              added_data = true;
              break;
            }
            case OUTPUTSETTINGS_ORIENTMODE_EULER: {
              // Output: Euler
              mtcomm_imu_.getValue(VALUE_ORIENT_EULER, fdata_, data_, BID_MT + i);
              values->push_back(fdata_[0]);
              values->push_back(fdata_[1]);
              values->push_back(fdata_[2]);
              added_data = true;
              break;
            }
            case OUTPUTSETTINGS_ORIENTMODE_MATRIX: {
              // Output: Cosine Matrix
              mtcomm_imu_.getValue(VALUE_ORIENT_MATRIX, fdata_, data_, BID_MT + i);
              values->push_back(fdata_[0]);
              values->push_back(fdata_[1]);
              values->push_back(fdata_[2]);
              values->push_back(fdata_[3]);
              values->push_back(fdata_[4]);
              values->push_back(fdata_[5]);
              values->push_back(fdata_[6]);
              values->push_back(fdata_[7]);
              values->push_back(fdata_[8]);
              added_data = true;
              break;
            }
            default: {
              break;
            }
          }
        }
      }


      if (mode_ == Mode::ABSTIME) {
        double abstime = get_time_diff() + get_time_stamp().tv_sec
            + (get_time_stamp().tv_nsec) / 1000000000.0;
        values->push_back(abstime);
      }
      return added_data;
    } else {
      read_fail_++;
      if(read_fail_ > 10){
        finalize();
        std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
        set_working(initialize());
        return false;
      }
    }
    return added_data;
}


void ImuSensor::finalize() {
  deinit_imu(const_cast<char*>(get_port_name().c_str()), mtcomm_imu_);
}
