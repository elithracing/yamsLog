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

#include "DataLogger.h"

#include <string>
#include <mutex>
#include <iostream>

#include <boost/bind.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/algorithm/string.hpp>
#include <boost/algorithm/string/predicate.hpp>
#include <boost/make_shared.hpp>

#include "ClientCallbackImpl.h"
#include "SensorMinMax.h"
#include "ModificationCallbackImpl.h"
#include "ProjectHandlerConstants.h"

#include "services/CommunicationServerService.h"
#include "services/KeyboardInputService.h"
#include "services/ProtobufFileOutputService.h"
#include "services/ProtobufTcpOutputService.h"
#include "services/TextFileOutputService.h"

#ifndef __APPLE__
#include "sensors/CanSensor.h"
#include "sensors/CorrsysSensor.h"
#include "sensors/GpsSensor.h"
#include "sensors/ImuSensor.h"
#endif
#include "sensors/VirtualSensor.h"

/**************************************************************************************
 HOWTO add more sensors:
 1.  Make a class, inheriting from AbstractSensor
 1.b Implement the virtual AbstractSensor functions
 2.  Instantiate an object of the class and add it to the "sensors_" vector in
       the create_sensors function below
 3.  Add value constraints, if needed, in the configuration file
 ***************************************************************************************/

using std::vector;
using std::cout;
using std::cerr;
using std::endl;
using std::string;
using std::unique_ptr;

static const int DEFAULT_COMMUNICATION_PORT = 2001;
std::string DEFAULT_MIN_MAX_FILE_NAME = "min_max_sensor_values.txt";
const std::string DEFAULT_PORT_CONFIG_FILE_NAME = "port_config.txt";

DataLogger::DataLogger() : status_changed_sema_(0), project_handler_(DEFAULT_PROJECT_PATH) {
}

int DataLogger::run(int argc, const char** argv) {
  vector<string> args(argv + 1, argv + argc);

  /* Initializing */

  // Sorts arguments and removes duplicates.
  std::sort(args.begin(), args.end());
  args.resize(std::distance(args.begin(), std::unique(args.begin(), args.end())));



  //Read from port_config file
  std::unordered_map<int, sensor_config_struct> sensor_config_map;
  std::ifstream file(DEFAULT_PORT_CONFIG_FILE_NAME);
  vector<string> wanted_sensors = args;
  if (file.is_open()) {
    std::string line;
    sensor_config_struct sensor_config;
    while (!file.eof()) {
      getline(file, line);
      int i = 0;
      std::stringstream ssin(line);
      std::string lines[3];
      while (ssin.good() && i < 3) {
          getline(ssin, lines[i], ',');
          boost::algorithm::trim(lines[i]);
          ++i;
      }
      if ((lines[0][0] != 0) && (lines[0][0]!= '%')) { // Checks for sensors in port_config
        sensor_config.port_path = lines[1];
        sensor_config.sensor_name = lines[2];
        sensor_config_map.insert(std::pair<int, sensor_config_struct>(stoi(lines[0]), sensor_config));
      }
    }
  } else {
    cerr << "# Could not locate configuration file at \"" << DEFAULT_PORT_CONFIG_FILE_NAME << "\". Exiting..." << endl;
    return EXIT_FAILURE;
  }


  int port = DEFAULT_COMMUNICATION_PORT;

  // Parse arguments.
  for (string& str : args) {
    if (boost::starts_with(str, "port=")) {
      string stringbuf = str;
      stringbuf.erase(stringbuf.begin(),
                      stringbuf.begin() + stringbuf.find("=") + 1);
      try {
        port = boost::lexical_cast<float>(stringbuf);
      } catch (boost::bad_lexical_cast& exc) {
        cerr << "# Malformed port number \"" << stringbuf << "\". Exiting..."
             << endl;
        return EXIT_FAILURE;
      }
    } else {
      cerr << "# No sensor with name \"" << str << "\" exists. Exiting..." << endl;
      return EXIT_FAILURE;
    }
  }

  CommunicationServer comm_server(port, this);
  create_sensors(sensor_config_map, comm_server);

  //Set the names and port paths of the wanted sensors declared in port_config.txt
  for (auto& sensor : sensors_) {
      auto itr = sensor_config_map.find(sensor->get_id());
    if (itr != sensor_config_map.end()) {
      sensor->set_name(itr->second.sensor_name);
      sensor->set_port_name(itr->second.port_path);
    }
  }

  create_services(comm_server);
  for (auto& sensor : sensors_) {
      auto itr = sensor_config_map.find(sensor->get_id());
    if (itr != sensor_config_map.end()) {
      sensor->set_comm_server(comm_server);
    }
  }
  ModificationCallbackImpl modification_callback_impl(project_handler_, comm_server);
  project_handler_.add_callback_interface(&modification_callback_impl);

  // Holds all service/sensor threads
  boost::thread_group service_threads;
  boost::thread_group sensor_threads;

  // Setup status observer to notify server on change
  status_observer_.subscribe_status_change(
      boost::bind(&DataLogger::status_observer_callback, this,
                  _1, boost::cref(comm_server)));

  // Setup sensor limits as found in min_max_sensor_values

  SensorMinMax sensor_min_max;
  if (sensor_min_max.initialize_min_max_values(DEFAULT_MIN_MAX_FILE_NAME, sensors_) == false) {
    std::cerr << "Could not locate sensor limits configuration file at \"" << DEFAULT_MIN_MAX_FILE_NAME << "\". Exiting..." << std::endl;
    finalize(service_threads, sensor_threads);
    return EXIT_FAILURE;
  }

  // initialize all sensors and services
  if (!initialize(service_threads, sensor_threads)) {
    finalize(service_threads, sensor_threads);
    return EXIT_FAILURE;
  }

  /* Main loop (most stuff in separate threads)*/
  running_ = true;
  while (running_) {
    // Wait for status to change
    status_changed_sema_.wait();
    if (running_ == false) {
      break;
    }
    // Find out what changed and act accordingly
    switch (status_observer_.get_status()) {
      case protobuf::StatusMsg::DATA_COLLECTION: {
        for (auto& sensor : sensors_) {
          sensor->set_mode(AbstractSensor::Mode::EXECUTE);
        }
        break;
      }
      case protobuf::StatusMsg::EXPERIMENT_PLAYBACK: {
        if (project_handler_.reset_read_pos() != ProjectHandlerErrorCode::SUCCESS) {
          std::cerr << "DataLogger: Failed to reset read position" << std::endl;
        }
        while (status_observer_.get_status() == protobuf::StatusMsg::EXPERIMENT_PLAYBACK) {
          ProjectHandlerErrorCode err;
          boost::optional<protobuf::GeneralMsg> general_message = project_handler_.read_next_data(err);
          switch (err) {
            case ProjectHandlerErrorCode::SUCCESS: {
              if (general_message) {
                comm_server.broadcast(*general_message);
              } else {
                if (project_handler_.reset_read_pos() != ProjectHandlerErrorCode::SUCCESS) {
                  std::cerr << "DataLogger: Failed to reset read position (" << static_cast<int>(err) << ")" << std::endl;
                }
                status_observer_.set_status(protobuf::StatusMsg::IDLE);
              }
              break;
            }
            case ProjectHandlerErrorCode::END_OF_FILE: {
              status_observer_.set_status(protobuf::StatusMsg::IDLE);
              break;
            }
            default: {
              std::cerr << "DataLogger: Failed to read message (" << static_cast<int>(err) << ")" << std::endl;
              status_observer_.set_status(protobuf::StatusMsg::IDLE);
              break;
            }
          }
        }
        break;
      }
      default: {
        for (auto& sensor : sensors_) {
          AbstractService::generate_time_stamp();
          sensor->set_mode(AbstractSensor::Mode::IDLE);
        }
        break;
      }
    }
  }

  cout << "# Terminating threads" << endl;
  finalize(service_threads, sensor_threads);
  cout << "# Program ended execution" << endl;
  return EXIT_SUCCESS;
}

unique_ptr<CommunicationServer::ClientCallbackInterface> DataLogger::create_client() {
  return unique_ptr<CommunicationServer::ClientCallbackInterface>(new ClientCallbackImpl(status_observer_, sensors_, &project_handler_));
}

void DataLogger::no_clients_callback() {
  status_observer_.set_status(protobuf::StatusMsg::IDLE);
}

void DataLogger::keyboard_input_service_exit_callback() {
  running_ = false;
  status_changed_sema_.post();
}

void DataLogger::create_services(
  CommunicationServer& comm_server) {
  // All services used. They will initialize in the order inserted, and terminate in the reverse order. Services terminate after sensors
  services_.push_back(
      boost::make_shared<CommunicationServerService>(comm_server));
  services_.push_back(
        boost::make_shared<KeyboardInputService>(boost::bind(&DataLogger::keyboard_input_service_exit_callback, this)));
  services_.push_back(
      boost::make_shared<TextFileOutputService>(sensors_, project_handler_,status_observer_));
  services_.push_back(
      boost::make_shared<ProtobufTcpOutputService>(sensors_, comm_server,status_observer_));
  services_.push_back(
      boost::make_shared<ProtobufFileOutputService>(sensors_, project_handler_,status_observer_));
}

void DataLogger::create_sensors(std::unordered_map<int, sensor_config_struct> sensor_config_map, CommunicationServer& comm_server) {
  // All sensors used. They will initialize in the order inserted, and terminate in the reverse order
  //TODO: make more general with ID

#ifndef __APPLE__
  if(sensor_config_map.find(0) != sensor_config_map.end()){
    sensors_.push_back(
                       boost::make_shared<ImuSensor>(0,
                                                     ImuSensor::Mode::ABSTIME, comm_server));
  }
  if(sensor_config_map.find(1) != sensor_config_map.end()){
    sensors_.push_back(
                       boost::make_shared<ImuSensor>(1, ImuSensor::Mode::NO_ABSTIME, comm_server));
  }
  if(sensor_config_map.find(2) != sensor_config_map.end()){
    sensors_.push_back(
                       boost::make_shared<CanSensor>(2, comm_server));
  }
  if(sensor_config_map.find(3) != sensor_config_map.end()){
    sensors_.push_back(
                       boost::make_shared<CanSensor>(3, comm_server));
  }
  if(sensor_config_map.find(4) != sensor_config_map.end()){
    sensors_.push_back(
                       boost::make_shared<GpsSensor>(4, comm_server));
  }
  if(sensor_config_map.find(6) != sensor_config_map.end()){
    sensors_.push_back(
                       boost::make_shared<CanSensor>(6, comm_server));
  }
  if(sensor_config_map.find(9) != sensor_config_map.end()){
    sensors_.push_back(
                       boost::make_shared<VirtualSensor>(9,  VirtualSensor::Mode::ABSTIME, comm_server));
  }
  if(sensor_config_map.find(30) != sensor_config_map.end()){
    sensors_.push_back(
                       boost::make_shared<CorrsysSensor>(30, comm_server));
  }
#else
  if(sensor_config_map.find(9) != sensor_config_map.end()){
    sensors_.push_back(
                       boost::make_shared<VirtualSensor>(9,  VirtualSensor::Mode::ABSTIME, comm_server));
  }
#endif
}

bool DataLogger::initialize(boost::thread_group& service_threads, boost::thread_group& sensor_threads) {
  // Initialize all services and sensors

  cout << "# Running initialization routines" << endl;
  for(auto& sensor : sensors_){
    sensor->init();
    if (sensor->initialized_correctly()) {
      sensor->set_running(true);
      cout << "# Initialization successful: " << sensor->get_name() << " (id " << sensor->get_id() << ")" << endl;
    } else {
      sensor->set_running(true);
      sensor->set_working(false);
      cerr << "# Initialization failure: " << sensor->get_name() << " (id " << sensor->get_id() << ")" << endl;
    }
  }
  for (auto& service : services_) {
    service->init();
    if (service->initialized_correctly()) {
      service->set_running(true);
    } else {
      service->set_running(false);
      cerr << "# Could not initialize all service threads. Exiting..."
           << endl;
      return false;
    }
  }

  cout << "# Running execution routines. Terminate program with the \"quit\" command." << endl;
  //Start threads
  for (auto& service : services_) {
    service_threads.create_thread(
        boost::bind(&AbstractService::run, service));
  }
  for (auto& sensor : sensors_) {
    sensor_threads.create_thread(
        boost::bind(&AbstractService::run, sensor));
  }

  // Get reference timestamp
  AbstractService::generate_time_stamp();

  return true;
}

void DataLogger::finalize(boost::thread_group& service_threads, boost::thread_group& sensor_threads) {
  // Stop threads backwards
  for (auto itr = sensors_.rbegin(); itr != sensors_.rend(); ++itr) {
    auto& sensor = *itr;
    sensor->set_running(false);
  }
  sensor_threads.join_all();
  for (auto itr = services_.rbegin(); itr != services_.rend(); ++itr) {
    auto& service = *itr;
    service->set_running(false);
  }
  service_threads.join_all();

  // Run finalization routines backwards
  cout << "# Running termination routines" << endl;
  for (auto itr = sensors_.rbegin(); itr != sensors_.rend(); ++itr) {
    auto& sensor = *itr;
    if (sensor->initialized_correctly()) {
      sensor->finalize();
    }
  }
  for (auto itr = services_.rbegin(); itr != services_.rend(); ++itr) {
    auto& service = *itr;
    if (service->initialized_correctly()) {
      service->finalize();
    }
  }
}

void DataLogger::status_observer_callback(protobuf::StatusMsg::StatusType new_status,
                              const CommunicationServer& comm_server) {
  // First, send new status to all
  protobuf::GeneralMsg msg;
  msg.set_sub_type(protobuf::GeneralMsg::STATUS_T);
  protobuf::StatusMsg* srt = new protobuf::StatusMsg();
  msg.set_allocated_status(srt);
  srt->set_status_type(new_status);
  comm_server.broadcast(msg);

  if (new_status == protobuf::StatusMsg::DATA_COLLECTION) {
    // Start time from 0 in data collection
    AbstractService::generate_time_stamp();
  }

  status_changed_sema_.post();
}


