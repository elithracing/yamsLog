/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2014 Per Öberg
 *
 * This file is based on the work of Gary Briggs obdgpslogger:
 * -- Copyright 2009 Gary Briggs
 * -- obdgpslogger is free software: you can redistribute it and/or modify
 * -- it under the terms of the GNU General Public License as published by
 * -- the Free Software Foundation, either version 2 of the License, or
 * -- (at your option) any later version.
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

#ifndef OBDCONFIG_H
#define OBDCONFIG_H

#define OBD_DEFAULT_SERIALPORT "/dev/ttyUSB0"
#define OBD_CONFIG_FILENAME ".obdgpslogger"
#define OBD_DEFAULT_COLUMNS "temp,rpm,vss,maf,throttlepos"
//#define OBD_FTDIPTY_DEVICE "/var/run/obdftdipty.device"
//#define OBDSIM_ELM_VERSION_STRING "ELM327 v1.3a OBDGPSLogger"
//#define OBDSIM_ELM_DEVICE_STRING "OBDGPSLogger"

#endif // OBDCONFIG_H

