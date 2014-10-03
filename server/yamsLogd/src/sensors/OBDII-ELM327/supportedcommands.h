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

#ifndef __SUPPORTEDCOMMANDS_H
#define __SUPPORTEDCOMMANDS_H

#include "obdservicecommands.h"

/// Print the capabilities this device claims
void printobdcapabilities(int obd_serial_port);

/// Get the capabilities this device claims
/** Be sure to pass the return value to freecapabilities when you're done
  \param wishlist NULL-sentinel'd list of PIDs. If NULL, assume they want to search all obd obdcmds
  \return an opaque type you then pass to iscapabilitysupported
  */
void *getobdcapabilities(int obd_serial_port, struct obdservicecmd **wishlist);

/// Free the values returned from getcapabilities
void freeobdcapabilities(void *caps);

/// Find out if a pid is supported
/** \param caps the value returned by getcapabilities
    \param pid the PID we want to know if it's supported
	\return 1 for "yes", 0 for "no".
 */
int isobdcapabilitysupported(void *caps, const unsigned int pid);

#endif // __SUPPORTEDCOMMANDS_H


