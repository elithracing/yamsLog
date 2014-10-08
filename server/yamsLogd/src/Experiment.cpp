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

#include "Experiment.h"

#include <system_error>
#include <fstream>
#include <iostream>

#include <cstdio>
#include <boost/filesystem.hpp>

#include "ProjectHandlerConstants.h"

const std::string DATA_FILE_ENDING  = ".data";
const std::string META_FILE_ENDING  = ".meta";
const std::string TEXT_FILE_ENDIG   = ".txt";

namespace io = google::protobuf::io;
namespace fs = boost::filesystem;

/*
 * This constructor constructs the experiment class. The class contains handles the files(.txt, .data and .meta)
 * corresponding to the project. The experiments files should be placed in a subfolder under the project it belongs to.
 */

Experiment::Experiment(const std::string& project_dir_path, const std::string& name, ProjectHandlerErrorCode& err_out) {
  experiment_dir_path_ = project_dir_path + name + "/";
  name_ = name;
  meta_file_name_ = name_ + META_FILE_ENDING;
  data_file_name_ = name_ + DATA_FILE_ENDING;
  text_file_name_ = name_ + TEXT_FILE_ENDIG;
  boost::system::error_code boost_err;
  fs::create_directory(experiment_dir_path_, boost_err);
  if (boost_err == boost::system::errc::success || boost_err == boost::system::errc::file_exists) {
    /*  Check if files exists.  */
    if (experiment_files_exist()) {
      // If files exist, initialize them(open and create streams for them). To make sure no data will be overwritten, open as read only
      meta_file_handler_ = std::fopen((experiment_dir_path_+ meta_file_name_).c_str(), "r");
      data_file_handler_ = std::fopen((experiment_dir_path_+ data_file_name_).c_str(), "r");
      text_file_ = new std::fstream();
      text_file_->open(experiment_dir_path_ + text_file_name_, std::ios::in);
      open_mode_ = experiment_open_mode::read;
    } else {
      // If files don't exist, create them
      meta_file_handler_ = std::fopen((experiment_dir_path_+ meta_file_name_).c_str(), "w+");
      data_file_handler_ = std::fopen((experiment_dir_path_+ data_file_name_).c_str(), "w+");
      text_file_ = new std::fstream();
      text_file_->open(experiment_dir_path_ + text_file_name_, std::ios::out);
      open_mode_ = experiment_open_mode::write;
    }

    if (meta_file_handler_ == nullptr ||
        data_file_handler_ == nullptr || !text_file_) {
      std::cerr << "Error opening file" << std::endl;
      err_out = ProjectHandlerErrorCode::IO_ERROR;
      open_mode_ = experiment_open_mode::not_initialized;
      // TODO: Make sure everything is destructed to not get memory leaks
      return;
    }
    data_file_input_stream_ = new io::FileInputStream(::fileno(data_file_handler_));
    meta_file_input_stream_ = new io::FileInputStream(::fileno(meta_file_handler_));
    err_out = ProjectHandlerErrorCode::SUCCESS;
  } else {
    // If directory can't be created or opened
    open_mode_ = experiment_open_mode::not_initialized;
    err_out = ProjectHandlerErrorCode::IO_ERROR;
  }
}


Experiment::~Experiment() {
  if (open_mode_ != experiment_open_mode::deleted) {
    delete(data_file_input_stream_);
    delete(meta_file_input_stream_);

    std::fclose(meta_file_handler_);
    std::fclose(data_file_handler_);
    text_file_->close();
    delete(text_file_);
  }
}

/*
 *  By clearing files, you simply just move them to another directory to avid unwanted deletes of data
 *  After clear_files have been called upon, deletion of Experiment should be done.
 *
 *  Notes:
 *  If removed files directory create succeded or it already exists,
 *  try to organise the removed files in corresponding project it
 *  was started in by creating a project folder in removed files folder
 *
 */
ProjectHandlerErrorCode Experiment::clear_files() {


  // do not attempt to clear nonexisting experiments
  if (open_mode_ == experiment_open_mode::not_initialized || open_mode_ == experiment_open_mode::deleted) {
    return ProjectHandlerErrorCode::IO_ERROR;
  }

  boost::system::error_code boost_err;
  //First try to create the default removed files path
  fs::create_directory(DEFAULT_REMOVE_PATH, boost_err);
  if (boost_err == boost::system::errc::success || boost_err == boost::system::errc::file_exists) {

    fs::create_directory(DEFAULT_REMOVE_PATH + get_project_name(), boost_err);
    std::string new_dir = DEFAULT_REMOVE_PATH + get_project_name() + name_ + "/";
    if (fs::exists(new_dir)) {
        return ProjectHandlerErrorCode::NAME_TAKEN;
    }
    fs::create_directory(new_dir, boost_err);
    if(boost_err !=  boost::system::errc::success){
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    delete(data_file_input_stream_);
    delete(meta_file_input_stream_);
    std::fclose(meta_file_handler_);
    std::fclose(data_file_handler_);
    text_file_->close();
    delete(text_file_);
    open_mode_ = experiment_open_mode::deleted;

    boost::system::error_code err;
    fs::rename(experiment_dir_path_, new_dir, err);
    if (err == boost::system::errc::success) {
      return ProjectHandlerErrorCode::SUCCESS;
    }else{
      return ProjectHandlerErrorCode::IO_ERROR;
    }
  } else {
    return ProjectHandlerErrorCode::IO_ERROR;
  }
}

/*
 * Experiment::write_protobuf_data(const protobuf::GeneralMsg& msg)
 *
 * Writes binary(protobuf encoded data) data to the experiment .data file.
 *
 * Notes:
 * Google iostreams are very cheap to create and destroy hence very effective/
 * or maybe only way, to use the following implementation.
 * The streams must be destructed after something is written to them for the data to
 * be written to file hence the code is in local bracets.
 *
 */

ProjectHandlerErrorCode Experiment::write_protobuf_data(const protobuf::GeneralMsg& msg) {
  if (open_mode_ == experiment_open_mode::write) {
    if (!data_file_handler_) {
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    {
      io::FileOutputStream file_output_stream(::fileno(data_file_handler_));
      io::CodedOutputStream coded_output_stream(&file_output_stream);
      coded_output_stream.WriteVarint32(msg.ByteSize());
      if (! msg.SerializeToCodedStream(&coded_output_stream)) {
        return ProjectHandlerErrorCode::IO_ERROR;
      }
    }
    if (fflush(data_file_handler_)) {
      return ProjectHandlerErrorCode::IO_ERROR;
    }

    return ProjectHandlerErrorCode::SUCCESS;
  } else {

    return ProjectHandlerErrorCode::IO_ERROR;
  }
}

ProjectHandlerErrorCode Experiment::write_text_data(const std::string& data) {
  if (open_mode_ == experiment_open_mode::write) {
    if (text_file_ != nullptr) {
      if (! (*text_file_ << data << std::endl)) {
        return ProjectHandlerErrorCode::IO_ERROR;
      }
      return ProjectHandlerErrorCode::SUCCESS;
    } else {
      return ProjectHandlerErrorCode::IO_ERROR;
    }
  } else {
    return ProjectHandlerErrorCode::IO_ERROR;
  }
}

/*
 * Experiment::read_protobuf_data(ProjectHandlerErrorCode& err_out)
 *
 * If it's possible to read the varin32, then there's probably data in the stream.
 * Because it's cheap to create and destroy google io streams, the codedinputstream
 * is declared locally. The varin32 that was previously read contains information about
 * how many bytes the stored binary message contains. You then have to
 * push the streams "start pointer"(limit) to then try to read the binary data until
 * that pointer and then try to parse the data. If the parsed data is valid you have to
 * pop the limit back to it's previous point. After the function goes out of scope
 * the io stream is destructed and when the function is called once again, the data will be
 * at the start of the new io stream.
 */
boost::optional<protobuf::GeneralMsg> Experiment::read_protobuf_data(ProjectHandlerErrorCode& err_out) {

  if (open_mode_ == experiment_open_mode::read) {
    if (!data_file_input_stream_) {
      err_out = ProjectHandlerErrorCode::IO_ERROR;
      return boost::none;
    }

    io::CodedInputStream coded_input_stream(data_file_input_stream_);
    uint32_t msg_size;
    bool result = coded_input_stream.ReadVarint32(&msg_size);
    protobuf::GeneralMsg read_msg;

    if (result) {
      io::CodedInputStream::Limit msg_limit = coded_input_stream.PushLimit(msg_size);
      if (read_msg.ParseFromCodedStream(&coded_input_stream)) {
        coded_input_stream.PopLimit(msg_limit);
      } else {
        err_out = ProjectHandlerErrorCode::IO_ERROR;
        return boost::none;
      }
    } else {
        err_out = ProjectHandlerErrorCode::END_OF_FILE;
        return boost::none;
    }
    err_out = ProjectHandlerErrorCode::SUCCESS;
    return read_msg;
  } else {
    err_out = ProjectHandlerErrorCode::IO_ERROR;
    return boost::none;
  }
}

ProjectHandlerErrorCode Experiment::reset_read_pos() {
  if (fseek(data_file_handler_, 0, SEEK_SET) != 0) {
    return ProjectHandlerErrorCode::IO_ERROR;
  }
  return ProjectHandlerErrorCode::SUCCESS;
}

/*
 * The metadata contains information about what sensors sensordata is stored in the given experiment.
 * The metadata also contains notes about the experiment that could identify the experiment later on.
 *
 * ExperimentMetadataStruct are written instead of GeneralMsg to avoid writing experiment name to file.
 * Writing GeneralMsg could be problematic when e.g. renaming because you then have to edit the binary data
 * that the medatada contains to the new name.
 */
ProjectHandlerErrorCode Experiment::write_metadata(const std::vector<std::string>& tags, const boost::optional<std::string>& notes) {
  if (fseek(meta_file_handler_, 0, SEEK_SET) != 0) {
    std::cout << "Could not fseek file" << std::endl;
    return ProjectHandlerErrorCode::IO_ERROR;
  } else {
    if (!meta_file_handler_) {
      std::cout << "!!meta_file_handler" << std::endl;
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    protobuf::ExperimentMetadataMsg::MetadataStruct read_msg;
    protobuf::ExperimentMetadataMsg::MetadataStruct new_msg;
    io::CodedInputStream coded_input_stream(meta_file_input_stream_);
    uint32_t msg_size;
    bool read_result = coded_input_stream.ReadVarint32(&msg_size);
    if (read_result) {
      // If there's old metadata, read it and copy sensor_configurations only, discard other old metadata information.
      io::CodedInputStream::Limit msg_limit = coded_input_stream.PushLimit(msg_size);
      if (read_msg.ParseFromCodedStream(&coded_input_stream)) {
        coded_input_stream.PopLimit(msg_limit);
        // Copy sensor configurations
        google::protobuf::RepeatedPtrField< ::protobuf::SensorConfiguration > sensor_configurations = read_msg.sensor_configurations();
        // If there were to be no sensor_configurations absent. The RepeatedField should be empty thus not adding any configurations.
        for (auto&  sensor_config : sensor_configurations ) {
          protobuf::SensorConfiguration* sensor_configuration =
              new_msg.add_sensor_configurations();
          sensor_configuration->CopyFrom(sensor_config);
        }
      }
    }

    for (auto& tag : tags) {
      new_msg.add_tags(tag);
    }
    if (notes) {
      new_msg.set_notes(notes.get());
    }

    if (!::ftruncate(::fileno(meta_file_handler_), 0)) {  // empty file
      std::cout << "Could not empty file" << std::endl;
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    bool write_result;
    {
      io::FileOutputStream file_output_stream(::fileno(meta_file_handler_));
      io::CodedOutputStream coded_output_stream(&file_output_stream);
      coded_output_stream.WriteVarint32(new_msg.ByteSize());
      write_result = new_msg.SerializeToCodedStream(&coded_output_stream);
    }
    if (fflush(meta_file_handler_)) {
      std::cout << "Could not flush file" << std::endl;
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    if (write_result) {
      return ProjectHandlerErrorCode::SUCCESS;
    } else {
      std::cout << "No write result" << std::endl;
      return ProjectHandlerErrorCode::IO_ERROR;
    }
  }
}

/*
 * The metadata should contain data of which sensors were present during
 * a given data collection. This is so that the client should know which
 * sensors the user could choose to get before a playback of the collection.
 *
 * The function simply updates the MetadataStruct in the metadata message
 * in the metadata file(.meta)
 */
ProjectHandlerErrorCode Experiment::write_sensor_configuration_metadata(const std::vector<boost::shared_ptr<AbstractSensor>>& sensors) {
  if (fseek(meta_file_handler_, 0, SEEK_SET) != 0) {
    return ProjectHandlerErrorCode::IO_ERROR;
  } else {
    if (!meta_file_handler_) {
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    protobuf::ExperimentMetadataMsg::MetadataStruct read_msg;
    protobuf::ExperimentMetadataMsg::MetadataStruct new_msg;
    io::CodedInputStream coded_input_stream(meta_file_input_stream_);
    uint32_t msg_size;
    bool read_result = coded_input_stream.ReadVarint32(&msg_size);
    if (read_result) {
      //If there's old metadata, read it and copy meta_daata only, discard other old sensor_configuration information.
      io::CodedInputStream::Limit msg_limit = coded_input_stream.PushLimit(msg_size);
      if (read_msg.ParseFromCodedStream(&coded_input_stream)) {
        coded_input_stream.PopLimit(msg_limit);
        google::protobuf::RepeatedPtrField< ::std::string> tags = read_msg.tags();
        for (auto& tag : tags) {
          new_msg.add_tags(tag);
        }
        if (read_msg.has_notes()) {
          new_msg.set_notes(read_msg.notes());
        }
      }
    }

    for (auto& sensor : sensors) {
      protobuf::SensorConfiguration* sensor_configuration =
          new_msg.add_sensor_configurations();
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
    if (::ftruncate(::fileno(meta_file_handler_), 0)) {  // empty file
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    bool write_result;
    {
      io::FileOutputStream file_output_stream(::fileno(meta_file_handler_));
      io::CodedOutputStream coded_output_stream(&file_output_stream);
      coded_output_stream.WriteVarint32(new_msg.ByteSize());
      write_result = new_msg.SerializeToCodedStream(&coded_output_stream);
    }
    if (fflush(meta_file_handler_)) {
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    if (write_result) {
      return ProjectHandlerErrorCode::SUCCESS;
    } else {
      return ProjectHandlerErrorCode::IO_ERROR;
    }
  }
  return ProjectHandlerErrorCode::SUCCESS;
}

/*
 * If, for some reason, you would like to be on the safe side and write all, for the time of writing,
 * known sensors to the metadata struct, this is the function to be called.
 *
 */
ProjectHandlerErrorCode Experiment::write_all_sensor_configurations_to_metadata() {
  if (fseek(meta_file_handler_, 0, SEEK_SET) != 0) {
    return ProjectHandlerErrorCode::IO_ERROR;
  } else {
    if (!meta_file_handler_) {
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    protobuf::ExperimentMetadataMsg::MetadataStruct read_msg;
    protobuf::ExperimentMetadataMsg::MetadataStruct new_msg;
    io::CodedInputStream coded_input_stream(meta_file_input_stream_);
    uint32_t msg_size;
    bool read_result = coded_input_stream.ReadVarint32(&msg_size);
    if (read_result) {
      //If there's old metadata, read it and copy meta_daata only, discard other old sensor_configuration information.
      io::CodedInputStream::Limit msg_limit = coded_input_stream.PushLimit(msg_size);
      if (read_msg.ParseFromCodedStream(&coded_input_stream)) {
        coded_input_stream.PopLimit(msg_limit);
        google::protobuf::RepeatedPtrField< ::std::string> tags = read_msg.tags();
        for (auto& tag : tags) {
          new_msg.add_tags(tag);
        }
        if (read_msg.has_notes()) {
          new_msg.set_notes(read_msg.notes());
        }
      }
    }
    {
      protobuf::SensorConfiguration* sensor_configuration =
         new_msg.add_sensor_configurations();
      sensor_configuration->set_sensor_id(0);
      sensor_configuration->set_name("imucam");
      std::vector<std::string> v = {"accX","accY","accZ","gyrX","gyrY","gyrZ","magX","magY","magZ","roll","pitch","yaw"};
      sensor_configuration->set_max_attributes(v.size());
      int i = 0;
      for (auto& s : v) {
        protobuf::AttributeConfiguration* attribute_configuration =
         sensor_configuration->add_attribute_configurations();
        attribute_configuration->set_index(i);
        attribute_configuration->set_name(s);
        i++;
      }
    }
    {
      protobuf::SensorConfiguration* sensor_configuration =
         new_msg.add_sensor_configurations();
      sensor_configuration->set_sensor_id(1);
      sensor_configuration->set_name("imucar");
      std::vector<std::string> v = {"accX","accY","accZ","gyrX","gyrY","gyrZ","magX","magY","magZ","roll","pitch","yaw"};
      sensor_configuration->set_max_attributes(v.size());
      int i = 0;
      for (auto& s : v) {
        protobuf::AttributeConfiguration* attribute_configuration =
         sensor_configuration->add_attribute_configurations();
        attribute_configuration->set_index(i);
        attribute_configuration->set_name(s);
        i++;
      }
    }
    {
      protobuf::SensorConfiguration* sensor_configuration =
         new_msg.add_sensor_configurations();
      sensor_configuration->set_sensor_id(2);
      sensor_configuration->set_name("carcan1");
      std::vector<std::string> v = {"CAN_ID", "CAN_message_length"};
      sensor_configuration->set_max_attributes(v.size());
      int i = 0;
      for (auto& s : v) {
        protobuf::AttributeConfiguration* attribute_configuration =
         sensor_configuration->add_attribute_configurations();
        attribute_configuration->set_index(i);
        attribute_configuration->set_name(s);
        i++;
      }
    }
    {
      protobuf::SensorConfiguration* sensor_configuration =
       new_msg.add_sensor_configurations();
      sensor_configuration->set_sensor_id(3);
      sensor_configuration->set_name("can");
      std::vector<std::string> v = {"CAN_ID", "CAN_message_length"};
      sensor_configuration->set_max_attributes(v.size());
      int i = 0;
      for (auto& s : v) {
        protobuf::AttributeConfiguration* attribute_configuration =
         sensor_configuration->add_attribute_configurations();
        attribute_configuration->set_index(i);
        attribute_configuration->set_name(s);
        i++;
      }
    }
    {
      protobuf::SensorConfiguration* sensor_configuration =
       new_msg.add_sensor_configurations();
      sensor_configuration->set_sensor_id(4);
      sensor_configuration->set_name("gps");
      std::vector<std::string> v = {"UT-Time", "Lat", "Lon", "Speed(m/s)", "Course/Heading"};
      sensor_configuration->set_max_attributes(v.size());
      int i = 0;
      for (auto& s : v) {
        protobuf::AttributeConfiguration* attribute_configuration =
         sensor_configuration->add_attribute_configurations();
        attribute_configuration->set_index(i);
        attribute_configuration->set_name(s);
        i++;
      }
    }
    {
      protobuf::SensorConfiguration* sensor_configuration =
       new_msg.add_sensor_configurations();
      sensor_configuration->set_sensor_id(30);
      sensor_configuration->set_name("corrsys");
      std::vector<std::string> v = {"abstime","starttime","h1","h2","h3","hc1","hc2","hc3","pitch","roll",
          "roll2","pitchrate","rollrate","roll2rate","vabs","vlat","vtrans","slipangle"};
      sensor_configuration->set_max_attributes(v.size());
      int i = 0;
      for (auto& s : v) {
        protobuf::AttributeConfiguration* attribute_configuration =
         sensor_configuration->add_attribute_configurations();
        attribute_configuration->set_index(i);
        attribute_configuration->set_name(s);
        i++;
      }
    }
    if (::ftruncate(::fileno(meta_file_handler_), 0)) {  // empty file
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    bool write_result;
    {
      io::FileOutputStream file_output_stream(::fileno(meta_file_handler_));
      io::CodedOutputStream coded_output_stream(&file_output_stream);
      coded_output_stream.WriteVarint32(new_msg.ByteSize());
      write_result = new_msg.SerializeToCodedStream(&coded_output_stream);
    }
    if (fflush(meta_file_handler_)) {
      return ProjectHandlerErrorCode::IO_ERROR;
    }
    if (write_result) {
      return ProjectHandlerErrorCode::SUCCESS;
    } else {
      return ProjectHandlerErrorCode::IO_ERROR;
    }
  }
  return ProjectHandlerErrorCode::SUCCESS;
}


boost::optional<protobuf::ExperimentMetadataMsg::MetadataStruct> Experiment::read_metadata(ProjectHandlerErrorCode& err_out) {
  if (!meta_file_input_stream_) {
    err_out = ProjectHandlerErrorCode::IO_ERROR;
    return boost::none;
  }

  if (fseek(meta_file_handler_, 0, SEEK_SET) != 0) {  // Set to start of file
    err_out = ProjectHandlerErrorCode::IO_ERROR;
    return boost::none;
  }
  io::CodedInputStream coded_input_stream(meta_file_input_stream_);
  uint32_t msg_size;
  bool result = coded_input_stream.ReadVarint32(&msg_size); //If false, => end of file
  protobuf::ExperimentMetadataMsg::MetadataStruct read_msg;
  if (result) {
    io::CodedInputStream::Limit msg_limit = coded_input_stream.PushLimit(msg_size);
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
}

bool Experiment::set_to_read_mode() {
  switch (open_mode_) {
    case experiment_open_mode::read: {
      return true;
      break;
    }
    case experiment_open_mode::write: {
      open_mode_ = experiment_open_mode::read;
      text_file_->close();
      text_file_->open(experiment_dir_path_ + text_file_name_, std::ios_base::in);
      return true;
      break;
    }
    default: {
      return false;
      break;
    }
  }
  return false;
}


/*
 * Experiment::rename_experiment(const std::string& project_dir_path, const std::string& name)
 *
 * By renaming an experiment, you simply close the open files, delete FILE pointer and delete open file streams.
 * Then you rename all experiment files, check if the renaming did good and opens the renamed files up for future
 * usage.
 *
 * ATTENTION!: This function assumes there is no active data collectoin going on. Renaming when in a data collection
 * could lead to unwanted behavior even though the function is called by a thread safe function and the
 * rename experiment function is thread safe.
 * So ensure the function is called after a check of the status of the server is not DATA_COLLECTION
 */

ProjectHandlerErrorCode Experiment::rename_experiment(const std::string& project_dir_path, const std::string& name) {
  if (open_mode_ != experiment_open_mode::not_initialized && open_mode_ != experiment_open_mode::deleted) {
    experiment_dir_path_ = project_dir_path + name + "/";
    name_ = name;
    std::string old_meta_file_name = meta_file_name_;
    std::string old_data_file_name = data_file_name_;
    std::string old_text_file_name = text_file_name_;
    meta_file_name_ = name_ + META_FILE_ENDING;
    data_file_name_ = name_ + DATA_FILE_ENDING;
    text_file_name_ = name_ + TEXT_FILE_ENDIG;
    fclose(meta_file_handler_);
    fclose(data_file_handler_);
    text_file_->close();
    delete(text_file_);
    int result_meta = rename((experiment_dir_path_+ old_meta_file_name).c_str(),(experiment_dir_path_+ meta_file_name_).c_str());
    int result_data = rename((experiment_dir_path_+ old_data_file_name).c_str(),(experiment_dir_path_+ data_file_name_).c_str());
    int result_text = rename((experiment_dir_path_+ old_text_file_name).c_str(),(experiment_dir_path_+  text_file_name_).c_str());

    if ((result_meta == 0 && result_data == 0) && result_text == 0) {
      meta_file_handler_ = std::fopen((experiment_dir_path_+ meta_file_name_).c_str(), "r");
      data_file_handler_ = std::fopen((experiment_dir_path_+ data_file_name_).c_str(), "r");
      text_file_ = new std::fstream(experiment_dir_path_+  text_file_name_, std::ios::in);
      return ProjectHandlerErrorCode::SUCCESS;
    } else {
      return ProjectHandlerErrorCode::IO_ERROR;
    }
  } else {
    return ProjectHandlerErrorCode::IO_ERROR;
  }
}

/*
 * NOT WORKING ATM:
 */
void Experiment::project_renamed(const std::string& project_dir_path) {
  if (open_mode_ != experiment_open_mode::not_initialized && open_mode_ != experiment_open_mode::deleted ) {
    experiment_dir_path_ = project_dir_path + name_ + "/";
  }
}

/*
 * Following functions are class private.
 */

bool Experiment::experiment_files_exist(){
return (fs::exists(fs::path(experiment_dir_path_ + meta_file_name_)) &&
    (fs::exists(fs::path(experiment_dir_path_ + data_file_name_)) &&
        fs::exists(fs::path(experiment_dir_path_ + text_file_name_))));
}

std::string Experiment::get_project_name(){
  std::string str = experiment_dir_path_;
  str.erase(str.find(name_),name_.length()+1).erase(0,DEFAULT_PROJECT_PATH.length());
  return str;
}
