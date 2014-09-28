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

#ifndef PROJECT_H_
#define PROJECT_H_

#include <memory>
#include <string>
#include <iostream>
#include <fstream>
#include <unordered_map>

#include "protocol.pb.h"
#include "sensors/AbstractSensor.h"
#include "Experiment.h"

#include "ProjectHandlerErrorCodes.h"

namespace io = google::protobuf::io;

class Project {
 public:
  Project(const std::string& dir_path, const std::string& name, ProjectHandlerErrorCode& err_out);
  virtual ~Project();

  ProjectHandlerErrorCode add_experiment(const std::string& name);
  std::shared_ptr<Experiment> get_experiment(const std::string& name);
  ProjectHandlerErrorCode remove_experiment(const std::string& name);
  ProjectHandlerErrorCode remove_all_experiments();
  std::vector<std::string> list_experiments();
  ProjectHandlerErrorCode write_metadata(const protobuf::GeneralMsg& msg);
  boost::optional<protobuf::GeneralMsg> read_metadata(ProjectHandlerErrorCode& err_out);
  ProjectHandlerErrorCode rename_experiment(const std::string& old_name, const std::string& new_name);
  void rename_project(const std::string& new_name);
  bool experiment_exists(const std::string& name);

 private:
  std::string project_dir_path_;
  std::string meta_file_name_;
  int project_meta_fd_;
  FILE* meta_file_handler_;
  std::string name_;
  std::unordered_map<std::string, std::shared_ptr<Experiment>> exp_map_;
  bool initialized_;
  io::FileInputStream* meta_file_input_stream_;

};

#endif  // PROJECT_H_
