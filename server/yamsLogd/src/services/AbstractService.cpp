/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2014  Emil Berg
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

#include "AbstractService.h"
#include <time.h>

#ifdef __APPLE__
// clock_gettime is not available in OS X
#include <mach/mach.h>
#include <mach/mach_time.h>
#define CLOCK_REALTIME 0

int
clock_gettime(int /*clk_id*/, timespec* t)
{
  static mach_timebase_info_data_t sTimebaseInfo;
  uint64_t absTime = mach_absolute_time();
  
  if ( sTimebaseInfo.denom == 0 ) {
    mach_timebase_info(&sTimebaseInfo);
  }
  
  uint64_t nanoseconds = absTime*sTimebaseInfo.numer/sTimebaseInfo.denom;
  
  t->tv_sec  = nanoseconds / 1e9;
  t->tv_nsec = nanoseconds % 1000000000;
  return 0;
}
#endif


timespec AbstractService::reference_time_stamp_;

AbstractService::AbstractService() : is_running_(true), initialized_correctly_(false) {
}

AbstractService::~AbstractService() {
}

void AbstractService::init() {
  initialized_correctly_ = initialize();
}

void AbstractService::run() {
  while (is_running_) {
    execute();
  }
}

void AbstractService::set_running(bool running) {
  is_running_ = running;
}

bool AbstractService::initialized_correctly() {
  return initialized_correctly_;
}

bool AbstractService::is_running() {
  return is_running_;
}

void AbstractService::generate_time_stamp() {
  clock_gettime(CLOCK_REALTIME, &reference_time_stamp_);
}

timespec AbstractService::get_time_stamp() {
  return reference_time_stamp_;
}

double AbstractService::get_time_diff_us(timeval* ts_us) {
  double time;
  time = ts_us->tv_sec - reference_time_stamp_.tv_sec
      + ts_us->tv_usec / 1000000.0
      - reference_time_stamp_.tv_nsec / 1000000000.0;
  return time;
}

double AbstractService::get_time_diff() {
  double time;
  timespec ts = { 0, 0 };
  clock_gettime(CLOCK_REALTIME, &ts);
  time = ts.tv_sec - reference_time_stamp_.tv_sec
      + (ts.tv_nsec - reference_time_stamp_.tv_nsec) / 1000000000.0;
  return time;
}

