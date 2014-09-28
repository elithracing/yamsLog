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

#include "XSensIMU.h"

/**********************************************************************************************
 * Function	: 	init_imu
 * Parameter: 	char *deviceName - Name of the COM-Port
 * 				CMTComm &mtcomm - Instance of hardware-interface-class for XSens device
 * 				unsigned short &numDevices - Number of devices
 * 				int outputMode - Defines output values
 * 				int outputSettings - Definces output settings
 * 				std::string &returnstring - Return-string with output information
 * Returns	: 	int - Success (=1, else !=1)
 * Misc		:	Set user settings in MTi/MTx
 * 				Assumes initialized global MTComm class
 **********************************************************************************************/
int init_imu(char *deviceName, CMTComm &mtcomm, unsigned short &numDevices,
		int outputMode, int outputSettings, std::string &returnstring) {
	std::ostringstream oss;

	oss << ("%% Accelerometer and position data from Xsens device: ")
			<< std::endl;
	oss << ("%% t,ID,");
	if ((outputMode & OUTPUTMODE_CALIB) != 0) {
		oss << ("accX,accY,accZ,gyrX,gyrY,gyrZ,magX,magY,magZ");
	}
	if (((outputMode & OUTPUTMODE_ORIENT) && (outputMode & OUTPUTMODE_CALIB))
			!= 0) {
		oss << (",");
	}
	if ((outputMode & OUTPUTMODE_ORIENT) != 0) {
		switch (outputSettings & OUTPUTSETTINGS_ORIENTMODE_MASK) {
		case OUTPUTSETTINGS_ORIENTMODE_QUATERNION:
			oss << ("q0,q1,q2,q3") << std::endl;
			break;
		case OUTPUTSETTINGS_ORIENTMODE_EULER:
			oss << ("roll,pitch,yaw") << std::endl;
			break;
		case OUTPUTSETTINGS_ORIENTMODE_MATRIX:
			oss << ("a,b,c,d,e,f,g,h,i") << std::endl;
			break;
		default:
			break;
		}
	}

	// Open and initialize serial port
	if (mtcomm.openPort(deviceName) != MTRV_OK) {
		oss.str() = std::string();
		oss << "%% Cannot open COM port " << std::string(deviceName)
				<< std::endl;
		returnstring = oss.str();
		return MTRV_INPUTCANNOTBEOPENED;
	}

	if (doMtSettings(deviceName, mtcomm, numDevices, outputMode, outputSettings,
			returnstring) == false) {
		oss.str() = std::string();
		oss << "%% Unexpected IMU-message from " << std::string(deviceName)
				<< std::endl;
		returnstring = oss.str();
		return MTRV_UNEXPECTEDMSG;
	}

	returnstring = oss.str();
	return 1;
}

/**********************************************************************************************
 * Function	: 	deinit_imu
 * Parameter:	char *deviceName - Name of the COM-Port
 * 				CMTComm &mtcomm - Instance of hardware-interface-class for XSens device
 * Returns	:	void
 * Misc		:	closes COM-Port and frees allocated memory
 **********************************************************************************************/
void deinit_imu(char *deviceName, CMTComm &mtcomm) {
	mtcomm.close();
}

/**********************************************************************************************
 * Function	: 	doMtSettings
 * Parameter: 	char *deviceName - Name of the COM-Port
 * 				CMTComm &mtcomm - Instance of hardware-interface-class for XSens device
 * 				unsigned short &numDevices - Number of devices
 * 				int outputMode - Defines output values
 * 				int outputSettings - Definces output settings
 * 				std::string &returnstring - Return-string with output information
 * Returns	: 	bool - Success (true/false)
 * Misc		:	Set user settings in MTi/MTx
 * 				Assumes initialized global MTComm class
 **********************************************************************************************/
bool doMtSettings(char *deviceName, CMTComm &mtcomm, unsigned short &numDevices,
		int outputMode, int outputSettings, std::string &returnstring) {
	unsigned long tmpOutputMode, tmpOutputSettings;
	unsigned short tmpDataLength;
	std::ostringstream oss;

	// Put MTi/MTx in Config State
	if (mtcomm.writeMessage(MID_GOTOCONFIG) != MTRV_OK) {
		oss << ("%%No device connected") << std::endl;
		return false;
	}

	// Get current settings and check if Xbus Master is connected
	if (mtcomm.getDeviceMode(&numDevices) != MTRV_OK) {
		if (numDevices == 1)
			oss
					<< ("%%MTi / MTx has not been detected\nCould not get device mode")
					<< std::endl;
		else
			oss
					<< ("%%Not just MTi / MTx connected to Xbus\nCould not get all device modes")
					<< std::endl;
		return false;
	}

	// Check if Xbus Master is connected
	mtcomm.getMode(tmpOutputMode, tmpOutputSettings, tmpDataLength, BID_MASTER);
	if (tmpOutputMode == OUTPUTMODE_XM) {
		// If Xbus Master is connected, attached Motion Trackers should not send sample counter
		outputSettings &= 0xFFFFFFFF - OUTPUTSETTINGS_TIMESTAMP_SAMPLECNT;
	}

	// Set output mode and output settings for each attached MTi/MTx
	for (int i = 0; i < numDevices; i++) {
		if (mtcomm.setDeviceMode(outputMode, outputSettings,
				BID_MT + i) != MTRV_OK) {
			oss << ("%%Could not set (all) device mode(s)") << std::endl;
			return false;
		}
	}

	// Put MTi/MTx in Measurement State
	mtcomm.writeMessage(MID_GOTOMEASUREMENT);

	returnstring = oss.str();
	return true;
}

