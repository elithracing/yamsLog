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

#ifndef COMMUNICATIONSERVER_H_
#define COMMUNICATIONSERVER_H_

#include <vector>
#include <string>
#include <mutex>

#include <boost/interprocess/sync/interprocess_semaphore.hpp>
#include <boost/asio.hpp>
#include <boost/utility.hpp>
#include <boost/function.hpp>
#include "sensors/AbstractSensor.h"

#include "ClientConnection.h"
//#include "ProjectHandler.h"

//class AbstractSensor;

// I hate c++
class CommunicationServer_ClientCallbackInterface {
 public:
  virtual ~CommunicationServer_ClientCallbackInterface() {
  }

  virtual void join_callback(
      boost::function<void(const protobuf::GeneralMsg&)> send_func) = 0;
  virtual void parse_incoming_callback(
      boost::function<void(const protobuf::GeneralMsg&)> send_func,
      const protobuf::GeneralMsg& msg) = 0;
  // Returns true if an outgoing message should be sent
  virtual bool filter_outgoing_callback(const protobuf::GeneralMsg& msg) = 0;
};

/**
 *  Listens and setups TCP connections. Tighlty coupled with ClientConnection. Holds all connected clients and makes it possible to send to all connected clients at once. Communicates with protobuf.
**/
class CommunicationServer : private boost::noncopyable {
 public:
  typedef CommunicationServer_ClientCallbackInterface ClientCallbackInterface;

  friend class ClientConnection;

  class ServerCallbackInterface {
   public:
    virtual ~ServerCallbackInterface() {
    }
    virtual std::unique_ptr<ClientCallbackInterface> create_client() = 0;
    virtual void no_clients_callback() = 0;
  };

  CommunicationServer(int port, ServerCallbackInterface* server_callback);

  // Blocks until all async handlers have stopped. Should only be called once
  void start();
  // Stops listening for new connections and kills all existing connections
  void stop();
  // Send to clients which are connected right now
  void broadcast(const protobuf::GeneralMsg& msg) const;

 private:
  void listen_for_connection();

  // Callback function for ClientConnection when the socket is down and there'll be no more reading
  void async_reading_terminated(ClientConnection::pointer tccp);

  void run_io_service();

  // Accept handler for accepting incoming TCP client connections to this server
  void accept_handler(ClientConnection::pointer tccp,
                      const boost::system::error_code& error);

  // The number of threads to handle async IO. Set to some low number to enable cores.
  static const int IO_THREAD_POOL_SIZE = 4;

  // Protects the list below
  mutable std::mutex list_mutex_;

  // Holds all active connections, and some inactive. Must be protected with mutex
  std::vector<ClientConnection::pointer> client_connections_;

  // Semaphore to make sure all async listening is cancelled before modifying the list
  boost::interprocess::interprocess_semaphore acceptor_aborted_sema_;

  boost::asio::io_service io_service_;

  // Accepts incoming connections (server socket)
  boost::asio::ip::tcp::acceptor acceptor_;

  ServerCallbackInterface* server_callback_;
};

#endif  // COMMUNICATIONSERVER_H_
