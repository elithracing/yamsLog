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

#ifndef __MAIN_H
#define __MAIN_H

#include <getopt.h>
#include <stdlib.h>

static const struct option longopts[] = {
	{ "help", no_argument, NULL, 'h' }, ///< Print the help text
	{ "version", no_argument, NULL, 'v' }, ///< Print the version text
	{ "serial", required_argument, NULL, 's' }, ///< Serial Port
	{ "db", required_argument, NULL, 'd' }, ///< Database file
	{ "samplerate", required_argument, NULL, 'a' }, ///< Number of samples per second
	{ "count", required_argument, NULL, 'c' }, ///< Number of values to grab
	{ "capabilities", no_argument, NULL, 'p' }, ///< Show the capabilities the OBD device claims it can report
	{ "spam-stdout", no_argument, NULL, 't' }, ///< Spam readings to stdout
	{ "serial-log", required_argument, NULL, 'l' }, ///< Log serial port data transfer
	{ "output-log", required_argument, NULL, 'u' }, ///< Log serial port data transfer
	{ "baud", required_argument, NULL, 'b' }, ///< Baud rate to connect at
	{ "modifybaud", required_argument, NULL, 'B' }, ///< Upgrade to this baudrate
	{ "log-columns", required_argument, NULL, 'i' }, ///< Log these columns
	{ "enable-optimisations", no_argument, NULL, 'o' }, ///< Enable elm optimisations
#ifdef OBDPLATFORM_POSIX
	{ "daemon", no_argument, NULL, 'm' }, ///< Daemonise
#endif //OBDPLATFORM_POSIX
	{ NULL, 0, NULL, 0 } ///< End
};

static const char shortopts[] = "htd:i:b:vs:l:c:a:opu:B:"
#ifdef OBDPLATFORM_POSIX
	"m"
#endif //OBDPLATFORM_POSIX
;

void printhelp(const char *argv0);

void printversion();

#endif // __MAIN_H

