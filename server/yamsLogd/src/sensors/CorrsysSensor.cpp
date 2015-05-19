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

#if !defined(__CYGWIN__)

#include "CorrsysSensor.h"
#include <iostream>

const int MAX_ATTRIBUTES = 18;

CorrsysSensor::CorrsysSensor(int id, CommunicationServer& comm_server)
    : CanSensor(id, MAX_ATTRIBUTES, comm_server) {
  vlat_ = vtrans_ = vabs_ = h1_ = h2_ = h3_ = hc1_ = hc2_ = hc3_ = pitch_ = roll_ = roll2_ = pitchrate_ = rollrate_ = roll2rate_ = slipangle_ = starttime_ = 0;
  heights_read = hcs_read_ = pitchs_and_rolls_read_ = rates_read_ = vabs_read_ = vlats_and_trans_read = false;
}


void CorrsysSensor::execute() {
  double time = get_time_diff();
  std::vector<float> values;
  if(read_one_data(&values))
    if(values.size() != static_cast<size_t>(MAX_ATTRIBUTES)){
      for(int i = values.size(); i < MAX_ATTRIBUTES; i++){
        values.push_back(0);
      }
    }
    add_to_fifos(create_data_message(time,&values,true),create_data_message(time, &values,false), create_text_data_message(time,&values));
}

bool CorrsysSensor::read_one_data(std::vector<float>* values){
  /* Get timestamp of recieved message */
  bool data_read = false;
  while (!data_read){
    ioctl(can_sock_, SIOCGSTAMP, &ts_);
    double time = get_time_diff_us(&ts_);

    if (read_can_struct()) {
      false_counter_ = 0;
      switch ((can_frame_.can_id & CAN_EFF_MASK)) {
        case 768:
          starttime_ = time;
          h1_ = (static_cast<float>(can_frame_.data[1]) * 256 + static_cast<float>(can_frame_.data[0]))
              * 0.01;
          h2_ = (static_cast<float>(can_frame_.data[3]) * 256 + static_cast<float>(can_frame_.data[2]))
              * 0.01;
          h3_ = (static_cast<float>(can_frame_.data[5]) * 256 + static_cast<float>(can_frame_.data[4]))
              * 0.01;
          //data_read = false;
          heights_read = true;
          break;
        case 769:
          if (can_frame_.data[1] >= 128)  // 2-complement --> negative
            hc1_ = (0.02)
                * ((static_cast<float>(can_frame_.data[1]) - 256) * 256
                    + static_cast<float>(can_frame_.data[0]));
          else
            // positive
            hc1_ = (0.02)
                * (static_cast<float>(can_frame_.data[1]) * 256 + static_cast<float>(can_frame_.data[0]));
          if (can_frame_.data[3] >= 128)  // 2-complement --> negative
            hc2_ = (0.02)
                * ((static_cast<float>(can_frame_.data[3]) - 256) * 256
                    + static_cast<float>(can_frame_.data[2]));
          else
            // positive
            hc2_ = (0.02)
                * (static_cast<float>(can_frame_.data[3]) * 256 + static_cast<float>(can_frame_.data[2]));
          if (can_frame_.data[5] >= 128)  // 2-complement --> negative
            hc3_ = (0.02)
                * ((static_cast<float>(can_frame_.data[5]) - 256) * 256
                    + static_cast<float>(can_frame_.data[4]));
          else
            // positive
            hc3_ = (0.02)
                * (static_cast<float>(can_frame_.data[5]) * 256 + static_cast<float>(can_frame_.data[4]));
          //data_read = true;
          hcs_read_ = true;
          break;
        case 770:
          if (can_frame_.data[1] >= 128)  // 2-complement --> negative
            pitch_ = (0.001)
                * ((static_cast<float>(can_frame_.data[1]) - 256) * 256
                    + static_cast<float>(can_frame_.data[0]));
          else
            // positive
            pitch_ = (0.001)
                * (static_cast<float>(can_frame_.data[1]) * 256 + static_cast<float>(can_frame_.data[0]));
          if (can_frame_.data[3] >= 128)  // 2-complement --> negative
            roll_ = (0.001)
                * ((static_cast<float>(can_frame_.data[3]) - 256) * 256
                    + static_cast<float>(can_frame_.data[2]));
          else
            // positive
            roll_ = (0.001)
                * (static_cast<float>(can_frame_.data[3]) * 256 + static_cast<float>(can_frame_.data[2]));
          if (can_frame_.data[5] >= 128)  // 2-complement --> negative
            roll2_ = (0.001)
                * ((static_cast<float>(can_frame_.data[5]) - 256) * 256
                    + static_cast<float>(can_frame_.data[4]));
          else
            // positive
            roll2_ = (0.001)
                * (static_cast<float>(can_frame_.data[5]) * 256 + static_cast<float>(can_frame_.data[4]));
          pitchs_and_rolls_read_ = true;
          break;
        case 771:
          if (can_frame_.data[1] >= 128)  // 2-complement --> negative
            pitchrate_ = (0.01)
                * ((static_cast<float>(can_frame_.data[1]) - 256) * 256
                    + static_cast<float>(can_frame_.data[0]));
          else
            // positive
            pitchrate_ = (0.01)
                * (static_cast<float>(can_frame_.data[1]) * 256 + static_cast<float>(can_frame_.data[0]));
          if (can_frame_.data[3] >= 128)  // 2-complement --> negative
            rollrate_ = (0.01)
                * ((static_cast<float>(can_frame_.data[3]) - 256) * 256
                    + static_cast<float>(can_frame_.data[2]));
          else
            // positive
            rollrate_ = (0.01)
                * (static_cast<float>(can_frame_.data[3]) * 256 + static_cast<float>(can_frame_.data[2]));
          if (can_frame_.data[5] >= 128)  // 2-complement --> negative
            roll2rate_ = (0.01)
                * ((static_cast<float>(can_frame_.data[5]) - 256) * 256
                    + static_cast<float>(can_frame_.data[4]));
          else
            // positive
            roll2rate_ = (0.01)
                * (static_cast<float>(can_frame_.data[5]) * 256 + static_cast<float>(can_frame_.data[4]));
          //data_read = true;
          rates_read_= true;
          break;
        case 2042:
          vabs_ = (static_cast<float>(can_frame_.data[3]) * 256 + static_cast<float>(can_frame_.data[2]))
              * 0.01;
          vabs_read_ = true;
          //data_read = true;
          break;
        case 2043: {
          vlat_ = (static_cast<float>(can_frame_.data[1]) * 256 + static_cast<float>(can_frame_.data[0]))
              * 0.01;
          if (can_frame_.data[3] >= 128)  // 2-complement --> negative
            vtrans_ = (0.01)
                * ((static_cast<float>(can_frame_.data[3]) - 256) * 256
                    + static_cast<float>(can_frame_.data[2]));
          else
            // positive
            vtrans_ = (0.01)
                * (static_cast<float>(can_frame_.data[3]) * 256 + static_cast<float>(can_frame_.data[2]));
          if (can_frame_.data[5] >= 128)  // 2-complement --> negative
            slipangle_ = (0.01)
                * ((static_cast<float>(can_frame_.data[5]) - 256) * 256
                    + static_cast<float>(can_frame_.data[4]));
          else
            // positive
            slipangle_ = (0.01)
                * (static_cast<float>(can_frame_.data[5]) * 256 + static_cast<float>(can_frame_.data[4]));
          // all data recorded. save them!!
          // clock_gettime(CLOCK_REALTIME, &ts);
          // abstime = ts.tv_sec + (ts.tv_nsec)/1000000000.0;
          double abstime = time + get_time_stamp().tv_sec
              + get_time_stamp().tv_nsec / 1000000000.0;
          vlats_and_trans_read = true;
          if(heights_read&& hcs_read_&& pitchs_and_rolls_read_&& rates_read_&& vabs_read_&&vlats_and_trans_read){
            //TODO make better check
            heights_read = hcs_read_ = pitchs_and_rolls_read_ = rates_read_ = vabs_read_ = vlats_and_trans_read = false;
            values->push_back(abstime);
            values->push_back(starttime_);
            values->push_back(h1_);
            values->push_back(h2_);
            values->push_back(h3_);
            values->push_back(hc1_);
            values->push_back(hc2_);
            values->push_back(hc3_);
            values->push_back(pitch_);
            values->push_back(roll_);
            values->push_back(roll2_);
            values->push_back(pitchrate_);
            values->push_back(rollrate_);
            values->push_back(roll2rate_);
            values->push_back(vabs_);
            values->push_back(vlat_);
            values->push_back(vtrans_);
            values->push_back(slipangle_);

            vlat_ = vtrans_ = vabs_ = h1_ = h2_ = h3_ = hc1_ = hc2_ = hc3_ = pitch_ = roll_ = roll2_ = pitchrate_ = rollrate_ = roll2rate_ = slipangle_ = 0;
            starttime_ = 0;
            data_read = true;
          }
          break;
        }
        case 2045:
          break;
        default:
          break;
      }
    }else{
      false_counter_++;
      if(false_counter_ > 20){
        return false;
      }
    }
  }
    return data_read;
}

#endif /* !_CYGWIN__ */

