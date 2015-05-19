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

#ifndef SERVICES_ABSTRACTSERVICE_H_
#define SERVICES_ABSTRACTSERVICE_H_

#include <boost/utility.hpp>

#include <sys/time.h>

/**
 * Parent class of all services. Services are responsible for services running parallell to Sensor::execute.
**/
class AbstractService : private boost::noncopyable {
 public:
  AbstractService();
  virtual ~AbstractService();

  void init();
  void set_running(bool running);
  virtual void run();

  // Run once, or never if initialize() returned false.
  virtual void finalize() = 0;


  bool initialized_correctly();
  bool is_running();

  static void generate_time_stamp();

 protected:
  static timespec get_time_stamp();
  static double get_time_diff_us(timeval* ts_us);
  static double get_time_diff();

  // Run once in the initialization phase of the service. Returns true if the initialization succeeded
  virtual bool initialize() = 0;

  // Run an arbitrary amount of times (when the service is started). Never run if initilaize returned false.
  virtual void execute() = 0;

  //If a service wasn't properly initiated, is_running_ will be set to false.
  volatile bool is_running_;

 private:
  static timespec reference_time_stamp_;
  bool initialized_correctly_;
};

#endif  // SERVICES_ABSTRACTSERVICE_H_
