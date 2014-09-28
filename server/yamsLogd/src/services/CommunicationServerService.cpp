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

#include "CommunicationServerService.h"

#include <thread>
#include <chrono>
#include <iostream>

const int SLEEP_TIME_MS = 500;

CommunicationServerService::CommunicationServerService(
    CommunicationServer& comm_server)
    : comm_server_(comm_server) {
}

bool CommunicationServerService::initialize() {
  // Verify that the version of the protobuf library that we linked against is compatible with the version of the headers.
  GOOGLE_PROTOBUF_VERIFY_VERSION;

  server_thread_ = boost::thread([&]() {comm_server_.start();});

  return true;
}

void CommunicationServerService::execute() {
  // Do nothing except sleep. Everything is handled in initialize, finalize
  std::this_thread::sleep_for(std::chrono::milliseconds(SLEEP_TIME_MS));
}

void CommunicationServerService::finalize() {
  comm_server_.stop();
  server_thread_.join();

  google::protobuf::ShutdownProtobufLibrary();
}
