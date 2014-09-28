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

#ifndef SENSORS_GPSSENSOR_H_
#define SENSORS_GPSSENSOR_H_

// Ignore some warnings
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma GCC diagnostic ignored "-Wshadow"
#include "SerialPortLib.h"
#pragma GCC diagnostic pop
#pragma GCC diagnostic pop

#include "AbstractSensor.h"

class GpsSensor : public AbstractSensor {
 public:
  explicit GpsSensor(int id, CommunicationServer& comm_server);

  virtual void idle() override;
  virtual void finalize() override;
  virtual bool read_one_data(std::vector<float>* values) override;

 protected:
  virtual bool initialize() override;
  virtual void execute() override;


 private:
  TimeoutSerial* gps_receiver_;
  mutable std::mutex mutex_;
};

#endif  // SENSORS_GPSSENSOR_H_
