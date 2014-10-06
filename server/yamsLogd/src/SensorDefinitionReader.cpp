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

#include "SensorDefinitionReader.h"

#include <iostream>
#include <fstream>
#include <sstream>
#include <unordered_map>
#include <string>

#include <boost/algorithm/string.hpp>

static const int COLUMNS = 5;

SensorDefinitionReader::SensorDefinitionReader() {
}

SensorDefinitionReader::~SensorDefinitionReader() {
}

bool SensorDefinitionReader::initialize_sensor_attributes(const std::string& file_name, const 
std::vector<boost::shared_ptr<AbstractSensor>>& sensors) {
  std::fstream instream;
  instream.open(file_name, std::ios_base::in);

  if (instream.is_open() == false) {
    return false;
  }
  std::string line;
  std::string lines[COLUMNS];

  while (!instream.eof()) {
    std::getline(instream, line);
    if ((line[0] != '%') && (line[0] != 0)) {  // 0 is the same as '/n' in the file
      delimit_string(line, lines);
      insert_sensor_attributes(sensors, lines);
    }
  }

  return true;
}

void SensorDefinitionReader::delimit_string(const std::string& line, std::string lines[]) {
  int i = 0;
  std::stringstream ssin(line);
  while (ssin.good() && i < COLUMNS) {
    std::getline(ssin, lines[i], ',');
    boost::algorithm::trim(lines[i]);
    ++i;
  }
}

void SensorDefinitionReader::insert_sensor_attributes(const std::vector<boost::shared_ptr<AbstractSensor>>& sensors,
                                      std::string lines[]) {
  std::string attr_name = lines[1];
  std::string min = lines[2];
  std::string max = lines[3];
  std::string attr_index = lines[4];
  AbstractSensor::attr_struct attr_struct;
  attr_struct.has_max_limit = is_number(max);
  attr_struct.has_min_limit = is_number(min);

  for (auto& sensor : sensors) {
    if (lines[0].compare(sensor->get_name()) == 0) {
      attr_struct.attr_name = lines[1];
      attr_struct.attr_index = stoi(lines[4]);

      if(attr_struct.has_max_limit){
        attr_struct.max = stof(lines[3]);
      }else{
        //The max value will not be looked at if it has no min_limit
        attr_struct.max = 0;
      }

      if(attr_struct.has_min_limit){
        attr_struct.min = stof(lines[2]);
      }else{
        //The min value will not be looked at if it has no min_limit
        attr_struct.min = 0;
      }
      attr_struct.status = protobuf::SensorStatusMsg::INSIDE_LIMITS;
      sensor->insert_attributes(attr_struct);
      break;
    }
  }
}

bool SensorDefinitionReader::is_number(const std::string& s){
    auto it = s.begin();
    //Account for negative sign characther
    if(*it == '-')it++;
    //Check all characters in string to see if all are digits
    while (it != s.end() && std::isdigit(*it)) ++it;
    return !s.empty() && it == s.end();
}

