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

#include <fstream>
#include <boost/algorithm/string.hpp>

#include <muParser.h>

#define SENSOR_DEF_READER_ENABLE_MUPARSER_DUMP 0

static const int COLUMNS = 6;
static const int SENSOR_NAME_COL = 0;
static const int ATTR_NAME_COL = 1;
static const int ATTR_MIN_COL = 2;
static const int ATTR_MAX_COL = 3;
static const int ATTR_IDX_COL = 4;
static const int ATTR_CONV_COL = 5;
static const std::string CONVERSION_VARIABLE = "x";

SensorDefinitionReader::SensorDefinitionReader() {
}

SensorDefinitionReader::~SensorDefinitionReader() {
}

bool SensorDefinitionReader::initialize_sensor_attributes(const std::string& file_name,
                                                          const std::vector
                                                          <boost::shared_ptr<AbstractSensor>>& sensors) {
  std::fstream instream;
  instream.open(file_name, std::ios_base::in);

  if (!instream.is_open()) {
    return false;
  }

  std::string line;
  std::string lines[COLUMNS];

  while (!instream.eof()) {
    std::getline(instream, line);
    boost::algorithm::trim(line);
    if ((line[0] != '%') && (line[0] != '\n')) {
      delimit_string(line, lines);
      insert_sensor_attributes(sensors, lines[SENSOR_NAME_COL], lines[ATTR_NAME_COL], lines[ATTR_MAX_COL],
                               lines[ATTR_MIN_COL], lines[ATTR_IDX_COL], lines[ATTR_CONV_COL]);
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
  for (; i < COLUMNS; ++i) {
    lines[i].clear();
  }
}

void SensorDefinitionReader::insert_sensor_attributes(const std::vector<boost::shared_ptr<AbstractSensor>>& sensors,
                                                      const std::string& sensor_name, const std::string& attr_name,
                                                      const std::string& attr_min, const std::string& attr_max,
                                                      const std::string& attr_index, const std::string& conversion) {
  AbstractSensor::attr_struct attr_struct;
  float dummyVal = 0;
  attr_struct.has_max_limit = is_number(attr_max);
  attr_struct.has_min_limit = is_number(attr_min);
  attr_struct.converter.DefineVar(CONVERSION_VARIABLE, &dummyVal);
  attr_struct.converter.SetExpr(conversion);

  for (auto& sensor : sensors) {
    if (sensor_name.compare(sensor->get_name()) == 0) {
      attr_struct.attr_name = attr_name;
      attr_struct.attr_index = stoi(attr_index);

      if(attr_struct.has_max_limit){
        attr_struct.max = stof(attr_max);
      }else{
        attr_struct.max = 0;
      }

      if(attr_struct.has_min_limit){
        attr_struct.min = stof(attr_min);
      }else{
        attr_struct.min = 0;
      }

      // Check if conversion expression works/exists
      try
      {
#if SENSOR_DEF_READER_ENABLE_MUPARSER_DUMP
        attr_struct.converter.EnableDebugDump(true, true);
#endif
        attr_struct.converter.Eval();
        attr_struct.has_conversion = true;
#ifndef NDEBUG
        std::cout << "Added conversion: """ << attr_struct.converter.GetExpr()
        << """ for attribute: " << attr_name << " in sensor: " << sensor_name << std::endl;
#endif
      }
      catch (mu::Parser::exception_type &e)
      {
        attr_struct.has_conversion = false;
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

