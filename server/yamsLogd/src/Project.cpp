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

#include "Project.h"

#include <memory>

#include <boost/filesystem.hpp>

const std::string PROJECT_META_FILE_ENDING = ".project.meta";

namespace fs = boost::filesystem;
namespace io = google::protobuf::io;

/*
 * A Project object handles it's metadata file and links to the experiments stored in the project.
 */

Project::Project(const std::string& dir_path, const std::string& name, ProjectHandlerErrorCode& err_out) {
  project_dir_path_ = dir_path + name + "/";
  name_ = name;
  boost::system::error_code boost_err;
  fs::create_directory(project_dir_path_, boost_err);
  if (boost_err == boost::system::errc::success || boost_err == boost::system::errc::file_exists) {
    // Create metadata file
    meta_file_name_ = name_ + PROJECT_META_FILE_ENDING;
    if (fs::exists(fs::path(project_dir_path_ + meta_file_name_))) {
      meta_file_handler_ = std::fopen((project_dir_path_ + meta_file_name_).c_str(), "w");
    } else {
      meta_file_handler_ = std::fopen((project_dir_path_ + meta_file_name_).c_str(), "w+");
    }
    if(meta_file_handler_ == nullptr){
      initialized_ = false;
      err_out = ProjectHandlerErrorCode::IO_ERROR;
      return;
    }
    meta_file_input_stream_ = new io::FileInputStream(::fileno(meta_file_handler_));

    // Insert existing experiments found in project folder
    err_out = ProjectHandlerErrorCode::SUCCESS;
    fs::directory_iterator end_iter;

    for (fs::directory_iterator dir_itr(project_dir_path_);
          dir_itr != end_iter;
          ++dir_itr) {
      if (fs::is_directory(dir_itr->status())) {
       // Find all experiment names and add them
        ProjectHandlerErrorCode err;
        exp_map_.insert(std::pair<std::string, std::shared_ptr<Experiment>>
                  (dir_itr->path().filename().string(), std::make_shared<Experiment> (project_dir_path_, dir_itr->path().filename().string(), err)));

       if (err != ProjectHandlerErrorCode::SUCCESS) {
         err_out = ProjectHandlerErrorCode::IO_ERROR;
         initialized_ = false;
         //throw std::runtime_error("Could not create experiment");
       }
      }
    }
    initialized_ = true;
  } else {
    err_out = ProjectHandlerErrorCode::IO_ERROR;
    initialized_ = false;
  }
}

Project::~Project() {
  std::fclose(meta_file_handler_);
}

/*
 * This function creates a new experiment object and inserts it in the
 * projects experiment_map.
 */

ProjectHandlerErrorCode Project::add_experiment(const std::string& name) {

  if (initialized_) {
    if (experiment_exists(name)) {
      return ProjectHandlerErrorCode::NAME_TAKEN;
    }
    // second element in returned pair
    ProjectHandlerErrorCode err;
    exp_map_.insert(std::pair<std::string, std::shared_ptr<Experiment>>
          (name, std::make_shared<Experiment> (project_dir_path_, name, err)));
    return err;
  } else {
    return ProjectHandlerErrorCode::IO_ERROR;
  }
}

std::shared_ptr<Experiment> Project::get_experiment(const std::string& name) {
  if (initialized_) {
    auto itr = exp_map_.find(name);
    if (itr == exp_map_.end()) {
      return nullptr;
    }
    return itr->second;
  } else {
    return nullptr;
  }
}

/*
 * Calls the clear_files of given experiment and erases the experiment from the experiment map.
 * Because every experiment is a shared ptr, removing the reference from the experiment map will
 * result in the object being destructed.
 */

ProjectHandlerErrorCode Project::remove_experiment(const std::string& name) {
  if (exp_map_.count(name) == 0) {
    return ProjectHandlerErrorCode::EXPERIMENT_NOT_FOUND;
  }
  ProjectHandlerErrorCode err = exp_map_.at(name)->clear_files();
  if (err == ProjectHandlerErrorCode::SUCCESS) {
    exp_map_.erase(name);
  }
  return err;
}

ProjectHandlerErrorCode Project::remove_all_experiments() {
  if (initialized_) {
    std::vector<std::string> n_list = list_experiments();

    for (std::string name : n_list) {
      ProjectHandlerErrorCode err = remove_experiment(name);
      if (err != ProjectHandlerErrorCode::SUCCESS) {
        return err;
      }
    }
    return ProjectHandlerErrorCode::SUCCESS;
  } else {
    return ProjectHandlerErrorCode::IO_ERROR;
  }
}

std::vector<std::string> Project::list_experiments() {
  std::vector<std::string> n_list;
  if (initialized_) {
    n_list.reserve(exp_map_.size());
    for (std::pair<std::string, std::shared_ptr<Experiment>> exp_pair : exp_map_) {
      n_list.push_back(exp_pair.first);
    }
  }
  return n_list;
}

ProjectHandlerErrorCode Project::write_metadata(const protobuf::GeneralMsg& msg) {
  if (initialized_) {
    if (::ftruncate(::fileno(meta_file_handler_), 0)) {  // empty file
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    {
      io::FileOutputStream file_output_stream(::fileno(meta_file_handler_));
      io::CodedOutputStream coded_output_stream(&file_output_stream);
      coded_output_stream.WriteVarint32(msg.ByteSize());
      if (!msg.SerializeToCodedStream(&coded_output_stream)) {
        return ProjectHandlerErrorCode::IO_ERROR;
      }
    }
    if (fflush(meta_file_handler_)) {
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    return ProjectHandlerErrorCode::SUCCESS;
  } else {
    return ProjectHandlerErrorCode::IO_ERROR;
  }
}

boost::optional<protobuf::GeneralMsg> Project::read_metadata(ProjectHandlerErrorCode& err_out) {
  if (initialized_) {
    if (fseek(meta_file_handler_, 0, SEEK_SET) != 0) {
      err_out = ProjectHandlerErrorCode::IO_ERROR;
      return boost::none;
    }
    io::CodedInputStream coded_input_stream(meta_file_input_stream_);
    uint32_t msg_size;
    bool result = coded_input_stream.ReadVarint32(&msg_size);
    if (result) {
      io::CodedInputStream::Limit msg_limit = coded_input_stream.PushLimit(msg_size);
      protobuf::GeneralMsg read_msg;

      if (read_msg.ParseFromCodedStream(&coded_input_stream)) {
        coded_input_stream.PopLimit(msg_limit);
        err_out = ProjectHandlerErrorCode::SUCCESS;
        return read_msg;
      } else {
        err_out = ProjectHandlerErrorCode::IO_ERROR;
        return boost::none;
      }
    } else {
      err_out = ProjectHandlerErrorCode::END_OF_FILE;
      return boost::none;
    }
  } else {
    err_out = ProjectHandlerErrorCode::IO_ERROR;
    return boost::none;
  }
}

ProjectHandlerErrorCode Project::rename_experiment(const std::string& old_name, const std::string& new_name) {
  if (initialized_) {
    auto it = exp_map_.find(old_name);
    if (it == exp_map_.end()) {
      return ProjectHandlerErrorCode::EXPERIMENT_NOT_FOUND;
    }
    boost::system::error_code err;
    fs::rename(fs::path(project_dir_path_ + old_name), fs::path(project_dir_path_ + new_name), err);

    if (err == boost::system::errc::success) {
      ProjectHandlerErrorCode ret = it->second->rename_experiment(project_dir_path_, new_name);
      if(ret == ProjectHandlerErrorCode::SUCCESS){
        exp_map_.erase(exp_map_.find(old_name));
        return add_experiment(new_name);
      }else{
        return ret;
      }

    } else {
      return ProjectHandlerErrorCode::IO_ERROR;
    }
  } else {
    return ProjectHandlerErrorCode::IO_ERROR;
  }
}
/*
 * NOT WORKING ATM. Just renames the local variable and don't actually renames the folder.
 * This applies to all functions called by this one aswell.
 */
void Project::rename_project(const std::string& new_name) {
  name_ = new_name;
  for (std::pair<std::string, std::shared_ptr<Experiment>> exp_pair : exp_map_) {
    exp_pair.second->project_renamed(name_);
  }
}

bool Project::experiment_exists(const std::string& name) {
  auto it = exp_map_.find(name);
  return it != exp_map_.end();
}
