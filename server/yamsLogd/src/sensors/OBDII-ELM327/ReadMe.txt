The code in this directory is used by the OBDII_ELM327 sensor. For
troubleshooting and other experiments a stand-alone tool "OBDtool" is
available, using exactly the same code as the sensor. 

Note on troubleshooting
------------------------

To get the OBDII code up an running the ELM327 (or similar) needs to
configured for the right baud-rate, right OBD-interface (usually
automatic works). A common problem is that the car does not work with
the auto-configure methods and therefore OBDII-interface needs to be
hard-coded.

To be able to troubleshoot the interface it can be wise to look at the actual
serial-port communication using 

> OBDtool --serial-log logname.txt 

Also: There are excellent documentation from ELM about how to communicate
with the chips using a terminal emulator,  see for example: [1]. There's also a nice 
ELM327-simulator available for testing your stuff at [2].

Note on supported PIDS
------------------------
It is possible to learn what PIDS your car is supporting using 

> OBDtool --capabilities


Upcoming features 
----------------------------
* Quqery/Set OBD-protocol from OBD-dongle
* Document practical termial-commands

[1] Upcoming documentation link...
[2] http://icculus.org/obdgpslogger/obdsim.html
