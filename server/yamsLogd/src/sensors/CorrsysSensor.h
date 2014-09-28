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

#ifndef SENSORS_CORRSYSSENSOR_H_
#define SENSORS_CORRSYSSENSOR_H_

#include "CanSensor.h"

// Inherit from CanSensor to minimize code duplication
class CorrsysSensor : public CanSensor {
 public:
  explicit CorrsysSensor(int id, CommunicationServer& comm_server);

 protected:
  virtual void execute() override;
  virtual bool read_one_data(std::vector<float>* values) override;


 private:
  float vlat_, vtrans_, vabs_, h1_, h2_, h3_, hc1_, hc2_, hc3_, pitch_, roll_, roll2_, pitchrate_, rollrate_, roll2rate_, slipangle_;
  double starttime_;
  bool heights_read, hcs_read_, pitchs_and_rolls_read_, rates_read_, vabs_read_,vlats_and_trans_read;
};

#endif  // SENSORS_CORRSYSSENSOR_H_
