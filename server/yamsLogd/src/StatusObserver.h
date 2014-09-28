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

#ifndef STATUSOBSERVER_H_
#define STATUSOBSERVER_H_

#include <boost/function.hpp>
#include <boost/signals2/signal.hpp>
#include <boost/utility.hpp>

#include "protocol.pb.h"

/**
 * Holds the global status. Support subscription of status change.
**/
class StatusObserver : private boost::noncopyable {
 public:
  StatusObserver();

  protobuf::StatusMsg::StatusType get_status() const;
  void set_status(protobuf::StatusMsg::StatusType status);
  void subscribe_status_change(boost::function<void(protobuf::StatusMsg::StatusType)> callback);

 private:
  volatile protobuf::StatusMsg::StatusType status_;
  boost::signals2::signal<void(protobuf::StatusMsg::StatusType)> signal_;
};

#endif  // STATUSOBSERVER_H_
