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

#ifndef SENSORS_CANSENSOR_H_
#define SENSORS_CANSENSOR_H_

#include "AbstractSensor.h"
#include "CAN.h"

class CanSensor : public AbstractSensor {
 public:
  explicit CanSensor(int id, CommunicationServer& comm_server);

  virtual void idle() override;
  virtual void finalize() override;
  virtual bool read_one_data(std::vector<float>* values) override;

 protected:
  CanSensor(int id, int max_attributes, CommunicationServer& comm_server);

  virtual bool initialize() override;
  virtual void execute() override;



  bool read_can_struct();
  //bool try_read_can_struct();


  int can_sock_;
  int false_counter_ = 0;
  timeval ts_ = { 0, 0 };
  struct can_frame can_frame_;
  mutable std::mutex mutex_;
  mutable std::mutex local_mutex_;
};

#endif  // SENSORS_CANSENSOR_H_
