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

/*
 * This is the class that saves binary data(protobuf data) to the experiments given binary-file(.data file).
 *
 */

#include "ProtobufFileOutputService.h"

#include <thread>
#include <chrono>

#include "OutputServiceConstants.h"

ProtobufFileOutputService::ProtobufFileOutputService(
    const std::vector<boost::shared_ptr<AbstractSensor>>& sensors, const ProjectHandler& project_handler, StatusObserver& status_observer)
    : sensors_(sensors), project_handler_(project_handler), status_observer_(status_observer) {
}

bool ProtobufFileOutputService::initialize() {
  return true;
}

void ProtobufFileOutputService::write_to_file(
    const boost::shared_ptr<AbstractSensor>& sensor) const {
  std::unique_ptr<protobuf::GeneralMsg> general_msg(sensor->protobuf_file_fifo_pop());
  if(status_observer_.get_status() == protobuf::StatusMsg::DATA_COLLECTION){
    ProjectHandlerErrorCode err = project_handler_.write_protobuf_data(*general_msg);
    if (err != ProjectHandlerErrorCode::SUCCESS) {
      std::cerr << "ProtobufFileOutputService: Failed to write Protobuf data to file (" << static_cast<int>(err) << ")" << std::endl;
    }
  }
}

void ProtobufFileOutputService::execute() {
  bool wrote = false;
  for (auto& sensor : sensors_) {
    for (int i = 0;
        sensor->protobuf_file_fifo_is_empty() == false
            && i < MAX_READINGS_PER_STEP; i++) {
      wrote = true;
      write_to_file(sensor);
    }
  }
  if (!wrote) {
    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
  }
}

void ProtobufFileOutputService::finalize() {
  // Save all
  for (auto& sensor : sensors_) {
    while (sensor->protobuf_file_fifo_is_empty() == false) {
      write_to_file(sensor);
    }
  }
}
