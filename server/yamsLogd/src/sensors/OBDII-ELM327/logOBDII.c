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



int logOBDII(int argc, char** argv) {
        int receive_exitsignal = 0; 

	/// Serial port full path to open
	char *serialport = NULL;

	/// List of columsn to log
	char *log_columns = NULL;

	/// Number of samples to take
	int samplecount = -1;

	/// Number of samples per second
	int samplespersecond = 1;

	/// Ask to show the capabilities of the OBD device then exit
	int showcapabilities = 0;

	/// Set if the user wishes to upgrade the baudrate
	long baudrate_upgrade = -1;

	/// Time between samples, measured in microseconds
	long frametime = 0;

	/// Spam all readings to stdout
	int spam_stdout = 0;

	/// Enable elm optimisations
	int enable_optimisations = 0;

	/// Enable serial logging
	int enable_seriallog = 0;

	/// Serial log filename
	char *seriallogname = NULL;

	/// Requested baudrate
	long requested_baud = -1;

	// Config File
	struct OBDGPSConfig *obd_config = obd_loadConfig(0);

	if(NULL != obd_config) {
		samplespersecond = obd_config->samplerate;
		enable_optimisations = obd_config->optimisations;
		requested_baud = obd_config->baudrate;
		baudrate_upgrade = obd_config->baudrate_upgrade;
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
				if(NULL != serialport) {
					free(serialport);
				}
				serialport = strdup(optarg);
				break;
			case 'o':
				enable_optimisations = 1;
				break;
			case 't':
				spam_stdout = 1;
				break;
			case 'c':
				samplecount = atoi(optarg);
				break;
			case 'b':
				requested_baud = strtol(optarg, (char **)NULL, 10);
				break;
			case 'B':
				baudrate_upgrade = strtol(optarg, (char **)NULL, 10);
				break;
			case 'i':
				if(NULL != log_columns) {
					free(log_columns);
				}
				log_columns = strdup(optarg);
				break;
			case 'a':
				samplespersecond = atoi(optarg);
				break;
			case 'l':
				enable_seriallog = 1;
				if(NULL != seriallogname) {
					free(seriallogname);
				}
				seriallogname = strdup(optarg);
				break;
			case 'p':
				showcapabilities = 1;
				break;
			default:
				mustexit = 1;
				break;
		}
	}

	if(mustexit) exit(0);

	if(0 >= samplespersecond) {
		frametime = 0;
	} else {
		frametime = 1000000 / samplespersecond;
	}

	if(NULL == serialport) {
		if(NULL != obd_config && NULL != obd_config->obd_device) {
			serialport = strdup(obd_config->obd_device);
		} else {
			serialport = strdup(OBD_DEFAULT_SERIALPORT);
		}
	}
	if(NULL == log_columns) {
		if(NULL != obd_config && NULL != obd_config->log_columns) {
			log_columns = strdup(obd_config->log_columns);
		} else {
			log_columns = strdup(OBD_DEFAULT_COLUMNS);
		}
	}


	if(enable_seriallog && NULL != seriallogname) {
		startseriallog(seriallogname);
	}

	// Open the serial port.
	int obd_serial_port = openserial(serialport, requested_baud, baudrate_upgrade);

	if(-1 == obd_serial_port) {
		fprintf(stderr, "Couldn't open obd serial port. Attempting to continue.\n");
	} else {
		fprintf(stderr, "Successfully connected to serial port. Will *try* to log obd data\n");
	}

	// Just figure out our car's OBD port capabilities and print them
	if(showcapabilities) {
		printobdcapabilities(obd_serial_port);
		printf("\n");
		closeserial(obd_serial_port);
		exit(0);
	}



	if(-1 == obd_serial_port
	) {
		fprintf(stderr, "Couldn't find either gps or obd to log. Exiting.\n");
		exit(1);
	}

	// number of columns in the insert
	int obdnumcols = 0;


	// Wishlist of commands from config file
	struct obdservicecmd **wishlist_cmds = NULL;
	obd_configCmds(log_columns, &wishlist_cmds);

	void *obdcaps = getobdcapabilities(obd_serial_port,wishlist_cmds);

	obd_freeConfigCmds(wishlist_cmds);
	wishlist_cmds=NULL;
       
	int ii = 0;
	for(ii=0; ii<sizeof(obdcmds_mode1)/sizeof(obdcmds_mode1[0]); ii++) {
		if(NULL != obdcmds_mode1[ii].db_column  && isobdcapabilitysupported(obdcaps,ii)) {
			obdnumcols++;
		}
	}
	obdnumcols++; // To match earlier code /Oberg

	// All of these have obdnumcols-1 since the last column is time
	int cmdlist[obdnumcols-1]; // Commands to send [index into obdcmds_mode1]

	int i,j;
	for(i=0,j=0; i<sizeof(obdcmds_mode1)/sizeof(obdcmds_mode1[0]); i++) {
		if(NULL != obdcmds_mode1[i].db_column) {
			if(isobdcapabilitysupported(obdcaps,i)) {
				cmdlist[j] = i;
				j++;
			}
		}
	}

	freeobdcapabilities(obdcaps);

	// The current time we're inserting
	double time_insert;
	printf("Num OBDCols = %i\n",obdnumcols);
	if(obdnumcols > 1)
	  while(samplecount == -1 || samplecount-- > 0) {
	    struct timeval starttime; // start time through loop
	    struct timeval endtime; // end time through loop
	    struct timeval selecttime; // =endtime-starttime [for select()]
	    struct timeval logged_time; // Time used in log

	    if(0 != gettimeofday(&starttime,NULL)) {
	      perror("Couldn't gettimeofday");
	      break;
	    }


	    time_insert = (double)starttime.tv_sec+(double)starttime.tv_usec/1000000.0f;

	    enum obd_serial_status obdstatus;
	    if(-1 < obd_serial_port) {

	      // Get all the OBD data
	      for(i=0; i<obdnumcols-1; i++) {
		float val;
		unsigned int cmdid = obdcmds_mode1[cmdlist[i]].cmdid;
		int numbytes = enable_optimisations?obdcmds_mode1[cmdlist[i]].bytes_returned:0;
		OBDConvFunc conv = obdcmds_mode1[cmdlist[i]].conv;
		obdstatus = getobdvalue(obd_serial_port, cmdid, &val, numbytes, conv);
		// Get time value just after data returned, will be somewhat close to actual time
		gettimeofday(&logged_time,NULL);
		if(OBD_SUCCESS == obdstatus) {
		  if(spam_stdout) {
		    printf("Spamming STDOUT (not necessary anymore since program changed): %s=%f\n", obdcmds_mode1[cmdlist[i]].db_column, val);
		  }
		  printf("t=%f,%s=%f\n", (double)logged_time.tv_sec+(double)logged_time.tv_usec/1000000.0f,obdcmds_mode1[cmdlist[i]].db_column, val);
		} else {
		  break;
		}
	      }

	      if(OBD_ERROR == obdstatus) {
		fprintf(stderr, "Received OBD_ERROR from serial read. Exiting\n");
		receive_exitsignal = 1;
	      } 
	    }

	    if(0 != gettimeofday(&endtime,NULL)) {
	      perror("Couldn't gettimeofday");
	      break;
	    }

	    // usleep() not as portable as select()

	    if(0 < frametime) {
	      selecttime.tv_sec = endtime.tv_sec - starttime.tv_sec;
	      if (selecttime.tv_sec != 0) {
		endtime.tv_usec += 1000000*selecttime.tv_sec;
		selecttime.tv_sec = 0;
	      }
	      selecttime.tv_usec = (frametime) - 
		(endtime.tv_usec - starttime.tv_usec);
	      if(selecttime.tv_usec < 0) {
		selecttime.tv_usec = 1;
	      }
	      select(0,NULL,NULL,NULL,&selecttime);
	    }
	    if(receive_exitsignal )
	      break;
	  }
	else
	{
	  printf("No supported OBD-Commands found, bitrate error?\n");
	}

	closeserial(obd_serial_port);

	if(enable_seriallog) {
		closeseriallog();
	}

	if(NULL != log_columns) free(log_columns);
	if(NULL != serialport) free(serialport);

	obd_freeConfig(obd_config);
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



