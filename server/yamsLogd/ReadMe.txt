This program, CombinedDataCollectorV2 or CDCV2, is used to recorded sensor data on LinCom.
Usage:
	CDCV2.out [port=<port>]

[port=<port>] (optioinal) is the TCP connection port. If no port is given, it defaults to 2001.

Example of usage: 1xIMU, 2xGPS, 1x cars internal CAN, 1x Corrsys velocity and height sensors

CDCV2.out imucam gps1 gps2 carcan1 carcan2

port_config.txt maps sensor IDs to sensor names and data port. The program tries to start all sensors given in port_config.txt
It has to be known which device is connected to which port on the computer.
In case of unknown device configuration this can be tested by using 'cat'-command or sequental connection of the devices.

min_max_sensor_values.txt describes a reasonable value range for a specific sensor. It also gives names to attributes.
