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

#include "ClientCallbackImpl.h"
#include "ProjectHandlerErrorCodes.h"

#include <thread>
#include <chrono>

#include <boost/system/error_code.hpp>
#include <iomanip>      // std::setprecision

/*
 * This class acts as a callback to the connected clients. By using the send_func, 
 * passed as a parameter to the functions, the message passed to send_func will only be sent to the 
 * client that this client callback is connected to.
 *
 */

ClientCallbackImpl::ClientCallbackImpl(
    StatusObserver& status_observer,
    const std::vector<boost::shared_ptr<AbstractSensor>>& sensors,
    ProjectHandler* project_handler)
    : status_observer_(status_observer),
      sensors_(sensors),
      project_handler_(project_handler),
      initiated_(false){
}

/*
 * The join_callback function is called when a client has connected to the server
 * and is ready to send and receive messaged. According to the network protocol 
 * the server has to send some setup information to the client about for exampel
 * what sensors are present, what projects there are to choose and the status of the server 
 * The messages have to be sent in the order used in the join_callback function. 
 */

void ClientCallbackImpl::join_callback(
    boost::function<void(const protobuf::GeneralMsg&)> send_func) {
  // Send sensor and attribute configuration data
    {
      protobuf::GeneralMsg general_msg;
      general_msg.set_sub_type(protobuf::GeneralMsg::CONFIGURATION_T);
      protobuf::ConfigurationMsg* configuration_msg =
          new protobuf::ConfigurationMsg();
      general_msg.set_allocated_configuration(configuration_msg);
      for (auto& sensor : sensors_) {
        protobuf::SensorConfiguration* sensor_configuration =
            configuration_msg->add_sensor_configurations();
        sensor_configuration->set_sensor_id(sensor->get_id());
        sensor_configuration->set_name(sensor->get_name());
        sensor_configuration->set_max_attributes(sensor->get_max_attributes());
        auto iterators = sensor->get_attribute_iterators();
        for (auto itr = iterators.first; itr != iterators.second; ++itr) {
          protobuf::AttributeConfiguration* attribute_configuration =
              sensor_configuration->add_attribute_configurations();
          attribute_configuration->set_index(itr->first);
          attribute_configuration->set_name(itr->second.attr_name);
        }
      }
      send_func(general_msg);
    }

    // Send status
    {
      protobuf::GeneralMsg general_msg;
      general_msg.set_sub_type(protobuf::GeneralMsg::STATUS_T);
      protobuf::StatusMsg* status_msg = new protobuf::StatusMsg();
      general_msg.set_allocated_status(status_msg);
      status_msg->set_status_type(status_observer_.get_status());
      send_func(general_msg);
    }

    // Send sensor working status and attribute statuses
    {
      protobuf::GeneralMsg general_msg;
      general_msg.set_sub_type(protobuf::GeneralMsg::SENSOR_STATUS_T);
      protobuf::SensorStatusMsg* sensor_status_msg =
          new protobuf::SensorStatusMsg();
      general_msg.set_allocated_sensor_status(sensor_status_msg);
      for (auto& sensor : sensors_) {
        protobuf::SensorStatusMsg::Sensor* sensor_msg = sensor_status_msg
            ->add_sensors();
        sensor_msg->set_sensor_id(sensor->get_id());
        if (sensor->is_working()) {
          sensor_msg->set_status(protobuf::SensorStatusMsg::WORKING);
        } else {
          sensor_msg->set_status(protobuf::SensorStatusMsg::NOT_WORKING);
        }
        auto iterators = sensor->get_attribute_iterators();
        for (auto itr = iterators.first; itr != iterators.second; ++itr) {
          protobuf::SensorStatusMsg::Attribute* attribute = sensor_msg
              ->add_attributes();
          attribute->set_index(itr->first);
          attribute->set_status(itr->second.status);
        }
      }
      send_func(general_msg);
    }

    // Send project list
    {
      protobuf::GeneralMsg general_msg;
      project_list(&general_msg);
      send_func(general_msg);
    }

    // Send active project
    {
       protobuf::GeneralMsg general_msg;
       active_project(&general_msg);
       send_func(general_msg);
    }
    {
      if(status_observer_.get_status() == protobuf::StatusMsg_StatusType::StatusMsg_StatusType_DATA_COLLECTION){
        protobuf::GeneralMsg active_exp;
        active_exp.set_sub_type(protobuf::GeneralMsg::ACTIVE_EXPERIMENT_T);
        protobuf::ActiveExperimentMsg* active_experiment = new protobuf::ActiveExperimentMsg();
        active_experiment->set_name(project_handler_->get_active_experiment());
        active_exp.set_allocated_active_experiment(active_experiment);
        send_func(active_exp);
      }
    }


    // Send experiment_list in active project
    {
      protobuf::GeneralMsg general_msg;
      general_msg.set_sub_type(protobuf::GeneralMsg::EXPERIMENT_LIST_T);
      protobuf::ExperimentListMsg* experiment_list_msg = new protobuf::ExperimentListMsg();
      general_msg.set_allocated_experiment_list(experiment_list_msg);
      ProjectHandlerErrorCode err;
      std::vector<std::string> experiment_list = project_handler_->get_project_experiment_names(err);
      switch (err) {
        case ProjectHandlerErrorCode::SUCCESS: {
          for (auto& experiment_name : experiment_list) {
            experiment_list_msg->add_names(experiment_name);
          }
          send_func(general_msg);
          break;
        }
        case ProjectHandlerErrorCode::NO_ACTIVE_PROJECT: {
          // Do nothing.
          break;
        }
        default: {
          std::cerr << "ClientCallbackImpl: Failed to get experiment names in active project (" << static_cast<int>(err) << ")" << std::endl;
        }
      }
    }

    // Send metadata for all experiments in active project
    {
      ProjectHandlerErrorCode err;
      std::vector<protobuf::GeneralMsg> general_msg_list = project_handler_->read_active_project_experiments_metadata(err);
      if (err == ProjectHandlerErrorCode::SUCCESS) {
        for (protobuf::GeneralMsg& general_msg : general_msg_list) {
          send_func(general_msg);
        }
      }
    }

  initiated_ = true;
}

/*
 * The filter_outgoing_callback is called every time the server wants to
 * send something to it's connected clients. Because there is a 
 * clientCallbackImpl for each connected client, the clientCallbackImpl
 * stores information about what information the client wants and how often
 * it wants to receive data.
 */

bool ClientCallbackImpl::filter_outgoing_callback(
                                      const protobuf::GeneralMsg& msg) {
  switch (msg.sub_type()) {
    case protobuf::GeneralMsg::DATA_T: {
        if(initiated_){
          protobuf::DataMsg data_msg = msg.data();
          double time = data_msg.time();
          int type_id = data_msg.type_id();

          // Protect lists and map
          std::lock_guard<std::mutex> lock(mutex_);
          auto itr = time_filter_map_.find(type_id);

          switch (status_observer_.get_status()) {
//            case protobuf::StatusMsg::IDLE: {
//              return true;
//            }
            case protobuf::StatusMsg::EXPERIMENT_PLAYBACK: {

              if (itr == time_filter_map_.end() && itr->second + (min_time_ * 0.001) < time) {
                time_filter_map_[type_id] = time;
              // Send if in valid sensors list
              return std::binary_search(valid_sensor_type_ids_playback_.begin(),
                                        valid_sensor_type_ids_playback_.end(),
                                        msg.data().type_id());
              }
              return false;
              break;
            }
            default: {
              //For dataCOllection mode
              // Send if in valid sensors list

              if (itr == time_filter_map_.end() || (itr->second + (min_time_ * 0.001)) < time) {
                time_filter_map_[type_id] = time;
                return std::binary_search(valid_sensor_type_ids_.begin(),
                                          valid_sensor_type_ids_.end(),
                                          msg.data().type_id());
              }

              return false;
              break;
            }
          }
        }else{
          return false;
          break;
        }
        break;

      }
      default: {
        break;
      }
  }
  return true;
}

/*
 * Everytime a message is recieved from a connected client it gets parsed
 * and regarding of what subtype it is, different sub-functions are beeing called. 
 */

void ClientCallbackImpl::parse_incoming_callback(
    boost::function<void(const protobuf::GeneralMsg&)> send_func,
    const protobuf::GeneralMsg& msg) {
  protobuf::GeneralMsg response_message;
  switch (msg.sub_type()) {
    case protobuf::GeneralMsg::SET_DYNAMIC_EVENT_REQUEST_T: {
      set_dynamic_event(msg.set_dynamic_event_request().dynamic_event(), &response_message);
      send_func(response_message);
      break;
    }
    case protobuf::GeneralMsg::SETTINGS_REQUEST_T: {
      set_settings(msg.settings_request(), &response_message);
      send_func(response_message);
      break;
    }
    case protobuf::GeneralMsg::CREATE_NEW_PROJECT_REQUEST_T: {
      create_new_project(msg.create_new_project_request().name(),
                         &response_message);
      send_func(response_message);
      break;
    }
    case protobuf::GeneralMsg::RENAME_PROJECT_REQUEST_T: {
      rename_project(msg.rename_project_request().old_name(),
                     msg.rename_project_request().new_name(),
                     &response_message);
      send_func(response_message);
      break;
    }
    case protobuf::GeneralMsg::REMOVE_PROJECT_REQUEST_T: {
      remove_project(msg.remove_project_request().name(), &response_message);
      send_func(response_message);
      break;
    }
    case protobuf::GeneralMsg::SET_ACTIVE_PROJECT_REQUEST_T: {
      set_active_project(msg.set_active_project_request().name(),
                         &response_message);
      send_func(response_message);
      break;
    }
    case protobuf::GeneralMsg::SET_PROJECT_METADATA_REQUEST_T: {
      set_project_metadata(msg.set_project_metadata_request().metadata(), &response_message);
      send_func(response_message);
      break;
    }
    case protobuf::GeneralMsg::EXPERIMENT_DATA_COLLECTION_START_REQUEST_T: {
      experiment_data_collection_start(&response_message, msg.experiment_data_collection_start_request().name());
      send_func(response_message);
      time_filter_map_.clear();
      break;
    }
    case protobuf::GeneralMsg::EXPERIMENT_DATA_COLLECTION_STOP_REQUEST_T: {
      experiment_data_collection_stop(&response_message);
      send_func(response_message);
      time_filter_map_.clear();
      break;
    }
    case protobuf::GeneralMsg::RENAME_EXPERIMENT_REQUEST_T: {
      rename_experiment(msg.rename_experiment_request().old_name(),
                        msg.rename_experiment_request().new_name(),
                              &response_message);
      send_func(response_message);
      break;
    }
    case protobuf::GeneralMsg::REMOVE_EXPERIMENT_REQUEST_T: {
      remove_experiment(msg.remove_experiment_request().name(),
                        &response_message);
      send_func(response_message);
      break;
    }
    case protobuf::GeneralMsg::EXPERIMENT_PLAYBACK_START_REQUEST_T: {
      // Response message has to be sent before status can be changed because the playback can start before response_message is sent.
      if (experiment_playback_start(msg.experiment_playback_start_request(), &response_message)) {
        send_func(response_message);
        status_observer_.set_status(protobuf::StatusMsg::EXPERIMENT_PLAYBACK);
      } else {
        send_func(response_message);
      }
      break;
    }
    case protobuf::GeneralMsg::EXPERIMENT_PLAYBACK_STOP_REQUEST_T: {
      experiment_playback_stop(&response_message);
      send_func(response_message);
      break;
    }
    case protobuf::GeneralMsg::SET_EXPERIMENT_METADATA_REQUEST_T: {
      set_experiment_metadata(msg.set_experiment_metadata_request(), &response_message);
      send_func(response_message);
      break;
    }
    case protobuf::GeneralMsg::DEBUG_T: {
      /*
       * If the program is not printing all incomming/outgoing messages,
       * then uncomment the following two lines. Otherwise this print-out is redundant.
       */

      //std::cout << "Received debug message: "
          //      << msg.debug_message().debug_message() << std::endl;
      break;
    }
    case protobuf::GeneralMsg::ERROR_T: {
      handle_error(
          "Received error message from client: "
              + protobuf::GeneralMsg::SubType_Name(msg.sub_type()));
      break;
    }
    default: {
      handle_error(
          "Received unexpected type: "
              + protobuf::GeneralMsg::SubType_Name(msg.sub_type()));
      break;
    }
  }
}

bool ClientCallbackImpl::is_valid_sensor_id(int id) {
  for (auto& sensor : sensors_) {
    if (sensor->get_id() == id) {
      return true;
    }
  }
  return false;
}

void ClientCallbackImpl::handle_error(const std::string& txt) {
  std::cerr << "ClientCallbackImpl: " << txt << std::endl;
}

void ClientCallbackImpl::project_list(
    protobuf::GeneralMsg* response_message) {
  response_message->set_sub_type(protobuf::GeneralMsg::PROJECT_LIST_T);
  protobuf::ProjectListMsg* project_list_msg = new protobuf::ProjectListMsg();
  response_message->set_allocated_project_list(project_list_msg);
  std::vector<std::string> project_name_list =
      project_handler_->get_all_project_names();
  for (auto& project_name : project_name_list) {
    project_list_msg->add_projects(project_name);
  }
}

void ClientCallbackImpl::active_project(protobuf::GeneralMsg* general_msg) {
  general_msg->set_sub_type(protobuf::GeneralMsg::ACTIVE_PROJECT_T);
  protobuf::ActiveProjectMsg* active_project_msg =
      new protobuf::ActiveProjectMsg();
  general_msg->set_allocated_active_project(active_project_msg);
  boost::optional<std::string> name = project_handler_->get_active_project();
  if (name) {
    active_project_msg->set_name(*name);
  }
}

void ClientCallbackImpl::set_dynamic_event(const protobuf::DynamicEventStruct& dynamic_event_struct, protobuf::GeneralMsg* response_message) {
  response_message->set_sub_type(
      protobuf::GeneralMsg::SET_DYNAMIC_EVENT_RESPONSE_T);
  protobuf::SetDynamicEventResponseMsg* sub_type =
      new protobuf::SetDynamicEventResponseMsg();

  // Copy DynamicEventStruct from incoming message to file storage message
  protobuf::GeneralMsg file_storage_msg;
  file_storage_msg.set_sub_type(protobuf::GeneralMsg::DYNAMIC_EVENT_T);
  protobuf::DynamicEventMsg* file_subtype_msg = new protobuf::DynamicEventMsg();
  protobuf::DynamicEventStruct* dynamic_event_struct_copy = new protobuf::DynamicEventStruct(dynamic_event_struct);
  file_subtype_msg->set_allocated_dynamic_event(dynamic_event_struct_copy);
  file_storage_msg.set_allocated_dynamic_event(file_subtype_msg);

  if (dynamic_event_struct.time() < 0) {
    sub_type->set_response_type(
        protobuf::SetDynamicEventResponseMsg::ILLEGAL_TIME);
  } else {
    switch (status_observer_.get_status()) {
      case protobuf::StatusMsg::DATA_COLLECTION: {
        ProjectHandlerErrorCode data_err = project_handler_->write_dynamic_event(file_storage_msg);
        ProjectHandlerErrorCode text_err = project_handler_->write_dynamic_event_text(dynamic_event_struct.message(), dynamic_event_struct.time());
        ProjectHandlerErrorCode merged_err;

        if((data_err == ProjectHandlerErrorCode::SUCCESS) &&
            (text_err == ProjectHandlerErrorCode::SUCCESS)){
            merged_err = ProjectHandlerErrorCode::SUCCESS;
        }else if((data_err == ProjectHandlerErrorCode::NO_ACTIVE_PROJECT) ||
            (text_err == ProjectHandlerErrorCode::NO_ACTIVE_PROJECT)){
          merged_err = ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
        }else{
          merged_err = ProjectHandlerErrorCode::IO_ERROR;
        }

        switch (merged_err) {
          case ProjectHandlerErrorCode::SUCCESS: {
            sub_type->set_response_type(
                protobuf::SetDynamicEventResponseMsg::SUCCESS);
            break;
          }
          case ProjectHandlerErrorCode::NO_ACTIVE_PROJECT: {
            sub_type->set_response_type(
                protobuf::SetDynamicEventResponseMsg::NO_ACTIVE_PROJECT);
            break;
          }
          default: {
            sub_type->set_response_type(
                protobuf::SetDynamicEventResponseMsg::OTHER_ERROR);
            break;
          }
        }
        break;
      }
      default: {
        sub_type->set_response_type(
            protobuf::SetDynamicEventResponseMsg::NOT_IN_DATA_COLLECTION_MODE);
        break;
      }
    }
  }

  response_message->set_allocated_set_dynamic_event_response(sub_type);
}

void ClientCallbackImpl::set_settings(const protobuf::SettingsRequestMsg& settings_request_msg, protobuf::GeneralMsg* response_message) {
  // Protect lists
  std::lock_guard<std::mutex> lock(mutex_);

  response_message->set_sub_type(protobuf::GeneralMsg::SETTINGS_RESPONSE_T);
  protobuf::SettingsResponseMsg* sub_type = new protobuf::SettingsResponseMsg();
  response_message->set_allocated_settings_response(sub_type);

  std::vector<int> new_ids;  // Intermediate vector, to enable rollback in case an id is illegal.

  bool sensor_ids_valid = true;
  for (int i = 0; i < settings_request_msg.sensor_ids_size(); i++) {
    int sensor_id = settings_request_msg.sensor_ids(i);
    if (is_valid_sensor_id(sensor_id)) {
      new_ids.push_back(sensor_id);
    } else {
      sensor_ids_valid = false;
      new_ids.clear();
      break;
    }
  }

  if (sensor_ids_valid) {  // Apply all settings here
    valid_sensor_type_ids_.clear();
    for (int id : new_ids) {
      valid_sensor_type_ids_.push_back(id);
    }
    min_time_ = settings_request_msg.min_time();

    sub_type->set_response_type(protobuf::SettingsResponseMsg::SUCCESS);
  } else {  // One sensor ID was illegal. Apply no settings.
    sub_type->set_response_type(protobuf::SettingsResponseMsg::ILLEGAL_SENSOR_ID);
  }
  // Sort vector to enable binary search
  std::sort(valid_sensor_type_ids_.begin(), valid_sensor_type_ids_.end());
}

void ClientCallbackImpl::create_new_project(const std::string& name,
                                            protobuf::GeneralMsg* general_msg) {
  general_msg->set_sub_type(
      protobuf::GeneralMsg::CREATE_NEW_PROJECT_RESPONSE_T);
  protobuf::CreateNewProjectResponseMsg* response_msg =
      new protobuf::CreateNewProjectResponseMsg();
  switch (ClientCallbackImpl::project_handler_->create_project(name)) {
    case ProjectHandlerErrorCode::SUCCESS: {
      response_msg->set_response_type(
          protobuf::CreateNewProjectResponseMsg::SUCCESS);
      break;
    }
    case ProjectHandlerErrorCode::NAME_TAKEN: {
      response_msg->set_response_type(
          protobuf::CreateNewProjectResponseMsg::NAME_TAKEN);
      break;
    }
    case ProjectHandlerErrorCode::ILLEGAL_NAME: {
      response_msg->set_response_type(
          protobuf::CreateNewProjectResponseMsg::ILLEGAL_NAME);
      break;
    }
    default: {
      response_msg->set_response_type(
          protobuf::CreateNewProjectResponseMsg::OTHER_ERROR);
      break;
    }
  }
  general_msg->set_allocated_create_new_project_response(response_msg);
}

void ClientCallbackImpl::remove_project(const std::string& name,
                                        protobuf::GeneralMsg* general_msg) {
  general_msg->set_sub_type(
      protobuf::GeneralMsg::REMOVE_PROJECT_RESPONSE_T);
  protobuf::RemoveProjectResponseMsg* response_msg =
      new protobuf::RemoveProjectResponseMsg();
  switch (ClientCallbackImpl::project_handler_->remove_project(name)) {
    case ProjectHandlerErrorCode::SUCCESS: {
      response_msg->set_response_type(
          protobuf::RemoveProjectResponseMsg::SUCCESS);
      break;
    }
    case ProjectHandlerErrorCode::PROJECT_NOT_FOUND: {
      response_msg->set_response_type(
          protobuf::RemoveProjectResponseMsg::PROJECT_NOT_FOUND);
      break;
    }
    default: {
      response_msg->set_response_type(
          protobuf::RemoveProjectResponseMsg::OTHER_ERROR);
      break;
    }
  }
  general_msg->set_allocated_remove_project_response(response_msg);
}

void ClientCallbackImpl::set_active_project(const std::string& name,
                                            protobuf::GeneralMsg* general_msg) {
  general_msg->set_sub_type(
      protobuf::GeneralMsg::SET_ACTIVE_PROJECT_RESPONSE_T);
  protobuf::SetActiveProjectResponseMsg* response_msg =
      new protobuf::SetActiveProjectResponseMsg();
  switch (status_observer_.get_status()) {
    //Only set active project if the server is in idle. Otherwise problems will occur
    case protobuf::StatusMsg::IDLE: {
      switch (ClientCallbackImpl::project_handler_->set_active_project(name)) {
        case ProjectHandlerErrorCode::SUCCESS: {
          response_msg->set_response_type(
              protobuf::SetActiveProjectResponseMsg::SUCCESS);
          break;
        }
        case ProjectHandlerErrorCode::PROJECT_NOT_FOUND: {
          response_msg->set_response_type(
              protobuf::SetActiveProjectResponseMsg::PROJECT_NOT_FOUND);
          break;
        }
        default: {
          response_msg->set_response_type(
              protobuf::SetActiveProjectResponseMsg::OTHER_ERROR);
          break;
        }
      }
      general_msg->set_allocated_set_active_project_response(response_msg);
      break;
    }
    default: {
      response_msg->set_response_type(
                    protobuf::SetActiveProjectResponseMsg::NOT_IN_IDLE_MODE);
    }
  }
}

void ClientCallbackImpl::set_project_metadata(const protobuf::ProjectMetadataStruct& project_metadata_struct, protobuf::GeneralMsg* response_message) {
  response_message->set_sub_type(
      protobuf::GeneralMsg::SET_PROJECT_METADATA_RESPONSE_T);
  protobuf::SetProjectMetadataResponseMsg* sub_type =
      new protobuf::SetProjectMetadataResponseMsg();

  // Copy ProjectMetadataStruct from incoming message to file storage message
  protobuf::GeneralMsg file_storage_msg;
  file_storage_msg.set_sub_type(protobuf::GeneralMsg::PROJECT_METADATA_T);
  protobuf::ProjectMetadataMsg* file_subtype_msg = new protobuf::ProjectMetadataMsg();
  protobuf::ProjectMetadataStruct* project_metadata_struct_copy = new protobuf::ProjectMetadataStruct(project_metadata_struct);
  file_subtype_msg->set_allocated_metadata(project_metadata_struct_copy);
  file_storage_msg.set_allocated_project_metadata(file_subtype_msg);

  switch (project_handler_->write_project_metadata(file_storage_msg)) {
    case ProjectHandlerErrorCode::SUCCESS: {
      sub_type->set_response_type(
          protobuf::SetProjectMetadataResponseMsg::SUCCESS);
      break;
    }
    case ProjectHandlerErrorCode::NO_ACTIVE_PROJECT: {
      sub_type->set_response_type(
          protobuf::SetProjectMetadataResponseMsg::NO_ACTIVE_PROJECT);
      break;
    }
    default: {
      sub_type->set_response_type(
          protobuf::SetProjectMetadataResponseMsg::OTHER_ERROR);
      break;
    }
  }

  response_message->set_allocated_set_project_metadata_response(sub_type);
}

void ClientCallbackImpl::rename_project(const std::string& old_name,
                                        const std::string& new_name,
                                        protobuf::GeneralMsg* general_msg) {
  general_msg->set_sub_type(protobuf::GeneralMsg::RENAME_PROJECT_RESPONSE_T);
  protobuf::RenameProjectResponseMsg* response_msg =
      new protobuf::RenameProjectResponseMsg();
  switch (ClientCallbackImpl::project_handler_->rename_project(old_name,
                                                              new_name)) {
    case ProjectHandlerErrorCode::SUCCESS: {
      response_msg->set_response_type(
          protobuf::RenameProjectResponseMsg::SUCCESS);
      break;
    }
    case ProjectHandlerErrorCode::ILLEGAL_NAME: {
      response_msg->set_response_type(
          protobuf::RenameProjectResponseMsg::ILLEGAL_NAME);
      break;
    }
    case ProjectHandlerErrorCode::NAME_TAKEN: {
      response_msg->set_response_type(
          protobuf::RenameProjectResponseMsg::NAME_TAKEN);
      break;
    }
    case ProjectHandlerErrorCode::PROJECT_NOT_FOUND: {
      response_msg->set_response_type(
          protobuf::RenameProjectResponseMsg::PROJECT_NOT_FOUND);
      break;
    }
    default: {
      response_msg->set_response_type(
          protobuf::RenameProjectResponseMsg::OTHER_ERROR);
      break;
    }
  }
  general_msg->set_allocated_rename_project_response(response_msg);
}

void ClientCallbackImpl::rename_experiment(const std::string& old_name,
                                           const std::string& new_name,
                                           protobuf::GeneralMsg* general_message) {

  general_message->set_sub_type(protobuf::GeneralMsg::RENAME_EXPERIMENT_RESPONSE_T);
  protobuf::RenameExperimentResponseMsg* response_msg =
      new protobuf::RenameExperimentResponseMsg();
  if(status_observer_.get_status() != protobuf::StatusMsg::DATA_COLLECTION &&
           status_observer_.get_status() != protobuf::StatusMsg::EXPERIMENT_PLAYBACK){
    switch (ClientCallbackImpl::project_handler_->rename_experiment(old_name,
                                                                 new_name)) {
       case ProjectHandlerErrorCode::SUCCESS: {
         response_msg->set_response_type(
             protobuf::RenameExperimentResponseMsg::SUCCESS);
         break;
       }
       case ProjectHandlerErrorCode::NO_ACTIVE_PROJECT: {
         response_msg->set_response_type(
             protobuf::RenameExperimentResponseMsg::NO_ACTIVE_PROJECT);
         break;
       }
       case ProjectHandlerErrorCode::NAME_TAKEN: {
         response_msg->set_response_type(
             protobuf::RenameExperimentResponseMsg::NAME_TAKEN);
         break;
       }
       case ProjectHandlerErrorCode::EXPERIMENT_NOT_FOUND: {
         response_msg->set_response_type(
             protobuf::RenameExperimentResponseMsg::EXPERIMENT_NOT_FOUND);
         break;
       }
       default: {
         response_msg->set_response_type(
             protobuf::RenameExperimentResponseMsg::OTHER_ERROR);
         break;
       }
     }
   }else{
     response_msg->set_response_type(
         protobuf::RenameExperimentResponseMsg::OTHER_ERROR);
     //TODO: Fix new enum in protocol that says "In playback or in data_collection"
   }
    general_message->set_allocated_rename_experiment_response(response_msg);
}

void ClientCallbackImpl::remove_experiment(const std::string& name,
                                           protobuf::GeneralMsg* response_message) {
  response_message->set_sub_type(protobuf::GeneralMsg::REMOVE_EXPERIMENT_RESPONSE_T);
  protobuf::RemoveExperimentResponseMsg* response_msg =
      new protobuf::RemoveExperimentResponseMsg();

  switch (ClientCallbackImpl::project_handler_->remove_experiment(name)) {
    case ProjectHandlerErrorCode::SUCCESS: {
      response_msg->set_response_type(
          protobuf::RemoveExperimentResponseMsg::SUCCESS);
      break;
    }
    case ProjectHandlerErrorCode::NO_ACTIVE_PROJECT: {
      response_msg->set_response_type(
          protobuf::RemoveExperimentResponseMsg::NO_ACTIVE_PROJECT);
      break;
    }
    case ProjectHandlerErrorCode::EXPERIMENT_NOT_FOUND: {
      response_msg->set_response_type(
          protobuf::RemoveExperimentResponseMsg::EXPERIMENT_NOT_FOUND);
      break;
    }
    default: {
      response_msg->set_response_type(
          protobuf::RemoveExperimentResponseMsg::OTHER_ERROR);
      break;
    }
  }
  response_message->set_allocated_remove_experiment_response(response_msg);
}

void ClientCallbackImpl::experiment_data_collection_start(protobuf::GeneralMsg* response_message, const std::string& name) {
  protobuf::ExperimentDataCollectionStartResponseMsg* data_col_start_response =
      new protobuf::ExperimentDataCollectionStartResponseMsg();
  response_message->set_sub_type(
      protobuf::GeneralMsg::EXPERIMENT_DATA_COLLECTION_START_RESPONSE_T);
  response_message->set_allocated_experiment_data_collection_start_response(
      data_col_start_response);
  switch (status_observer_.get_status()) {
    case protobuf::StatusMsg::IDLE: {
      switch (project_handler_->create_experiment(name, sensors_)) {
        case ProjectHandlerErrorCode::SUCCESS: {
          status_observer_.set_status(protobuf::StatusMsg::DATA_COLLECTION);
          data_col_start_response->set_response_type(
              protobuf::ExperimentDataCollectionStartResponseMsg::SUCCESS);
          break;
        }
        case ProjectHandlerErrorCode::NO_ACTIVE_PROJECT: {
          data_col_start_response->set_response_type(
              protobuf::ExperimentDataCollectionStartResponseMsg::NO_ACTIVE_PROJECT);
          break;
        }
        case ProjectHandlerErrorCode::NAME_TAKEN: {
          data_col_start_response->set_response_type(
              protobuf::ExperimentDataCollectionStartResponseMsg::NAME_TAKEN);
          break;
        }
        case ProjectHandlerErrorCode::ILLEGAL_NAME: {
          data_col_start_response->set_response_type(
              protobuf::ExperimentDataCollectionStartResponseMsg::ILLEGAL_NAME);
          break;
        }
        default: {
          data_col_start_response->set_response_type(
              protobuf::ExperimentDataCollectionStartResponseMsg::OTHER_ERROR);
          break;
        }
      }
      break;
    }
    default: {
      data_col_start_response->set_response_type(
          protobuf::ExperimentDataCollectionStartResponseMsg::NOT_IN_IDLE_MODE);
      break;
    }
  }
}

void ClientCallbackImpl::experiment_data_collection_stop(protobuf::GeneralMsg* response_message) {
  protobuf::ExperimentDataCollectionStopResponseMsg* data_col_stop_response =
      new protobuf::ExperimentDataCollectionStopResponseMsg();
  response_message->set_sub_type(
      protobuf::GeneralMsg::EXPERIMENT_DATA_COLLECTION_STOP_RESPONSE_T);
  response_message->set_allocated_experiment_data_collection_stop_response(
      data_col_stop_response);
  switch (status_observer_.get_status()) {
    case protobuf::StatusMsg::DATA_COLLECTION: {
      if (project_handler_->experiment_data_collection_stop() == ProjectHandlerErrorCode::SUCCESS) {
        status_observer_.set_status(protobuf::StatusMsg::IDLE);
        project_handler_->set_no_active_experiment();
        data_col_stop_response->set_response_type(protobuf::ExperimentDataCollectionStopResponseMsg::SUCCESS);
      } else {
        data_col_stop_response->set_response_type(protobuf::ExperimentDataCollectionStopResponseMsg::OTHER_ERROR);
      }
      break;
    }
    default: {
      data_col_stop_response->set_response_type(protobuf::ExperimentDataCollectionStopResponseMsg::NOT_IN_DATA_COLLECTION_MODE);
      break;
    }
  }
}

bool ClientCallbackImpl::experiment_playback_start(const protobuf::ExperimentPlaybackStartRequestMsg& experiment_playback_start_request_msg, protobuf::GeneralMsg* response_message) {
  // Protect lists
  std::lock_guard<std::mutex> lock(mutex_);

  protobuf::ExperimentPlaybackStartResponseMsg* playback_start_response =
      new protobuf::ExperimentPlaybackStartResponseMsg();
  response_message->set_sub_type(
      protobuf::GeneralMsg::EXPERIMENT_PLAYBACK_START_RESPONSE_T);
  response_message->set_allocated_experiment_playback_start_response(
      playback_start_response);

  valid_sensor_type_ids_playback_.clear();
  for (int i = 0; i < experiment_playback_start_request_msg.sensor_ids_size(); i++) {
    valid_sensor_type_ids_playback_.push_back(experiment_playback_start_request_msg.sensor_ids(i));
  }

  switch (status_observer_.get_status()) {
    case protobuf::StatusMsg::IDLE: {
      switch (project_handler_->set_active_experiment(experiment_playback_start_request_msg.name())) {
        case ProjectHandlerErrorCode::SUCCESS: {
          playback_start_response->set_response_type(
              protobuf::ExperimentPlaybackStartResponseMsg::SUCCESS);
          break;
        }
        case ProjectHandlerErrorCode::NO_ACTIVE_PROJECT: {
          playback_start_response->set_response_type(
              protobuf::ExperimentPlaybackStartResponseMsg::NO_ACTIVE_PROJECT);
          break;
        }
        case ProjectHandlerErrorCode::EXPERIMENT_NOT_FOUND: {
          playback_start_response->set_response_type(
              protobuf::ExperimentPlaybackStartResponseMsg::EXPERIMENT_NOT_FOUND);
          break;
        }
        default: {
          playback_start_response->set_response_type(
              protobuf::ExperimentPlaybackStartResponseMsg::OTHER_ERROR);
          break;
        }
      }
      return true;
      break;
    }
    default: {
      playback_start_response->set_response_type(protobuf::ExperimentPlaybackStartResponseMsg::NOT_IN_IDLE_MODE);
      return false;
      break;
    }
  }
}

void ClientCallbackImpl::experiment_playback_stop(protobuf::GeneralMsg* response_message) {
  protobuf::ExperimentPlaybackStopResponseMsg* playback_stop_response =
      new protobuf::ExperimentPlaybackStopResponseMsg();
  response_message->set_sub_type(
      protobuf::GeneralMsg::EXPERIMENT_PLAYBACK_STOP_RESPONSE_T);
  response_message->set_allocated_experiment_playback_stop_response(
      playback_stop_response);
  switch (status_observer_.get_status()) {
    case protobuf::StatusMsg::EXPERIMENT_PLAYBACK: {
      project_handler_->set_no_active_experiment();
      status_observer_.set_status(protobuf::StatusMsg::IDLE);
      playback_stop_response->set_response_type(protobuf::ExperimentPlaybackStopResponseMsg::SUCCESS);
      break;
    }
    default: {
      playback_stop_response->set_response_type(protobuf::ExperimentPlaybackStopResponseMsg::NOT_IN_PLAYBACK_MODE);
      break;
    }
  }
}

void ClientCallbackImpl::set_experiment_metadata(const protobuf::SetExperimentMetadataRequestMsg& set_experiment_metadata_request_msg, protobuf::GeneralMsg* response_message) {
  response_message->set_sub_type(
      protobuf::GeneralMsg::SET_EXPERIMENT_METADATA_RESPONSE_T);
  protobuf::SetExperimentMetadataResponseMsg* sub_type =
      new protobuf::SetExperimentMetadataResponseMsg();

  std::vector<std::string> tags;
  for (int i = 0; i < set_experiment_metadata_request_msg.tags_size(); i++) {
    tags.push_back(set_experiment_metadata_request_msg.tags(i));
  }
  boost::optional<std::string> notes;
  if (set_experiment_metadata_request_msg.has_notes()) {
    notes = set_experiment_metadata_request_msg.notes();
  } else {
    notes = boost::none;
  }

  switch (project_handler_->write_experiment_metadata(set_experiment_metadata_request_msg.experiment_name(), tags, notes)) {
    case ProjectHandlerErrorCode::SUCCESS: {
      sub_type->set_response_type(
          protobuf::SetExperimentMetadataResponseMsg::SUCCESS);
      break;
    }
    case ProjectHandlerErrorCode::NO_ACTIVE_PROJECT: {
      sub_type->set_response_type(
          protobuf::SetExperimentMetadataResponseMsg::NO_ACTIVE_PROJECT);
      break;
    }
    case ProjectHandlerErrorCode::EXPERIMENT_NOT_FOUND: {
      sub_type->set_response_type(
          protobuf::SetExperimentMetadataResponseMsg::EXPERIMENT_NOT_FOUND);
      break;
    }
    default: {
      sub_type->set_response_type(
          protobuf::SetExperimentMetadataResponseMsg::OTHER_ERROR);
      break;
    }
  }

  response_message->set_allocated_set_experiment_metadata_response(sub_type);
}
