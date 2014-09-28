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

#ifndef EXPERIMENT_H_
#define EXPERIMENT_H_

#include <fstream>
#include <cstdio>

#include <boost/optional.hpp>
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl.h>
#include <boost/shared_ptr.hpp>

#include "ProjectHandlerErrorCodes.h"
#include "sensors/AbstractSensor.h"

#include "protocol.pb.h"

namespace io = google::protobuf::io;

class Experiment {
 public:
    enum experiment_open_mode{
      read,write,not_initialized,deleted
    };
    Experiment(const std::string& dir_path, const std::string& name, ProjectHandlerErrorCode& err_out);
    virtual ~Experiment();
    ProjectHandlerErrorCode clear_files();

    /* Interface type 1  */
    ProjectHandlerErrorCode write_protobuf_data(const protobuf::GeneralMsg& msg);
    ProjectHandlerErrorCode write_text_data(const std::string& data);
    boost::optional<protobuf::GeneralMsg> read_protobuf_data(ProjectHandlerErrorCode& err_out);
    ProjectHandlerErrorCode write_metadata(const std::vector<std::string>& tags, const boost::optional<std::string>& notes);
    ProjectHandlerErrorCode write_sensor_configuration_metadata(const std::vector<boost::shared_ptr<AbstractSensor>>& sensors);
    ProjectHandlerErrorCode write_all_sensor_configurations_to_metadata();
    boost::optional<protobuf::ExperimentMetadataMsg::MetadataStruct> read_metadata(ProjectHandlerErrorCode& err_out);
    ProjectHandlerErrorCode reset_read_pos();
    bool set_to_read_mode();
    ProjectHandlerErrorCode rename_experiment(const std::string& project_dir_path, const std::string& name);
    void project_renamed(const std::string& project_name);

 private:
    bool experiment_files_exist();
    std::string get_project_name();
    std::string experiment_dir_path_;
    std::string name_;
    std::string data_file_name_;
    std::string meta_file_name_;
    std::string text_file_name_;
    experiment_open_mode open_mode_;

    FILE* meta_file_handler_;
    FILE* data_file_handler_;
    std::fstream* text_file_;
    io::FileInputStream* data_file_input_stream_;
    io::FileInputStream* meta_file_input_stream_;
};

#endif  // EXPERIMENT_H_
