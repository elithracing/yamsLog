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

// Some references:
// mpg calculation: http://www.mp3car.com/vbulletin/engine-management-obd-ii-engine-diagnostics-etc/75138-calculating-mpg-vss-maf-obd2.html
// function list: http://www.kbmsystems.net/obd_tech.htm

#include "obdconfig.h"
#include "logOBDII.h"
#include "obdservicecommands.h"
#include "obdserial.h"
#include "supportedcommands.h"

#include "obdconfigfile.h"

#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <time.h>
#include <getopt.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/stat.h>

class OBDIIData{
public:
  OBDIIData();
  ~OBDIIData();

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

OBDIIData::OBDIIData()
{
        receive_exitsignal = 0; 

	/// Serial port full path to open
	serialport = NULL;

	/// List of columsn to log
	log_columns = NULL;

	/// Number of samples to take
	samplecount = -1;

	/// Number of samples per second
	samplespersecond = 1;

	/// Ask to show the capabilities of the OBD device then exit
	showcapabilities = 0;

	/// Set if the user wishes to upgrade the baudrate
	baudrate_upgrade = -1;

	/// Time between samples, measured in microseconds
	frametime = 0;

	/// Spam all readings to stdout
	spam_stdout = 0;

	/// Enable elm optimisations
	enable_optimisations = 0;

	/// Enable serial logging
	enable_seriallog = 0;

	/// Serial log filename
	seriallogname = NULL;

	/// Requested baudrate
	requested_baud = -1;

	// Config File
	obd_config = obd_loadConfig(0);

	// Zero the serial port.
	obd_serial_port = 0;
	
	// To keep track of the actual number of sensors logged
	obdnumcols = 0;

	// Pointer to command list for all logged signals
	cmdlist = NULL;

}

OBDIIData::~OBDIIData()
{
  delete cmdlist;
  if(NULL != log_columns) free(log_columns);
  if(NULL != serialport) free(serialport);
  if(NULL != seriallogname) free(seriallogname);
  obd_freeConfig(obd_config);
}


void obdreadloop(OBDIIData*);

void obdcleanup(OBDIIData*);

int logOBDII(int argc, char** argv) {
  
        OBDIIData obddata;

	if(NULL != obddata.obd_config) {
		obddata.samplespersecond = obddata.obd_config->samplerate;
		obddata.enable_optimisations = obddata.obd_config->optimisations;
		obddata.requested_baud = obddata.obd_config->baudrate;
		obddata.baudrate_upgrade = obddata.obd_config->baudrate_upgrade;
	}
	

	// Do not attempt to buffer stdout at all
	setvbuf(stdout, (char *)NULL, _IONBF, 0);

	int optc;
	int mustexit = 0;
	while ((optc = getopt_long (argc, argv, shortopts, longopts, NULL)) != -1) {
		switch (optc) {
		        case 'h':
                               printhelp(argv[0]);
                               mustexit = 1;
                               break;
			case 's':
				if(NULL != obddata.serialport) {
					free(obddata.serialport);
				}
				obddata.serialport = strdup(optarg);
				break;
			case 'o':
				obddata.enable_optimisations = 1;
				break;
			case 't':
				obddata.spam_stdout = 1;
				break;
			case 'c':
				obddata.samplecount = atoi(optarg);
				break;
			case 'b':
				obddata.requested_baud = strtol(optarg, (char **)NULL, 10);
				break;
			case 'B':
				obddata.baudrate_upgrade = strtol(optarg, (char **)NULL, 10);
				break;
			case 'i':
				if(NULL != obddata.log_columns) {
					free(obddata.log_columns);
				}
				obddata.log_columns = strdup(optarg);
				break;
			case 'a':
				obddata.samplespersecond = atoi(optarg);
				break;
			case 'l':
				obddata.enable_seriallog = 1;
				if(NULL != obddata.seriallogname) {
					free(obddata.seriallogname);
				}
				obddata.seriallogname = strdup(optarg);
				break;
			case 'p':
				obddata.showcapabilities = 1;
				break;
			default:
				mustexit = 1;
				break;
		}
	}

	if(mustexit) exit(0);

	if(0 >= obddata.samplespersecond) {
		obddata.frametime = 0;
	} else {
		obddata.frametime = 1000000 / obddata.samplespersecond;
	}

	if(NULL == obddata.serialport) {
		if(NULL != obddata.obd_config && NULL != obddata.obd_config->obd_device) {
			obddata.serialport = strdup(obddata.obd_config->obd_device);
		} else {
			obddata.serialport = strdup(OBD_DEFAULT_SERIALPORT);
		}
	}
	if(NULL == obddata.log_columns) {
		if(NULL != obddata.obd_config && NULL != obddata.obd_config->log_columns) {
			obddata.log_columns = strdup(obddata.obd_config->log_columns);
		} else {
			obddata.log_columns = strdup(OBD_DEFAULT_COLUMNS);
		}
	}


	if(obddata.enable_seriallog && NULL != obddata.seriallogname) {
		startseriallog(obddata.seriallogname);
	}

	// Open the serial port.
	obddata.obd_serial_port = openserial(obddata.serialport, obddata.requested_baud, obddata.baudrate_upgrade);

	if(-1 == obddata.obd_serial_port) {
		fprintf(stderr, "Couldn't open obd serial port. Attempting to continue.\n");
	} else {
		fprintf(stderr, "Successfully connected to serial port. Will *try* to log obd data\n");
	}

	// Just figure out our car's OBD port capabilities and print them
	if(obddata.showcapabilities) {
		printobdcapabilities(obddata.obd_serial_port);
		printf("\n");
		closeserial(obddata.obd_serial_port);
		exit(0);
	}



	if(-1 == obddata.obd_serial_port
	) {
		fprintf(stderr, "Couldn't find either gps or obd to log. Exiting.\n");
		exit(1);
	}

	// Wishlist of commands from config file
	struct obdservicecmd **wishlist_cmds = NULL;
	obd_configCmds(obddata.log_columns, &wishlist_cmds);

	void *obdcaps = getobdcapabilities(obddata.obd_serial_port,wishlist_cmds);

	obd_freeConfigCmds(wishlist_cmds);
	wishlist_cmds=NULL;
       
	unsigned int ii = 0;
	for(ii=0; ii < sizeof(obdcmds_mode1)/sizeof(obdcmds_mode1[0]); ii++) {
		if(NULL != obdcmds_mode1[ii].db_column  && isobdcapabilitysupported(obdcaps,ii)) {
			obddata.obdnumcols++;
		}
	}
	obddata.obdnumcols++; // To match earlier code /Oberg

	// All of these have obddata.obdnumcols-1 since the last column is time
	obddata.cmdlist = new int[obddata.obdnumcols-1]; // Commands to send [index into obdcmds_mode1]

	unsigned int i,j;
	for(i=0,j=0; i<sizeof(obdcmds_mode1)/sizeof(obdcmds_mode1[0]); i++) {
		if(NULL != obdcmds_mode1[i].db_column) {
			if(isobdcapabilitysupported(obdcaps,i)) {
				obddata.cmdlist[j] = i;
				j++;
			}
		}
	}

	freeobdcapabilities(obdcaps);

	printf("Num OBDCols = %i\n",obddata.obdnumcols);
	if(obddata.obdnumcols > 1)
	  obdreadloop(&obddata);
	else
	{
	  printf("No supported OBD-Commands found, bitrate error?\n");
	}

	obdcleanup(&obddata);

	return 0;
}


void printhelp(const char *argv0) {
       printf("Usage: %s [params]\n"
                               "   [-s|--serial <" OBD_DEFAULT_SERIALPORT ">]\n"
                               "   [-c|--count <infinite>]\n"
                               "   [-i|--log-columns <" OBD_DEFAULT_COLUMNS ">]\n"
                               "   [-t|--spam-stdout]\n"
                               "   [-p|--capabilities]\n"
                               "   [-o|--enable-optimisations]\n"
                               "   [-u|--output-log <filename>]\n"
                               "   [-b|--baud <number>]\n"
                               "   [-B|--modifybaud <number>]\n"
                               "   [-l|--serial-log <filename>]\n"
                               "   [-a|--samplerate [1]]\n"
                               "   [-v|--version] [-h|--help]\n", argv0);
}


void obdreadloop(OBDIIData* obddata)
{
  // The current time we're inserting
  // double time_insert;
  
  while(obddata->samplecount == -1 || obddata->samplecount-- > 0) {
    struct timeval starttime; // start time through loop
    struct timeval endtime; // end time through loop
    struct timeval selecttime; // =endtime-starttime [for select()]
    struct timeval logged_time; // Time used in log
    
    if(0 != gettimeofday(&starttime,NULL)) {
      perror("Couldn't gettimeofday");
      break;
    }
    
    // time_insert = (double)starttime.tv_sec+(double)starttime.tv_usec/1000000.0f;
    
    enum obd_serial_status obdstatus;
    if(-1 < obddata->obd_serial_port) {
      
      // Get all the OBD data
      int i;
      for(i=0; i<obddata->obdnumcols-1; i++) {
	float val;
	unsigned int cmdid = obdcmds_mode1[obddata->cmdlist[i]].cmdid;
	int numbytes = obddata->enable_optimisations?obdcmds_mode1[obddata->cmdlist[i]].bytes_returned:0;
	OBDConvFunc conv = obdcmds_mode1[obddata->cmdlist[i]].conv;
	obdstatus = getobdvalue(obddata->obd_serial_port, cmdid, &val, numbytes, conv);
	// Get time value just after data returned, will be somewhat close to actual time
	gettimeofday(&logged_time,NULL);
	if(OBD_SUCCESS == obdstatus) {
	  if(obddata->spam_stdout) {
	    printf("Spamming STDOUT (not necessary anymore since program changed): %s=%f\n", obdcmds_mode1[obddata->cmdlist[i]].db_column, val);
	  }
	  printf("t=%f,%s=%f\n", (double)logged_time.tv_sec+(double)logged_time.tv_usec/1000000.0f,obdcmds_mode1[obddata->cmdlist[i]].db_column, val);
	} else {
	  break;
	}
      }
      
      if(OBD_ERROR == obdstatus) {
	fprintf(stderr, "Received OBD_ERROR from serial read. Exiting\n");
	obddata->receive_exitsignal = 1;
      } 
    }
    
    if(0 != gettimeofday(&endtime,NULL)) {
      perror("Couldn't gettimeofday");
      break;
    }
    
    // usleep() not as portable as select()
    
    if(0 < obddata->frametime) {
      selecttime.tv_sec = endtime.tv_sec - starttime.tv_sec;
      if (selecttime.tv_sec != 0) {
	endtime.tv_usec += 1000000*selecttime.tv_sec;
	selecttime.tv_sec = 0;
      }
      selecttime.tv_usec = (obddata->frametime) - 
	(endtime.tv_usec - starttime.tv_usec);
      if(selecttime.tv_usec < 0) {
	selecttime.tv_usec = 1;
      }
      select(0,NULL,NULL,NULL,&selecttime);
    }
    if(obddata->receive_exitsignal )
      break;
  }
}

void obdcleanup(OBDIIData* obddata)
{
	closeserial(obddata->obd_serial_port);

	if(obddata->enable_seriallog) {
		closeseriallog();
	}


}
