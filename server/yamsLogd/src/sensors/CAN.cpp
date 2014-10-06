/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2012  Per Öberg, Philipp Koschorrek
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


#include "CAN.h"
#include <iostream>

bool init_can(char* deviceName, int& CANsock, std::string& returnstring) {

  int rc;
  std::ostringstream oss;

  oss<<("%% Interpretation of CAN messages is as follows: ")<<std::endl;
  oss<<("%% t,ID,CAN_ID,CAN_message_length,CAN_message with CAN_message_length bytes")<<std::endl;

  CANsock = socket(PF_CAN, SOCK_RAW, CAN_RAW);

  // P.Ö.
  //can_err_mask_t err_mask = ( CAN_ERR_TX_TIMEOUT | CAN_ERR_BUSOFF );
  //setsockopt(CANsock, SOL_CAN_RAW, CAN_RAW_ERR_FILTER,&err_mask, sizeof(err_mask));

  /* Locate the interface you wish to use */
  struct ifreq ifr;
  strcpy(ifr.ifr_name, deviceName);

  /* returns 0 if success */
  rc = ioctl(CANsock, SIOCGIFINDEX, &ifr);

  /* ifr.ifr_ifindex gets filled with that device's index */

  /* Select that CAN interface, and bind the socket to it. */
  struct sockaddr_can addr;
  addr.can_family = AF_CAN;
  addr.can_ifindex = ifr.ifr_ifindex;

  /* returns 0 if success */
  rc += bind(CANsock, (struct sockaddr*) &addr, sizeof(addr));

  struct timeval tv;
  tv.tv_sec = 5;  /* 5 Secs Timeout */
  tv.tv_usec = 0;  // Not init'ing this can cause strange errors

  //Set timeout option for socket so that the function recv returns after tv_sec seconds if socket has no data to be read
  setsockopt(CANsock, SOL_SOCKET, SO_RCVTIMEO, (char *)&tv,sizeof(struct timeval));

  if(!rc){
    returnstring = oss.str();
    return true;
  }else{
    oss<<("%% Cannot initialize CAN")<<std::endl;
    returnstring = oss.str();
    return false;
  }
}

void deinit_can(int&) {

}


