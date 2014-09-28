/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2012 Per Öberg, Philipp Koschorrek, 2014  Emil Berg
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

#ifndef SENSORS_IMUSENSOR_H_
#define SENSORS_IMUSENSOR_H_

#include "XSensIMU.h"

#include "AbstractSensor.h"

class ImuSensor : public AbstractSensor {
 public:
  enum class Mode {
    ABSTIME,
    NO_ABSTIME
  };

  ImuSensor(int id, Mode mode, CommunicationServer& comm_server);

  virtual void idle() override;
  virtual void finalize() override;
  virtual bool read_one_data(std::vector<float>* values) override;

 protected:
  virtual bool initialize() override;
  virtual void execute() override;



 private:
  const int output_mode_ = OUTPUTMODE_CALIB | OUTPUTMODE_ORIENT;
  const int output_settings_ = OUTPUTSETTINGS_ORIENTMODE_EULER
      | OUTPUTSETTINGS_TIMESTAMP_SAMPLECNT;

  CMTComm mtcomm_imu_;
  Mode mode_;
  unsigned short num_devices_;
  int read_fail_;
  unsigned char data_[MAXMSGLEN];
  float fdata_[18] = { 0 };
};

#endif  // SENSORS_IMUSENSOR_H_
