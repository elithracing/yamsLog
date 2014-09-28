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

#include "ClientConnection.h"

#include <string>
#include <thread>
#include <sstream>
#include <iomanip>      // std::setprecision

#include <boost/interprocess/sync/interprocess_semaphore.hpp>
#include <boost/bind.hpp>

#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl.h>

#include "CommunicationServer.h"

ClientConnection::pointer ClientConnection::create(
    CommunicationServer* parent,
    std::unique_ptr<CommunicationServer_ClientCallbackInterface> client_callback,
    boost::asio::io_service& io_service) {
  return pointer(new ClientConnection(parent, std::move(client_callback), io_service));
}

ClientConnection::ClientConnection(
    CommunicationServer* parent,
    std::unique_ptr<CommunicationServer_ClientCallbackInterface> client_callback,
    boost::asio::io_service& io_service)
    : parent_(parent),
      socket_(io_service),
      output_stream_(&streambuf_out_),
      client_callback_(
          std::move(
              client_callback)) {
}

void ClientConnection::listen() {
  boost::asio::async_read(
      socket_,
      boost::asio::buffer(input_stream_buffer_),
      boost::asio::transfer_at_least(1),
      boost::bind(&ClientConnection::read_handler, shared_from_this(),
                  _1, _2));
}

void ClientConnection::start_listening() {
  // Start listening async for messages to read
  listen();

  client_callback_->join_callback(
      boost::bind(&ClientConnection::send, shared_from_this(), _1));
}

boost::asio::ip::tcp::socket& ClientConnection::get_socket() {
  return socket_;
}

void ClientConnection::set_socket_ip() {
  try {
    connected_ip_ = socket_.remote_endpoint().address().to_string();
  } catch (std::exception& exc) {
    connected_ip_ = "unknown";
  }
}

std::string ClientConnection::get_ip() const {
  return connected_ip_;
}

void ClientConnection::send(const protobuf::GeneralMsg& msg) {
  if (msg.IsInitialized() == false) {
    std::stringstream ss;
    ss << "Required fields that were not set for message " << msg.GetTypeName() << ": " << msg.InitializationErrorString();
    throw std::runtime_error(ss.str());
  }

  if (client_callback_->filter_outgoing_callback(msg)) {
    // Protect buffer
    std::lock_guard<std::mutex> lock(send_mutex_);

    // These brackets are to ensure the objects below are destructed -> flushed
    {
      google::protobuf::io::OstreamOutputStream raw_output(&output_stream_);
      google::protobuf::io::CodedOutputStream coded_output(&raw_output);

      coded_output.WriteVarint32(msg.ByteSize());
      msg.SerializeToCodedStream(&coded_output);
    }
    output_stream_.flush();

    try {
      boost::asio::write(socket_, streambuf_out_);
#ifndef NDEBUG
//      if(msg.sub_type() == protobuf::GeneralMsg::DATA_T){
////        std::cout << msg.DebugString() << std::endl;
//        std::cout << msg.data().time()
//            << std::setprecision(2) << std::fixed
//            << " Send DATA_T with id:  " << msg.data().type_id() << std::endl;
//      }else{
        std::cout << "Sent " << msg.DebugString() << " to " << get_ip()
                        << std::endl;
//      }

#endif
    } catch (std::exception& exc) {
      // Do almost nothing. Errors are handled by read handler
#ifndef NDEBUG
      std::cout << "Failed to send " << msg.DebugString() << " to " << get_ip()
                << std::endl;
#endif
    }
  }
}

void ClientConnection::close() {
  if (socket_.is_open()) {
    try {
      socket_.shutdown(boost::asio::ip::tcp::socket::shutdown_both);
    } catch (std::exception& exc) {
      // Do nothing
    }
    try {
      socket_.close();
    } catch (std::exception& exc) {
      // Do nothing
    }
  }
}

// Ignore unused parameter warnings for bytes_transferred
#pragma GCC diagnostic ignored "-Wunused-parameter"
void ClientConnection::read_handler(const boost::system::error_code& error, std::size_t bytes_transferred) {
  if (error) {
    // Signal that async reading has finished
    parent_->async_reading_terminated(shared_from_this());
  } else {
    bool read_success = false;
    protobuf::GeneralMsg read_msg;
    {
      std::lock_guard<std::mutex> lock(receive_mutex_);

      google::protobuf::io::ArrayInputStream array_input_stream(
          input_stream_buffer_, INPUT_BUFFER_SIZE);
      google::protobuf::io::CodedInputStream coded_input_stream(
          &array_input_stream);
      uint32_t msg_size;
      if (coded_input_stream.ReadVarint32(&msg_size)) {
        // Read more here
        google::protobuf::io::CodedInputStream::Limit msg_limit = coded_input_stream
            .PushLimit(msg_size);

        read_success = read_msg.ParseFromCodedStream(&coded_input_stream);
        if (read_success) {
          coded_input_stream.PopLimit(msg_limit);

    #ifndef NDEBUG
          std::cout << "Received " << read_msg.DebugString() << " from " << get_ip()
                    << std::endl;
    #endif

          // Listen async again
          listen();
        } else {
          // Signal that async reading has finished
          parent_->async_reading_terminated(shared_from_this());
        }
      } else {
        parent_->async_reading_terminated(shared_from_this());
      }
    }
    // Don't keep lock here.
    if (read_success) {
      client_callback_->parse_incoming_callback(
          boost::bind(&ClientConnection::send, shared_from_this(), _1),
          read_msg);
    }
  }
}
#pragma GCC diagnostic pop
