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

#include "AbstractSensor.h"
#include "CommunicationServer.h"
#include <iostream>

#include <sstream>
#include <iostream>
#include <string>
#include <vector>
#include <thread>
#include <chrono>
#include <unordered_map>

const bool TEXT_FILE_OUTPUT_ACTIVATED = true;
const int MESSAGE_PAYLOAD_SIZE = 22;
const int SLEEP_TIME_MS = 250;

std::vector<int> AbstractSensor::all_ids_;
std::vector<std::string> AbstractSensor::all_names_;

AbstractSensor::AbstractSensor(int id, int max_attributes, CommunicationServer& comm_server)
    : comm_server_(comm_server),
      id_(id),
      is_working_(true),
      max_attributes_(max_attributes),
      header("time, sensor_id"), header_written(false) {
  // Check that IDs are unique
  if (std::find(all_ids_.begin(), all_ids_.end(), id) != all_ids_.end()) {
    std::stringstream ss;
    ss << "Id " << id << " for sensor with name is already in use";
    throw std::runtime_error(ss.str());
  }
  all_ids_.push_back(id);
}

void AbstractSensor::set_comm_server(CommunicationServer& comm_server){
//  comm_server_ = comm_server;
}

std::vector<std::string> AbstractSensor::get_all_names() {
  return all_names_;
}

void AbstractSensor::idle() {
  std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
}


void AbstractSensor::run() {
  while (is_running_) {
    if (mode_ == Mode::EXECUTE && is_working_) {
      execute();
    } else {
      idle();
    }

  }
}


void AbstractSensor::set_mode(Mode mode) {
  mode_ = mode;
}

void AbstractSensor::set_name(const std::string& name) {
  // Check that name is valid
  if (name == "") {
    throw std::runtime_error("Cannot set name to \"\"");
  }

  // Check that name isn't in use
  if (std::find(all_names_.begin(), all_names_.end(), name) != all_names_.end()) {
    std::stringstream ss;
    ss << "Name " << name << " for sensor with ID \"" << id_ << "\" is already in use";
    throw std::runtime_error(ss.str());
  }

  // If name_ has been set before, remove name_ from all_names_
  if (name_ != "") {
    auto itr = std::find(all_names_.begin(), all_names_.end(), name_);
    if (itr == all_names_.end()) {
      throw std::runtime_error("Couldn't locate name in list");
    }
    all_names_.erase(itr);
  }

  // Add new name
  all_names_.push_back(name);
  name_ = name;
}

void AbstractSensor::set_port_name(const std::string& port_name) {
  port_name_ = port_name;
}

bool AbstractSensor::is_working() {
  return is_working_;
}

int AbstractSensor::get_id() {
  return id_;
}

std::string AbstractSensor::get_name() {
  return name_;
}

std::string AbstractSensor::get_port_name() {
  return port_name_;
}

int AbstractSensor::get_max_attributes() {
  return max_attributes_;
}

void AbstractSensor::insert_attributes(AbstractSensor::attr_struct attribute_struct){
  // If for some reason the attributes min and max values already have been inserted, just update them.
  auto it = attr_properties_.find(attribute_struct.attr_index);
  if (it == attr_properties_.end()) {
    attr_properties_.insert(std::pair<int, AbstractSensor::attr_struct>(attribute_struct.attr_index, attribute_struct));
  } 
  else {
    attr_properties_.erase(attribute_struct.attr_index);
    attr_properties_.insert(std::pair<int, AbstractSensor::attr_struct>(attribute_struct.attr_index, attribute_struct));
  }
  header.append(", " + attribute_struct.attr_name);
}

bool AbstractSensor::update_attribute_status(int attr, protobuf::SensorStatusMsg::AttributeStatusType status) {
  // Find out if for some reason there is no element with specified attribute in map. This should never happen but just to be sure?
  auto it = attr_properties_.find(attr);
  if (it != attr_properties_.end()) {
    it->second.status = status;
    return true;
  }
  return false;
}


void AbstractSensor::add_to_fifos(protobuf::DataMsg* tcp_msg, protobuf::DataMsg* file_msg,std::ostringstream* oss) {
  if (TEXT_FILE_OUTPUT_ACTIVATED) {
    if (!header_written) {
      text_file_fifo_.push(header);
      header_written = true;
    }
    text_file_fifo_.push((*oss).str());
  }
  delete(oss);
  protobuf_tcp_fifo_.push(create_gen_data_msg(tcp_msg));
  protobuf_file_fifo_.push(create_gen_data_msg(file_msg));
}

std::ostringstream* AbstractSensor::create_text_data_message(double time, std::vector<float>* values){
  std::ostringstream* oss = new std::ostringstream;
  *oss << std::fixed << time << ",";
  *oss << std::fixed << id_ << ",";
  for (int i = 0; i < static_cast<int>(values->size()); i++)
    *oss << std::fixed << values->at(i) << ",";
  // zero padding, fill missing payload with zeros
  for (int i = 2 + values->size(); i < MESSAGE_PAYLOAD_SIZE; i++) {
    *oss << 0;
    if (i != (MESSAGE_PAYLOAD_SIZE - 1)) {
      *oss << ",";
    }
  }
  return oss;
}

protobuf::DataMsg* AbstractSensor::create_data_message(double time,std::vector<float>* values, bool isTcp){

  protobuf::DataMsg* sub_msg = new protobuf::DataMsg();
  sub_msg->set_time(time);
  sub_msg->set_type_id(id_);
  for (int i = 0; i < static_cast<int>(values->size()); i++) {
      auto it = attr_properties_.find(i);
      // Convert value with eventual conversion expression
      if (it != attr_properties_.end() && it->second.has_conversion) {
        sub_msg->add_data(convert_value(it->second, values->at(i)));
      }
      else {
        sub_msg->add_data(values->at(i));
      }
      // Check if we should send an out of bounds message to client
      if(it != attr_properties_.end() && isTcp) {
        if (outside_limits(it->second, values->at(i))){
          if (it->second.status == protobuf::SensorStatusMsg::INSIDE_LIMITS) {
            it->second.status = protobuf::SensorStatusMsg::OUTSIDE_LIMITS;
            protobuf_tcp_fifo_.push(create_status_attr_msg(protobuf::SensorStatusMsg::OUTSIDE_LIMITS, i));
          }
        } else {
          if (it->second.status == protobuf::SensorStatusMsg::OUTSIDE_LIMITS) {
            it->second.status = protobuf::SensorStatusMsg::INSIDE_LIMITS;
            protobuf_tcp_fifo_.push(create_status_attr_msg(protobuf::SensorStatusMsg::INSIDE_LIMITS, i));
          }
        }
      }
      }
    if (sub_msg->data().size() > max_attributes_) {
      std::stringstream ss;
      ss << "More attributes was sent (" << values->size() << ") than maximum for this sensor (" << max_attributes_ << "). Increase it?";
      throw std::range_error(ss.str());
    }
    return sub_msg;
}



bool AbstractSensor::outside_limits(const AbstractSensor::attr_struct& attribute_struct, float value) {
  if(attribute_struct.has_max_limit && attribute_struct.has_min_limit) {
    return (value < attribute_struct.min || value > attribute_struct.max);
  }
  else if(attribute_struct.has_max_limit && !attribute_struct.has_min_limit) {
    return (value < attribute_struct.max);
  }
  else if(!attribute_struct.has_max_limit && attribute_struct.has_min_limit) {
    return (value < attribute_struct.min);
  }
  else {
    return false;
  }
}

float AbstractSensor::convert_value(attr_struct& attribute_s, float value) {
  if (attribute_s.has_conversion) {
    // Update variable
    std::string varname = attribute_s.converter.GetVar().begin()->first;
    attribute_s.converter.DefineVar(varname, &value);
    // Calculate converted value
    value = attribute_s.converter.Eval();
  }
  return value;
}

protobuf::GeneralMsg* AbstractSensor::create_gen_data_msg(protobuf::DataMsg* data_msg) {
  protobuf::GeneralMsg* general_msg = new protobuf::GeneralMsg();
  general_msg->set_sub_type(protobuf::GeneralMsg::DATA_T);
  general_msg->set_allocated_data(data_msg);
  return general_msg;
}

protobuf::GeneralMsg* AbstractSensor::create_status_attr_msg(protobuf::SensorStatusMsg::AttributeStatusType status,
                                                      int attr_index) {
  protobuf::GeneralMsg* general_msg = new protobuf::GeneralMsg();
  general_msg->set_sub_type(protobuf::GeneralMsg::SENSOR_STATUS_T);

  protobuf::SensorStatusMsg* sensor_status_msg = new protobuf::SensorStatusMsg();
  general_msg->set_allocated_sensor_status(sensor_status_msg);

  protobuf::SensorStatusMsg::Sensor* sensor_msg = sensor_status_msg->add_sensors();
  sensor_msg->set_sensor_id(id_);
  if (is_working()) {
    sensor_msg->set_status(protobuf::SensorStatusMsg::WORKING);
  } else {
    sensor_msg->set_status(protobuf::SensorStatusMsg::NOT_WORKING);
  }

  protobuf::SensorStatusMsg::Attribute* attr = sensor_msg->add_attributes();
  attr->set_index(attr_index);
  attr->set_status(status);
  return general_msg;
}

std::pair<std::unordered_map<int, AbstractSensor::attr_struct>::iterator, std::unordered_map<int, AbstractSensor::attr_struct>::iterator> AbstractSensor::get_attribute_iterators() {
  return std::pair<std::unordered_map<int, AbstractSensor::attr_struct>::iterator, std::unordered_map<int, AbstractSensor::attr_struct>::iterator>(attr_properties_.begin(), attr_properties_.end());
}

void AbstractSensor::set_working(bool working) {
  if (working != is_working_) {
    protobuf::GeneralMsg* general_msg = new protobuf::GeneralMsg();
    general_msg->set_sub_type(protobuf::GeneralMsg::SENSOR_STATUS_T);
    protobuf::SensorStatusMsg* sensor_status_msg = new protobuf::SensorStatusMsg();
    general_msg->set_allocated_sensor_status(sensor_status_msg);
    protobuf::SensorStatusMsg::Sensor* sensor = sensor_status_msg->add_sensors();
    sensor->set_sensor_id(id_);
    if (working) {
      sensor->set_status(protobuf::SensorStatusMsg::WORKING);
    } else {
      sensor->set_status(protobuf::SensorStatusMsg::NOT_WORKING);
    }
    protobuf_tcp_fifo_.push(general_msg);
  }
  is_working_ = working;
}

size_t AbstractSensor::tcp_fifo_lenght(){
  return protobuf_tcp_fifo_.get_size();
}

