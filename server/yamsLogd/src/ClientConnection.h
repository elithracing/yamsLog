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

#ifndef CLIENTCONNECTION_H_
#define CLIENTCONNECTION_H_

#include <iostream>
#include <string>
#include <mutex>
#include <memory>

#include <boost/asio.hpp>
#include <boost/enable_shared_from_this.hpp>
#include <boost/utility.hpp>

#include "protocol.pb.h"

// Forward declarations
class CommunicationServer;
class CommunicationServer_ClientCallbackInterface;

/**
 * A connection to a client. One of these exists for each connected client. Tightly copled with CommunicationServer. 
 **/
class ClientConnection :
    public boost::enable_shared_from_this<ClientConnection>,
    private boost::noncopyable {
 public:
  typedef boost::shared_ptr<ClientConnection> pointer;

  static pointer create(
      CommunicationServer* parent,
      std::unique_ptr<CommunicationServer_ClientCallbackInterface> client_callback,
      boost::asio::io_service& io_service);

  // Returns instantly.
  void start_listening();

  boost::asio::ip::tcp::socket& get_socket();

  void set_socket_ip();

  // Returns a string representation of the IP address
  std::string get_ip() const;

  // Blocking, but probably only a buffer copy
  void send(const protobuf::GeneralMsg& msg);

  void close();

 private:
  ClientConnection(CommunicationServer* parent,
                   std::unique_ptr<CommunicationServer_ClientCallbackInterface> client_callback,
                   boost::asio::io_service& io_service);

  void write_handler(const boost::system::error_code& error,
                     std::size_t bytes_transferred, std::string debug_str);
  void read_handler(const boost::system::error_code& error, std::size_t bytes_transferred);
  void listen();

  // In bytes
  static const int INPUT_BUFFER_SIZE = 5000;

  CommunicationServer* parent_;

  // The TCP communication socket
  boost::asio::ip::tcp::socket socket_;

  std::string connected_ip_;

  // Protects buffers
  std::mutex send_mutex_;
  std::mutex receive_mutex_;

  // IO
  boost::asio::streambuf streambuf_out_;
  std::ostream output_stream_;
  char input_stream_buffer_[INPUT_BUFFER_SIZE];

  std::unique_ptr<CommunicationServer_ClientCallbackInterface> client_callback_;
};

#endif  // CLIENTCONNECTION_H_
