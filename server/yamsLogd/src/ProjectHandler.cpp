/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2014  Max Halldén
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

#include "ProjectHandler.h"

#include <memory>
#include <system_error>
#include <unistd.h>
#include <sys/types.h>
#include <sstream>

#include <boost/filesystem.hpp>

#include "ProjectHandlerConstants.h"

namespace fs = boost::filesystem;

const std::string               NO_ACTIVE           = "";
const boost::filesystem::perms  FILE_PERMISSIONS    = fs::owner_all | fs::group_exe | fs::group_read;

/*
 * This class handles all the requests that applies to project or experiment.
 * It's functions can be called by threads simultaneously therefore it has to be
 * thread safe.
 */

ProjectHandler::ProjectHandler(const std::string& dir_path) {
  active_project_ = NO_ACTIVE;

  active_exp_ = NO_ACTIVE;

  // We want path to end with /
  if (dir_path.at(dir_path.length() -1) != '/') {
    dir_path_ = dir_path + "/";
  } else {
    dir_path_ = dir_path;
  }

  fs::path path(dir_path_);
  try{
    if (!fs::exists(path)) {
      fs::create_directories(path);
      fs::permissions(path, FILE_PERMISSIONS);
    }

    fs::directory_iterator end_iter;
    bool success = true;
    for (fs::directory_iterator dir_itr(dir_path_);
          dir_itr != end_iter;
          ++dir_itr) {
      if (fs::is_directory(dir_itr->status())) {
        ProjectHandlerErrorCode err = create_project(dir_itr->path().filename().string());
        if (err != ProjectHandlerErrorCode::SUCCESS) {
          success = false;
        }
      }
    }
    if(success){
      std::cerr << "# Could not open all projects or experiments" << std::endl;
    }
  }catch(std::exception& e){
    std::cerr << "# ERROR: Could not open all projects or experiments" << e.what() << std::endl;
  }
}

ProjectHandler::~ProjectHandler() {}

/*
 * The active project is used so that when the client wants to create an experiment,
 * then the experiment is created under that active project.
 */
ProjectHandlerErrorCode ProjectHandler::set_active_project(const std::string& name) {
  std::unordered_map<std::string, std::shared_ptr<Project>>::iterator new_project_it;
std::unordered_map<std::string, std::shared_ptr<Project>>::iterator map_end;
  {
    std::lock_guard<std::mutex> lock(mutex_);
    new_project_it = project_map_.find(name);
    map_end = project_map_.end();
    if (new_project_it != map_end) {
      active_project_ = name;
    }
  }
  if (new_project_it == map_end) {
    // No project with desired name exists
    return ProjectHandlerErrorCode::PROJECT_NOT_FOUND;
  } else {
    // Activate callback. No reason to lock this.
    active_project_modified_signal_();
    return ProjectHandlerErrorCode::SUCCESS;
  }
}

boost::optional<std::string> ProjectHandler::get_active_project() {
  std::lock_guard<std::mutex> lock(mutex_);

  if (active_project_ == NO_ACTIVE) {
    return boost::none;
  } else {
    return active_project_;
  }
}

/*
 * Active experiment is a server-only thing. It there so that the projecthandler easily
 * can read or write data to desiered experiment.
 */

ProjectHandlerErrorCode ProjectHandler::set_active_experiment(const std::string& name) {
  std::lock_guard<std::mutex> lock(mutex_);
  if (name == NO_ACTIVE) {
    return ProjectHandlerErrorCode::ILLEGAL_NAME;
  }
  auto it = project_map_.find(active_project_);
  if (it != project_map_.end()) {
    if (it->second->experiment_exists(name)) {
      active_exp_ = name;
      return ProjectHandlerErrorCode::SUCCESS;
    } else {
      return ProjectHandlerErrorCode::EXPERIMENT_NOT_FOUND;
    }
  }
  return ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
}

void ProjectHandler::set_no_active_experiment() {
  std::lock_guard<std::mutex> lock(mutex_);

  active_exp_ = NO_ACTIVE;
}

ProjectHandlerErrorCode ProjectHandler::write_dynamic_event(const protobuf::GeneralMsg& msg) const {
  return write_protobuf_data(msg);
}

ProjectHandlerErrorCode ProjectHandler::write_dynamic_event_text(std::string dynamic_event_text, double time) const {
  std::ostringstream strs;
  strs << time;
  std::string time_string = strs.str();
  std::string dynamic_event_str = "%"  + time_string + ",7," + dynamic_event_text;
  std::cout << dynamic_event_str << std::endl;
  return write_text_data(dynamic_event_str);
}


ProjectHandlerErrorCode ProjectHandler::write_project_metadata(const protobuf::GeneralMsg& msg) const {
  ProjectHandlerErrorCode err;
  {
    std::lock_guard<std::mutex> lock(mutex_);

    if (!active_proj()) {
      return ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
    }

    err = project_map_.at(active_project_)->write_metadata(msg);
  }
  if (err == ProjectHandlerErrorCode::SUCCESS) {
    // Callback. No reason to keep lock here.
    active_project_metadata_modified_signal_();
  }
  return err;
}


boost::optional<protobuf::GeneralMsg> ProjectHandler::read_project_metadata(ProjectHandlerErrorCode& err_out) {
  std::lock_guard<std::mutex> lock(mutex_);

  if (!active_proj()) {
    err_out = ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
    return boost::none;
  }

  boost::optional<protobuf::GeneralMsg> msg = project_map_.at(active_project_)->read_metadata(err_out);

  if (err_out == ProjectHandlerErrorCode::SUCCESS) {
    return msg;
  } else {
    return boost::none;
  }
}


ProjectHandlerErrorCode ProjectHandler::write_experiment_metadata(const std::string& experiment_name, const std::vector<std::string>& tags, const boost::optional<std::string>& notes) const {
  ProjectHandlerErrorCode err;
  {
    std::lock_guard<std::mutex> lock(mutex_);

    if (!active_proj()) {
      return ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
    }

    std::shared_ptr<Experiment> experiment = project_map_.at(active_project_)->get_experiment(experiment_name);
    if (experiment == nullptr) {
      return ProjectHandlerErrorCode::EXPERIMENT_NOT_FOUND;
    }
    err = experiment->write_metadata(tags, notes);
  }
  if (err == ProjectHandlerErrorCode::SUCCESS) {
    active_project_experiments_metadata_modified_signal_();
  }
  return err;
}

/*
 * This function reads all the experiments metadata that is in the active experiment.
 * This has to be done when a client sets an active project. The client then must get all the metadata
 * of all the experiments in the active project.
 */

std::vector<protobuf::GeneralMsg> ProjectHandler::read_active_project_experiments_metadata(ProjectHandlerErrorCode& err_out) {
  std::lock_guard<std::mutex> lock(mutex_);

  std::vector<protobuf::GeneralMsg> ret;

  if (!active_proj()) {
    err_out = ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
    return std::vector<protobuf::GeneralMsg>();  // Return empty vector if error.
  }

  std::shared_ptr<Project> active_project = project_map_.at(active_project_);
  std::vector<std::string> experiment_names = active_project->list_experiments();

  for (std::string& exp : experiment_names) {
    // No need to check that map returns are valid
    boost::optional<protobuf::ExperimentMetadataMsg::MetadataStruct> msg = active_project->get_experiment(exp)->read_metadata(err_out);
    if (err_out != ProjectHandlerErrorCode::SUCCESS && err_out != ProjectHandlerErrorCode::END_OF_FILE) {
      return std::vector<protobuf::GeneralMsg>();  // Return empty vector if error.
    }
    if (msg) {
      protobuf::GeneralMsg general_msg;
      general_msg.set_sub_type(protobuf::GeneralMsg::EXPERIMENT_METADATA_T);
      protobuf::ExperimentMetadataMsg* sub_msg = new protobuf::ExperimentMetadataMsg();
      general_msg.set_allocated_experiment_metadata(sub_msg);
      protobuf::ExperimentMetadataMsg::MetadataStruct* allocated_metadata = new protobuf::ExperimentMetadataMsg::MetadataStruct(msg.get());
      sub_msg->set_experiment_name(exp);
      sub_msg->set_allocated_metadata(allocated_metadata);
      ret.push_back(general_msg);
    }
  }

  return ret;
}

ProjectHandlerErrorCode ProjectHandler::write_protobuf_data(const protobuf::GeneralMsg& msg) const {
  std::lock_guard<std::mutex> lock(mutex_);

  if (!active_proj()) {
    return ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
  }
  if (!active_exp()) {
    return ProjectHandlerErrorCode::NO_ACTIVE_EXPERIMENT;
  }

  // No need to check that map returns are valid
  return project_map_.at(active_project_)->get_experiment(active_exp_)->write_protobuf_data(msg);
}

ProjectHandlerErrorCode ProjectHandler::write_text_data(const std::string& data) const {
  std::lock_guard<std::mutex> lock(mutex_);

  if (!active_proj()) {
    return ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
  }
  if (!active_exp()) {
    return ProjectHandlerErrorCode::NO_ACTIVE_EXPERIMENT;
  }

  // No need to check that map returns are valid
  std::shared_ptr<Experiment> exp = project_map_.at(active_project_)->get_experiment(active_exp_);

  return exp->write_text_data(data);
}

ProjectHandlerErrorCode ProjectHandler::reset_read_pos() {
  std::lock_guard<std::mutex> lock(mutex_);

  if (! active_proj()) {

    return ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
  }
  if (!active_exp()) {
    return ProjectHandlerErrorCode::NO_ACTIVE_EXPERIMENT;
  }

  // No need to check that map returns are valid
  return project_map_.at(active_project_)->get_experiment(active_exp_)->reset_read_pos();
}

boost::optional<protobuf::GeneralMsg> ProjectHandler::read_next_data(ProjectHandlerErrorCode& err_out) {
  std::lock_guard<std::mutex> lock(mutex_);

  if (!active_proj()) {
    err_out = ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
    return boost::none;
  }
  if (!active_exp()) {
    err_out = ProjectHandlerErrorCode::NO_ACTIVE_EXPERIMENT;
    return boost::none;
  }

  // No need to check that map returns are valid
  return project_map_.at(active_project_)->get_experiment(active_exp_)->read_protobuf_data(err_out);
}

ProjectHandlerErrorCode ProjectHandler::remove_experiment(const std::string& name) {
  ProjectHandlerErrorCode err;
  {
    std::lock_guard<std::mutex> lock(mutex_);

    if (!active_proj()) {
      return ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
    }

    err = project_map_.at(active_project_)->remove_experiment(name);
  }
  if (err == ProjectHandlerErrorCode::SUCCESS) {
    active_project_experiment_list_modified_signal_();
  }
  return err;
}

// Implicitly sets the active experiment
ProjectHandlerErrorCode ProjectHandler::create_experiment(const std::string& name, const std::vector<boost::shared_ptr<AbstractSensor>>& sensors) {
  ProjectHandlerErrorCode err;
  {
    std::lock_guard<std::mutex> lock(mutex_);
    if (name == NO_ACTIVE) {
      return ProjectHandlerErrorCode::ILLEGAL_NAME;
    }
    if (!active_proj()) {
      return ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
    }

    err = project_map_.at(active_project_)->add_experiment(name);

    if (err == ProjectHandlerErrorCode::SUCCESS) {
      // No need to check that map returns are valid

      err = project_map_.at(active_project_)->get_experiment(name)->write_sensor_configuration_metadata(sensors);
    }
  }

  if (err == ProjectHandlerErrorCode::SUCCESS) {
    active_exp_ = name;
    active_project_experiment_list_modified_signal_();
    active_project_experiments_metadata_modified_signal_();
  }
  return err;
}

ProjectHandlerErrorCode ProjectHandler::rename_experiment(const std::string& old_name, const std::string& new_name) {
  ProjectHandlerErrorCode err;
  {
    std::lock_guard<std::mutex> lock(mutex_);

    if (!active_proj()) {
      return ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
    }

    err = project_map_.at(active_project_)->rename_experiment(old_name, new_name);
  }
  if (err == ProjectHandlerErrorCode::SUCCESS) {
    active_project_experiment_list_modified_signal_();
  }
  return err;
}

/*
 * DOES NOT WORK ATM
 */

ProjectHandlerErrorCode ProjectHandler::rename_project(const std::string& old_name, const std::string& new_name) {
  {
    std::lock_guard<std::mutex> lock(mutex_);

    if (project_map_.count(old_name) == 0) {
      return ProjectHandlerErrorCode::PROJECT_NOT_FOUND;
    }
    if (project_map_.count(new_name) != 0) {
      return ProjectHandlerErrorCode::NAME_TAKEN;
    }

    std::shared_ptr<Project> proj_p = project_map_.at(old_name);
    project_map_.erase(old_name);

    proj_p->rename_project(new_name);
    project_map_.insert(std::pair<std::string, std::shared_ptr<Project>>(new_name, proj_p));
  }
  project_list_modified_signal_();
  return ProjectHandlerErrorCode::SUCCESS;
}


ProjectHandlerErrorCode ProjectHandler::create_project(const std::string& name) {
  ProjectHandlerErrorCode err;
  {
    std::lock_guard<std::mutex> lock(mutex_);
    if (name == NO_ACTIVE) {
      return ProjectHandlerErrorCode::ILLEGAL_NAME;
    }
    if (project_map_.count(name) != 0) {
      return ProjectHandlerErrorCode::NAME_TAKEN;
    }
    project_map_.insert(std::pair<std::string, std::shared_ptr<Project>>
      (name, std::make_shared<Project>(dir_path_, name, err)));
  }
  if (err == ProjectHandlerErrorCode::SUCCESS) {
    project_list_modified_signal_();
  }
  return err;
}

ProjectHandlerErrorCode ProjectHandler::remove_project(const std::string& name) {
  ProjectHandlerErrorCode err;
  {
    std::lock_guard<std::mutex> lock(mutex_);
    if (project_map_.count(name) == 0) {
      return ProjectHandlerErrorCode::PROJECT_NOT_FOUND;
    }
    err = project_map_.at(name)->remove_all_experiments();
  }
  if (err == ProjectHandlerErrorCode::SUCCESS) {
    project_map_.erase(name);
    project_list_modified_signal_();
  }
  return err;
}

std::vector<std::string> ProjectHandler::get_all_project_names() {
  std::lock_guard<std::mutex> lock(mutex_);
  std::vector<std::string> names;
  names.reserve(project_map_.size());
  for (std::pair<std::string, std::shared_ptr<Project>> proj_pair : project_map_) {
      names.push_back(proj_pair.first);
  }
  return names;
}

std::vector<std::string> ProjectHandler::get_project_experiment_names(ProjectHandlerErrorCode& err_out) {
  std::lock_guard<std::mutex> lock(mutex_);
  std::vector<std::string> names;
  auto it = project_map_.find(active_project_);
  if (it == project_map_.end()) {
    err_out = ProjectHandlerErrorCode::NO_ACTIVE_PROJECT;
  } else {
    names = it->second.get()->list_experiments();
    err_out = ProjectHandlerErrorCode::SUCCESS;
  }
  return names;
}

void ProjectHandler::add_callback_interface(ModificationCallbackInterface* callback_interface) {
  active_project_modified_signal_.connect(boost::bind(&ModificationCallbackInterface::active_project_modified_callback, callback_interface));
  active_project_metadata_modified_signal_.connect(boost::bind(&ModificationCallbackInterface::active_project_metadata_modified_callback, callback_interface));
  project_list_modified_signal_.connect(boost::bind(&ModificationCallbackInterface::project_list_modified_callback, callback_interface));
  active_project_experiments_metadata_modified_signal_.connect(boost::bind(&ModificationCallbackInterface::active_project_experiments_metadata_modified_callback, callback_interface));
  active_project_experiment_list_modified_signal_.connect(boost::bind(&ModificationCallbackInterface::active_project_experiment_list_modified_callback, callback_interface));
}

bool ProjectHandler::active_proj() const {
  // ATTENTION: Not thread safe because only called by thread safe functions.
  return active_project_ != NO_ACTIVE;
}

bool ProjectHandler::active_exp() const {
  // ATTENTION: Not thread safe because only called by thread safe functions.
  return active_exp_ != NO_ACTIVE && active_project_ != NO_ACTIVE;
}

ProjectHandlerErrorCode ProjectHandler::experiment_data_collection_stop() {
  std::lock_guard<std::mutex> lock(mutex_);
  if (active_exp()) {
    if (project_map_.at(active_project_)->get_experiment(active_exp_)->set_to_read_mode()) {
      return ProjectHandlerErrorCode::SUCCESS;
    } else {
      return ProjectHandlerErrorCode::IO_ERROR;
    }
  } else {
    return ProjectHandlerErrorCode::NO_ACTIVE_EXPERIMENT;
  }
}

std::string ProjectHandler::get_active_experiment(){
  return active_exp_;
}
