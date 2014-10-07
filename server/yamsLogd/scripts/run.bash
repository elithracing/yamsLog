#!/bin/bash

yamslogdir="/opt/yamsLog/"
# Fixme: Should be more intelligent and actually check what interfaces are available and maybe what speeds they should have. 
sudo ip link set can0 up type can bitrate 500000
cd $yamslogdir
./yamsLogd &
#gdb -x test ./yamsLogd > output.txt

echo $! > /var/run/yamslogd.pid
