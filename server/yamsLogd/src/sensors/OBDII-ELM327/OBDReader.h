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

#ifndef __OBDREADER_H
#define __OBDREADER_H

#include <getopt.h>
#include <stdlib.h>
#include <list>
#include <string>

class OBDReader{
public:

  struct argStruct{
    const char *s = NULL;
    int c = -1;
    const char *logColums = NULL;
    bool spamStdout = false;
    bool optimizations = false;
    const char *outPutLog = NULL;
    unsigned int baud = 0;
    unsigned int modifyBaud = 0;
    const char *serialLog = NULL;
    unsigned int sampleRate = -1;
  };

  struct measData{
    struct timeval time;
    std::string name;
    double val;
  };

  OBDReader();
  ~OBDReader();

  int initConnection();
  int readLoop(std::list<measData*>* returnQueue);
  void printHelp(const char *argv0);
  void printVersion();
  void argsFromCmdLine(int argc, char** argv);
  void argsFromStruct(OBDReader::argStruct* args);

private:
  int receive_exitsignal; 
  
  /// Serial port full path to open
  char *serialport;
  
  /// List of columsn to log
  char *log_columns;
  
  /// Number of samples to take
  int samplecount;
  
  /// Number of samples per second
  int samplespersecond;
  
  /// Ask to show the capabilities of the OBD device then exit
  int showcapabilities;
  
  /// Set if the user wishes to upgrade the baudrate
  long baudrate_upgrade;
  
  /// Time between samples, measured in microseconds
  long frametime;
  
  /// Spam all readings to stdout
  int spam_stdout;
  
  /// Enable elm optimisations
  int enable_optimisations;
  
  /// Enable serial logging
  int enable_seriallog;
  
  /// Serial log filename
  char *seriallogname;
  
  /// Requested baudrate
  long requested_baud;
  
  // Config File
  struct OBDGPSConfig *obd_config;

  // Serial port 
  int obd_serial_port;

  // To keep track of actual number of colums logged
  int obdnumcols;

  // Pointer to command list for all logged signals
  int* cmdlist;
};


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


#endif // __MAIN_H

