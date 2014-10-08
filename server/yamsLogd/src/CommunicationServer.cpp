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

#include "CommunicationServer.h"
#include "sensors/AbstractSensor.h"

#include <iostream>
#include <thread>

#include <boost/bind.hpp>
#include <boost/thread.hpp>

CommunicationServer::CommunicationServer(
    int port,
    ServerCallbackInterface* server_callback)
    : acceptor_aborted_sema_(0),
      acceptor_(
          io_service_,
          boost::asio::ip::tcp::endpoint(boost::asio::ip::tcp::v6(), port)),
      server_callback_(server_callback) {
}

void CommunicationServer::run_io_service() {
  io_service_.run();
}

void CommunicationServer::start() {
  listen_for_connection();

  // Create a thread pool, sharing the IO burden
  boost::thread_group t_group;
  for (int i = 0; i < CommunicationServer::IO_THREAD_POOL_SIZE - 1; i++) {
    t_group.create_thread(
        boost::bind(&CommunicationServer::run_io_service, this));
  }

  // Blocks until all async handlers have stopped
  run_io_service();
  t_group.join_all();
}

void CommunicationServer::stop() {
  acceptor_.close();

  // Wait for async handlers to terminate. This is to make sure it stops accepting new clients before it closes existing connections
  acceptor_aborted_sema_.wait();
  acceptor_aborted_sema_.post();  // Signal, in case some other thread calls this function

  // Close all existing connections
  {
    std::lock_guard<std::mutex> lock(list_mutex_);
    for (ClientConnection::pointer& tccp : client_connections_) {
      tccp->close();
    }
  }

  io_service_.stop();
}

void CommunicationServer::broadcast(const protobuf::GeneralMsg& msg) const {
  std::lock_guard<std::mutex> lock(list_mutex_);
  for (const ClientConnection::pointer& tccp : client_connections_) {
    tccp->send(msg);
  }
}

void CommunicationServer::async_reading_terminated(
    ClientConnection::pointer tccp) {
//  bool is_empty = false;
  {
    std::lock_guard<std::mutex> lock(list_mutex_);

    // Find element in list and erase it
    auto pos = std::find(client_connections_.begin(), client_connections_.end(),
                         tccp);
    if (pos == client_connections_.end()) {
      throw std::runtime_error(
          "Could not locate client_tcp_connection pointer in list");
    } else {
      std::cout << "Connection to " << tccp->get_ip() << " got disconnected" << std::endl;
      client_connections_.erase(pos);
    }

//    is_empty = client_connections_.empty();
  }

  //Uncomment if you wan't server to stop when there are no more clients connected
//  if (is_empty) {
//    server_callback_->no_clients_callback();
//  }
}

void CommunicationServer::listen_for_connection() {
  ClientConnection::pointer tccp = ClientConnection::create(
      this, std::move(server_callback_->create_client()), io_service_);

  // Call accept_handler async when a new client connects
  acceptor_.async_accept(tccp->get_socket(),
                         boost::bind(&CommunicationServer::accept_handler, this, tccp,
                         boost::asio::placeholders::error));
}

void CommunicationServer::accept_handler(
    ClientConnection::pointer tccp, const boost::system::error_code& error) {

  if (error) {
    acceptor_aborted_sema_.post();
  } else {
    // Protect list and insert new client
    {
      std::lock_guard<std::mutex> lock(list_mutex_);
      client_connections_.push_back(tccp);
    }
    tccp->set_socket_ip();
#ifndef NDEBUG
    std::cout << "# CommunicationServer: Client " << tccp->get_ip()
              << " connected" << std::endl;
#endif
    tccp->start_listening();

    listen_for_connection();
  }
}
