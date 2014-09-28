/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2012 Philipp Koschorrek, Per Öberg
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


#ifndef XSENSIMU_H_
#define XSENSIMU_H_

#include <sys/ioctl.h>

#include "MTComm.h"

#include <time.h>
#include <signal.h>
#include <stdlib.h>

#include <string>
#include <sstream>

int init_imu(char *, CMTComm &, unsigned short &, int, int, std::string & );
void deinit_imu(char *, CMTComm &);
bool doMtSettings(char *, CMTComm &, unsigned short &, int, int, std::string & );

#endif /* IMU_H_ */
