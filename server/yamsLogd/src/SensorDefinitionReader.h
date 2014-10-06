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

#ifndef SENSORDEFINITIONREADER_H_
#define SENSORDEFINITIONREADER_H_

#include <vector>
#include <string>

#include <boost/thread.hpp>
#include <boost/bind.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/interprocess/sync/interprocess_semaphore.hpp>
#include <boost/algorithm/string/predicate.hpp>

#include "sensors/AbstractSensor.h"

// This class inititates the range that the reasonable values can be.
class SensorDefinitionReader {
 public:
  SensorDefinitionReader();
  virtual ~SensorDefinitionReader();

  bool initialize_sensor_attributes(const std::string& file_name, const std::vector<boost::shared_ptr<AbstractSensor>>& 
sensors);

 private:
  void delimit_string(const std::string& line, std::string lines[]);
  void insert_sensor_attributes(const std::vector<boost::shared_ptr<AbstractSensor>>& sensors,
                                        std::string lines[]);
  bool is_number(const std::string& s);
};

#endif  // SENSORDEFINITIONREADER_H_
