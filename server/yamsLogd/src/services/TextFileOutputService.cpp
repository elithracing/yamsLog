/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2012 Philipp Koschorrek, 2014  Emil Berg
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
 * This is the class that handles text data saves to the experiments given text file.
 *
 */

#include "TextFileOutputService.h"

#include <thread>
#include <chrono>

#include "OutputServiceConstants.h"

TextFileOutputService::TextFileOutputService(
    const std::vector<boost::shared_ptr<AbstractSensor>>& sensors,
    const ProjectHandler& project_handler,
    StatusObserver& status_observer)
    : sensors_(sensors),
      project_handler_(project_handler),
      status_observer_(status_observer) {
}

bool TextFileOutputService::initialize() {
  return true;
}

void TextFileOutputService::write_msg(
    const boost::shared_ptr<AbstractSensor>& sensor) const {
  std::string data = sensor->text_file_fifo_pop();

  if(status_observer_.get_status() == protobuf::StatusMsg::DATA_COLLECTION){
    ProjectHandlerErrorCode err = project_handler_.write_text_data(data);
    if (err != ProjectHandlerErrorCode::SUCCESS) {
      std::cerr << "TextFileOutputService: Failed to write text data to file (" << static_cast<int>(err) << ")" << std::endl;
    }
  }
}

void TextFileOutputService::execute() {
  bool wrote = false;
  for (auto& sensor : sensors_) {
    for (int i = 0;
        sensor->text_file_fifo_is_empty() == false
            && i < MAX_READINGS_PER_STEP; i++) {
      wrote = true;
      write_msg(sensor);
    }
  }
  if (!wrote) {
    std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
  }
}

void TextFileOutputService::finalize() {
  // Save all
  for (auto& sensor : sensors_) {
    while (sensor->text_file_fifo_is_empty() == false) {
      write_msg(sensor);
    }
  }
}
