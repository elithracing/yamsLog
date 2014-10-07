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

Some AT-commands
-------------------------
ATE1   - Echo ON
ATL1   - Linefeed on
ATZ    - Reset
ATDP   - Describe protocol
ATRV   - Read voltage
ATSP00 - Set protocol to auto and save it
ATTPA0 - Try protocol ...
ATAT0  - Adaptive timing off
ATAT1  - Adaptive timing auto1
ATAT2  - Adaptive timing auto2
ATSThh - Waiting time before NO DATA (Set Timeout to hh x 4 msec)
ATST0  - Reset Waiting time before NO DATA (Set Timeout to hh x 4 msec)

Protocols: 

0 - Automatic
1 - SAE J1850 PWM (41.6 kbaud)
2 - SAE J1850 VPW (10.4 kbaud)
3 - ISO 9141-2 (5 baud init, 10.4 kbaud)
4 - ISO 14230-4 KWP (5 baud init, 10.4 kbaud)
5 - ISO 14230-4 KWP (fast init, 10.4 kbaud)
6 - ISO 15765-4 CAN (11 bit ID, 500 kbaud)
7 - ISO 15765-4 CAN (29 bit ID, 500 kbaud)
8 - ISO 15765-4 CAN (11 bit ID, 250 kbaud)
9 - ISO 15765-4 CAN (29 bit ID, 250 kbaud)
A - SAE J1939 CAN (29 bit ID, 250* kbaud)
B - USER1 CAN (11* bit ID, 125* kbaud)
C - USER2 CAN (11* bit ID, 50* kbaud)
