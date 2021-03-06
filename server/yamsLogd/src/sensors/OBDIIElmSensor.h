/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
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

#ifndef SENSORS_OBDIIELMSENSOR_H_
#define SENSORS_OBDIIELMSENSOR_H_

#include "AbstractSensor.h"
#include "OBDII-ELM327/OBDReader.h"

class OBDIIElmSensor : public AbstractSensor {
 public:
  OBDIIElmSensor(int id, CommunicationServer& comm_server);
  ~OBDIIElmSensor();
  virtual void idle() override;
  virtual void finalize() override;
  virtual bool read_one_data(std::vector<float>* values) override;

  /*  struct loggedPIDS{
    double ;
    };*/

 protected:
  virtual bool initialize() override;
  virtual void execute() override;

 private:
  double sampleTime;
  double currentTime;
  OBDReader* obdreader;
  std::list<OBDReader::measData*> measDataQueue;
};

#endif  // SENSORS_OBDIIELMSENSOR_H_
