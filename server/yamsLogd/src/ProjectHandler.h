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

#ifndef PROJECTHANDLER_H_
#define PROJECTHANDLER_H_

#include <string>
#include <mutex>
#include <unordered_map>

#include <boost/filesystem.hpp>
#include <boost/optional.hpp>
#include <boost/signals2/signal.hpp>

#include "ProjectHandlerErrorCodes.h"
#include "sensors/AbstractSensor.h"
//#include "CommunicationServer.h"
#include "Project.h"

class ProjectHandler {
 public:
  class ModificationCallbackInterface {
   public:
    virtual void active_project_modified_callback() = 0;
    virtual void active_project_metadata_modified_callback() = 0;
    virtual void project_list_modified_callback() = 0;
    virtual void active_project_experiments_metadata_modified_callback() = 0;
    virtual void active_project_experiment_list_modified_callback() = 0;
  };

  explicit ProjectHandler(const std::string& dir_path);
  virtual ~ProjectHandler();

  ProjectHandlerErrorCode set_active_project(const std::string& name);
  boost::optional<std::string> get_active_project();
  ProjectHandlerErrorCode set_active_experiment(const std::string& name);
  void set_no_active_experiment();

  ProjectHandlerErrorCode write_dynamic_event(const protobuf::GeneralMsg& msg) const;
  ProjectHandlerErrorCode write_text_data(const std::string& data) const;
  ProjectHandlerErrorCode write_dynamic_event_text(std::string dynamic_event_text, double time) const;
  ProjectHandlerErrorCode write_project_metadata(const protobuf::GeneralMsg& msg) const;
  ProjectHandlerErrorCode write_experiment_metadata(const std::string& experiment_name, const std::vector<std::string>& tags, const boost::optional<std::string>& notes) const;

  boost::optional<protobuf::GeneralMsg> read_project_metadata(ProjectHandlerErrorCode& err_out);
  std::vector<protobuf::GeneralMsg> read_active_project_experiments_metadata(ProjectHandlerErrorCode& err_out);

  ProjectHandlerErrorCode write_protobuf_data(const protobuf::GeneralMsg& msg) const;

  ProjectHandlerErrorCode reset_read_pos();
  boost::optional<protobuf::GeneralMsg> read_next_data(ProjectHandlerErrorCode& err_out);

  ProjectHandlerErrorCode remove_experiment(const std::string& name);
  ProjectHandlerErrorCode create_experiment(const std::string& name, const std::vector<boost::shared_ptr<AbstractSensor>>& sensors);
  ProjectHandlerErrorCode rename_experiment(const std::string& old_name, const std::string& new_name);

  /* Removes everything in project. If it fails it removes what it can.
   * Is made unaccessible (removed from project_map_) on success only */
  ProjectHandlerErrorCode remove_project(const std::string& name);
  ProjectHandlerErrorCode create_project(const std::string& name);
  ProjectHandlerErrorCode rename_project(const std::string& old_name, const std::string& new_name);
  std::vector<std::string> get_all_project_names();
  std::vector<std::string> get_project_experiment_names(ProjectHandlerErrorCode& err_out);
  ProjectHandlerErrorCode experiment_data_collection_stop();

  void add_callback_interface(ModificationCallbackInterface* callback_interface);
  std::string get_active_experiment();

 private:
  bool active_proj() const;
  bool active_exp() const;
  std::string dir_path_;
  std::string active_project_;
  std::string active_exp_;
  std::unordered_map<std::string, std::shared_ptr<Project>> project_map_;
  mutable std::mutex mutex_;

  boost::signals2::signal<void()> active_project_modified_signal_;
  boost::signals2::signal<void()> active_project_metadata_modified_signal_;
  boost::signals2::signal<void()> project_list_modified_signal_;
  boost::signals2::signal<void()> active_project_experiments_metadata_modified_signal_;
  boost::signals2::signal<void()> active_project_experiment_list_modified_signal_;
};

#endif  // PROJECTHANDLER_H_
