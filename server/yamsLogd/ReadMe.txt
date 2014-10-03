This program, yamsLogd, is used to recorded sensor data on a unix/linux or mac server. (There's no real reason why it shouln't be possible to use on a windows computer to.)


Building: 
--------------

Building a "Release" version

> mkdir Release
> cd Release
> cmake -DCMAKE_BUILD_TYPE=Release ..

Building a "Debug" version

> mkdir Debug
> cd Debug
> cmake -DCMAKE_BUILD_TYPE=Debug ..

Usage: 
--------------
After building, put your binary wherever you want it. Make sure that
"min_max_sensor_values.txt" and "port_config.txt" are available in the
working dir when stating.

	yamsLogd [port=<port>]

[port=<port>] (optioinal) is the TCP connection port. If no port is given, it defaults to 2001.

Example of usage: 1xIMU, 2xGPS, 1x cars internal CAN, 1x Corrsys velocity and height sensors

	yamsLogd imucam gps1 gps2 carcan1 carcan2

port_config.txt maps sensor IDs to sensor names and data port. The
program tries to start all sensors given in port_config.txt It has to
be known which device is connected to which port on the computer.  In
case of unknown device configuration this can be tested by using
'cat'-command or sequental connection of the devices.

min_max_sensor_values.txt describes a reasonable value range for a
specific sensor. It also gives names to attributes.


Note: Since the implementation of port_config.txt it's not necessary to list the sensors that are used on the command line
