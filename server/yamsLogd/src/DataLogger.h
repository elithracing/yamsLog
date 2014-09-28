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

#ifndef DATALOGGER_H_
#define DATALOGGER_H_

#include <vector>
#include <memory>

#include <boost/thread.hpp>
#include <boost/interprocess/sync/interprocess_semaphore.hpp>

#include "sensors/AbstractSensor.h"
#include "CommunicationServer.h"
#include "StatusObserver.h"
#include "ProjectHandler.h"

#include "services/AbstractService.h"



class DataLogger : public CommunicationServer::ServerCallbackInterface {
 public:
  DataLogger();

  struct sensor_config_struct {
    std::string port_path;
    std::string sensor_name;
  };

  int run(int argc, const char** argv);

  virtual std::unique_ptr<CommunicationServer::ClientCallbackInterface> create_client()
      override;
  virtual void no_clients_callback() override;

 private:
  void keyboard_input_service_exit_callback();

  void create_services(
    CommunicationServer& comm_server);

  void create_sensors(std::unordered_map<int,
                      sensor_config_struct> sensor_configs
                      ,CommunicationServer& comm_server);

  bool initialize(boost::thread_group& service_threads, boost::thread_group& sensor_threads);
  void finalize(boost::thread_group& service_threads, boost::thread_group& sensor_threads);

  void status_observer_callback(protobuf::StatusMsg::StatusType new_status,
                                const CommunicationServer& comm_server);



  StatusObserver status_observer_;
  std::vector<boost::shared_ptr<AbstractSensor>> sensors_;
  std::vector<boost::shared_ptr<AbstractService>> services_;
  boost::interprocess::interprocess_semaphore status_changed_sema_;
  volatile bool running_ = false;
  ProjectHandler project_handler_;
};

#endif  // DATALOGGER_H_
