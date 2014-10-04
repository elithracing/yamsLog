sudo chmod a+rwx /dev/ttyUSB0
./logOBDII -s /dev/ttyUSB0  -b 57600 -i temp,rpm,vss,maf,throttlepos -a 10
