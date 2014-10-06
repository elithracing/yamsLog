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

#ifndef SENSORS_ABSTRACTSENSOR_H_
#define SENSORS_ABSTRACTSENSOR_H_

#include <memory>
#include <string>
#include <vector>
#include <unordered_map>

#include "services/AbstractService.h"
#include "CommunicationServer.h"

#include "ThreadSafeFifo.h"
#include "protocol.pb.h"

/**
 * Parent class of all sensors.
**/

class CommunicationServer;

class AbstractSensor : public AbstractService {
 public:
  AbstractSensor(int id, int max_attributes, CommunicationServer& comm_server);

  enum class Mode {
    IDLE,
    EXECUTE
  };

  //Attr struct contains information of all the attributes in a sensor. If there is a min or max limit that shall notify the client if values pass min or max,
  //what the min and max are and also what attribute index and name the attribute has.
  struct attr_struct {
    bool has_max_limit;
    bool has_min_limit;
    protobuf::SensorStatusMsg::AttributeStatusType status;
    std::string attr_name;
    int attr_index;
    float min;
    float max;
  };

  static std::vector<std::string> get_all_names();

  virtual void idle();

  virtual bool read_one_data(std::vector<float>* values) = 0;

  virtual void run() override;

  void set_comm_server(CommunicationServer& comm_server);
  void set_mode(Mode mode);
  void set_name(const std::string& name);
  void set_port_name(const std::string& port_name);
  bool is_working();
  void set_working(bool working);

  // Returns the id of this sensor
  int get_id();

  // Returns the name of this sensor
  std::string get_name();

  // Returns the port name of this sensor
  std::string get_port_name();

  void add_value(float val);

  // Adds values to the send queue. These values will be sent in the same order with TCP, and saved to file.
  void add_to_fifos(protobuf::DataMsg* tcp_msg, protobuf::DataMsg* file_msg,std::ostringstream* oss);

  int get_max_attributes();

  // Queue functions
  void text_file_fifo_push(const std::string& str) {
    text_file_fifo_.push(str);
  }
  std::string text_file_fifo_pop() {
    return text_file_fifo_.pop();
  }
  bool text_file_fifo_is_empty() {
    return text_file_fifo_.is_empty();
  }
  void protobuf_tcp_fifo_push(protobuf::GeneralMsg* msg) {
    protobuf_tcp_fifo_.push(msg);
  }
  protobuf::GeneralMsg* protobuf_tcp_fifo_pop() {
    return protobuf_tcp_fifo_.pop();
  }
  bool protobuf_tcp_fifo_is_empty() {
    return protobuf_tcp_fifo_.is_empty();
  }
  void protobuf_file_fifo_push(protobuf::GeneralMsg* msg) {
    protobuf_file_fifo_.push(msg);
  }
  protobuf::GeneralMsg* protobuf_file_fifo_pop() {
    return protobuf_file_fifo_.pop();
  }
  bool protobuf_file_fifo_is_empty() {
    return protobuf_file_fifo_.is_empty();
  }
  /*
   * Insert min and max values for the desiered attribute, this will for now also initiate a status that states
   * that the values for the desired attribute is in range.
   */
 // void insert_attribute_min_max(int attr, float min, float max, const std::string& attr_name);
  void insert_attributes(AbstractSensor::attr_struct attr_struct);
  // Updates the status of desiered attributes status, returns false if the attribute cannot be found in the map
  bool update_attribute_status(int attr, protobuf::SensorStatusMsg::AttributeStatusType status);

  protobuf::GeneralMsg* create_status_attr_msg(protobuf::SensorStatusMsg::AttributeStatusType status, int attr_index);
  protobuf::GeneralMsg* create_gen_data_msg(protobuf::DataMsg* data_msg);
  protobuf::DataMsg* create_data_message(double time,std::vector<float>* values, bool isTcp);
  std::ostringstream* create_text_data_message(double time, std::vector<float>* values);

  bool outside_limits(const AbstractSensor::attr_struct& attribute_struct, float value);
  std::pair<std::unordered_map<int, attr_struct>::iterator, std::unordered_map<int, attr_struct>::iterator> get_attribute_iterators();
  size_t tcp_fifo_lenght();
  CommunicationServer& comm_server_;
 protected:

 private:
  static std::vector<int> all_ids_;
  static std::vector<std::string> all_names_;
  std::unordered_map<int, attr_struct> attr_properties_;
  ThreadSafeFifo<std::string> text_file_fifo_;
  ThreadSafeFifo<protobuf::GeneralMsg*> protobuf_tcp_fifo_;
  ThreadSafeFifo<protobuf::GeneralMsg*> protobuf_file_fifo_;

  //Each sensor has a numerical ID. This ID is determined in sensor_config.txt (and also right now in the creation of the sensors in DataLogger class)
  int id_;
  //If the sensor isn't giving data, it's concidered as not working.
  volatile bool is_working_;
  //A sensor has two modes, idle and execute. When in idle, the program does not save any data to disk but will send data to client. When in execute, data will be saved to disk(full speed)
  //and also sent to client in the speed wanted by the client.
  Mode mode_ = Mode::IDLE;
  std::string port_name_;
  std::string name_;
  int max_attributes_;
};

#endif  // SENSORS_ABSTRACTSENSOR_H_
