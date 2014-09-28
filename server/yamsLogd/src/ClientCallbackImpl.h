/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2014  Tony Fredriksson
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

#ifndef CLIENTCALLBACKIMPL_H_
#define CLIENTCALLBACKIMPL_H_

#include <vector>

#include "StatusObserver.h"
#include "ProjectHandler.h"
#include "CommunicationServer.h"
#include "sensors/AbstractSensor.h"

/**
 * Implementation of ClientCallbackInterface. Callback that determines the actions of a connected client.
**/
class ClientCallbackImpl : public CommunicationServer::ClientCallbackInterface {
 public:
  ClientCallbackImpl(StatusObserver& status_observer, const std::vector<boost::shared_ptr<AbstractSensor>>& sensors, ProjectHandler* project_handler);

  virtual void join_callback(
      boost::function<void(const protobuf::GeneralMsg&)> send_func) override;


  virtual bool filter_outgoing_callback(const protobuf::GeneralMsg& msg)
      override;

  // Parses an incoming message
  virtual void parse_incoming_callback(
      boost::function<void(const protobuf::GeneralMsg&)> send_func,
      const protobuf::GeneralMsg& msg) override;

 private:
  // The sensor ids that should be sent to the client in anything but playback mode. Should always be sorted. Starts empty.
  std::vector<int> valid_sensor_type_ids_;
  // The sensor ids that should be sent to the client in playback mode. Should always be sorted. Starts empty.
  std::vector<int> valid_sensor_type_ids_playback_;
  // Maps sensor id to the time the sensor data was sent earlier
  std::unordered_map<int, double> time_filter_map_;
  // Protects the private vars above
  std::mutex mutex_;

  StatusObserver& status_observer_;
  const std::vector<boost::shared_ptr<AbstractSensor>>& sensors_;
  volatile uint min_time_;
  ProjectHandler* project_handler_;
  mutable std::mutex send_mutex_;
  bool initiated_;


  void handle_error(const std::string& txt);
  bool is_valid_sensor_id(int id);

  void project_list(protobuf::GeneralMsg* response_msg);
  void active_project(protobuf::GeneralMsg* response_message);

  void set_dynamic_event(const protobuf::DynamicEventStruct& dynamic_event_struct, protobuf::GeneralMsg* response_message);
  void set_settings(const protobuf::SettingsRequestMsg& settings_request_msg, protobuf::GeneralMsg* response_message);

  void create_new_project(const std::string& name, protobuf::GeneralMsg* response_message);
  void rename_project(const std::string& old_name, const std::string& new_name, protobuf::GeneralMsg* response_message);
  void remove_project(const std::string& name, protobuf::GeneralMsg* response_message);
  void set_active_project(const std::string& name, protobuf::GeneralMsg* response_message);
  void set_project_metadata(const protobuf::ProjectMetadataStruct& project_metadata_struct, protobuf::GeneralMsg* response_message);

  void experiment_data_collection_start(protobuf::GeneralMsg* response_message, const std::string& name);
  void experiment_data_collection_stop(protobuf::GeneralMsg* response_message);
  void rename_experiment(const std::string& old_name, const std::string& new_name, protobuf::GeneralMsg* response_message);
  void remove_experiment(const std::string& old_name, protobuf::GeneralMsg* response_message);
  bool experiment_playback_start(const protobuf::ExperimentPlaybackStartRequestMsg& experiment_playback_start_request_msg, protobuf::GeneralMsg* response_message);
  void experiment_playback_stop(protobuf::GeneralMsg* response_message);
  void set_experiment_metadata(const protobuf::SetExperimentMetadataRequestMsg& set_experiment_metadata_request_msg, protobuf::GeneralMsg* response_message);
};

#endif  // CLIENTCALLBACKIMPL_H_
