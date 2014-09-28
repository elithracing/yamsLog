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

#include "ModificationCallbackImpl.h"

ModificationCallbackImpl::ModificationCallbackImpl(ProjectHandler& project_handler, const CommunicationServer& comm_server) : project_handler_(project_handler), comm_server_(comm_server) {
}

ModificationCallbackImpl::~ModificationCallbackImpl() {}

void ModificationCallbackImpl::active_project_modified_callback() {
  broadcast_active_project();
  broadcast_project_metadata();
  broadcast_experiment_list();
  broadcast_experiments_metadata();
}

void ModificationCallbackImpl::active_project_metadata_modified_callback() {
  broadcast_project_metadata();
}

void ModificationCallbackImpl::project_list_modified_callback() {
  broadcast_project_list();
}

void ModificationCallbackImpl::active_project_experiments_metadata_modified_callback() {
  broadcast_experiments_metadata();
}

void ModificationCallbackImpl::active_project_experiment_list_modified_callback() {
  broadcast_experiment_list();
}

void ModificationCallbackImpl::broadcast_active_project() {
  protobuf::GeneralMsg general_msg;
  protobuf::ActiveProjectMsg* active_project_msg = new protobuf::ActiveProjectMsg();
  boost::optional<std::string> name = project_handler_.get_active_project();
  active_project_msg->set_name(*name);
  if (active_project_msg) {
    general_msg.set_sub_type(protobuf::GeneralMsg::ACTIVE_PROJECT_T);
    general_msg.set_allocated_active_project(active_project_msg);
    comm_server_.broadcast(general_msg);
  }
}

void ModificationCallbackImpl::broadcast_project_metadata() {
  ProjectHandlerErrorCode err;
  boost::optional<protobuf::GeneralMsg> general_msg = project_handler_.read_project_metadata(err);
  if (err == ProjectHandlerErrorCode::SUCCESS && general_msg) {
    comm_server_.broadcast(general_msg.get());
  } else if (err != ProjectHandlerErrorCode::END_OF_FILE) {
    std::cerr << "ModificationCallbackImpl: Failed to read project metadata in active project (" << static_cast<int>(err) << ") " << std::endl;
  }
}

void ModificationCallbackImpl::broadcast_experiments_metadata() {
  ProjectHandlerErrorCode err;
  std::vector<protobuf::GeneralMsg> general_msg_list = project_handler_.read_active_project_experiments_metadata(err);
  if (err == ProjectHandlerErrorCode::SUCCESS) {
    for (protobuf::GeneralMsg& msg : general_msg_list) {
      comm_server_.broadcast(msg);
    }
  } else if (err != ProjectHandlerErrorCode::END_OF_FILE) {
     std::cerr << "ModificationCallbackImpl: Failed to get experiment metadata in active project (" << static_cast<int>(err) << ")" << std::endl;
  }
}

void ModificationCallbackImpl::broadcast_project_list() {
  protobuf::GeneralMsg general_msg;
  general_msg.set_sub_type(protobuf::GeneralMsg::PROJECT_LIST_T);
  protobuf::ProjectListMsg* project_list_msg = new protobuf::ProjectListMsg();
  general_msg.set_allocated_project_list(project_list_msg);
  std::vector<std::string> project_list = project_handler_.get_all_project_names();
  for (auto& project_name : project_list) {
    project_list_msg->add_projects(project_name);
  }
  comm_server_.broadcast(general_msg);
}

void ModificationCallbackImpl::broadcast_experiment_list() {
  protobuf::GeneralMsg general_msg;
  general_msg.set_sub_type(protobuf::GeneralMsg::EXPERIMENT_LIST_T);
  protobuf::ExperimentListMsg* experiment_list_msg = new protobuf::ExperimentListMsg();
  general_msg.set_allocated_experiment_list(experiment_list_msg);
  ProjectHandlerErrorCode err;
  std::vector<std::string> experiment_list = project_handler_.get_project_experiment_names(err);
  if (err == ProjectHandlerErrorCode::SUCCESS) {
    for (auto& experiment_name : experiment_list) {
      experiment_list_msg->add_names(experiment_name);
    }
    comm_server_.broadcast(general_msg);
  } else if (err == ProjectHandlerErrorCode::NO_ACTIVE_PROJECT) {
    // Do nothing
  } else {
    std::cerr << "ModificationCallbackImpl: Failed to get experiment names in active project (" << static_cast<int>(err) << ")" << std::endl;
  }
}
