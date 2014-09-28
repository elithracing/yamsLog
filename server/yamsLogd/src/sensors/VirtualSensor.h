/**
 * CombinedDataCollectorV2 is a program for collecting and 
 * distributing sensor data.
 * Copyright (C) 2014  Per Öberg, Erik Frisk
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

#ifndef SENSORS_VIRTUALSENSOR_H_
#define SENSORS_VIRTUALSENSOR_H_

#include "AbstractSensor.h"

class VirtualSensor : public AbstractSensor {
 public:
  enum class Mode {
    ABSTIME,
    NO_ABSTIME
  };

  VirtualSensor(int id, Mode mode, CommunicationServer& comm_server);

  virtual void idle() override;
  virtual void finalize() override;
  virtual bool read_one_data(std::vector<float>* values) override;

 protected:
  virtual bool initialize() override;
  virtual void execute() override;

 private:
  double sampleTime;
  Mode mode_;
  double currentTime;
};

#endif  // SENSORS_VIRTUALSENSOR_H_
