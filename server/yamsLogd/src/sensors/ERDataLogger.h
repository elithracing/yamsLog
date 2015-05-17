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

#ifndef SENSORS_ERDATALOGGER_H_
#define SENSORS_ERDATALOGGER_H_
// Ignore some warnings TODO from GPS, remove _if_ not needed
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma GCC diagnostic ignored "-Wshadow"
#include "SerialPortLib.h"
#pragma GCC diagnostic pop // Not needed ??
#pragma GCC diagnostic pop

#include "AbstractSensor.h"

class ERDataLogger : public AbstractSensor {
 public:
  explicit ERDataLogger(int id, CommunicationServer& comm_server);

  virtual void idle() override;
  virtual void finalize() override;
  virtual bool read_one_data(std::vector<float>* values) override;

 protected:
  virtual bool initialize() override;
  virtual void execute() override;

 private:
  TimeoutSerial* er_receiver_;
  mutable std::mutex mutex_;
  double time;  // Needs to be accessible across member functions because of lack of
                // distinction between sensor data and time. This ugliness is preferred
                // over reading data two times.
  bool dummy;
};

#endif  // SENSORS_GPSSENSOR_H_
