yamsLog
=======

yasmLog - yet another multi sensor logger - is a program for real time
multi sensor logging and supervision. The intention is to be able to
simplify setup, start and stop of measurement and synchronization of
measurement data.

The aim is a program that can save time-stamped sensor data and
display it on different devices such as tablets, desktop computers and
phones. The architecture of the program and it's design (as of 2014)
is described in the documents/Arkitekturbeskrivning.pdf (Swedish)

Licensing 
---------

Most of the project is licensed under GNU GPLv2 except for 
some files that are based on software from other sources.
These are:
 * Distributed under the Boost Software License, Version 1.0.
  * server/yamsLogd/src/sensors/SerialPortLib.cpp
  * server/yamsLogd/src/sensors/SerialPortLib.h

 * Distributed as example implementation of the Xsens MT Communication
   protocol This code is part of the Software Development Toolkit
   distributed with the XSens driver and while it's not GPL-ed it's
   readily available as parts of other open source projects. They are
   therefore assumed to be free to include. Should this be a cause of
   concerns, the ROS-projects, http://wiki.ros.org, uses a
   GPL-re-implementation of this code.
  * server/yamsLogd/src/sensors/MTComm.cpp
  * server/yamsLogd/src/sensors/MTComm.h


Structure of root directory
----------------------------

* server: This is the measurement server yasmLogd. It is written in
      C++ for linux using a few dependencies, mostly boost and
      protobuf libraries.  

* client-backend: This is the common backend, which is the
      general implementation of the display device. It is written in Java.

* clients:
  * android: The first, and currently only frontend at this
      writing. It is written in Java for Android.

* ios: A stand-alone iPhone client (doesn't use the
     client-backend). (Upcoming)

* documents: Contains documents such as Licensing and architecture
     notes. The GNU GPL license can be found in "documents/LICENSE.txt"

* SDK-workspaces: A directory holding build-environments for different
     parts of the project

Building the different parts
------------------------------

Instructions for building the different parts are distributed in
ReadMe files througout the project. A summary is provided below

* client-backend: An ant file "build.xml" is provied to compile
  "backend.jar"

* server: cmake-files are available in the yamsLogd dir

* protobuf: A shellscript "generate.sh" calls protoc and javac to
  create "Protocol.jar"

* android-client: An eclipse project is provided that imports
  jar-files backend.jar from client-backend as well as extLisbs/* from
  external and Protocol.jar from protobuf

Project History
---------------

* 2011: Started out as a set of different programs for logging and
      timestamping of CAN, GPS, accelerometers, and OBDII data by Per
      Öberg.

* 2012: First version (under the name CombinedDataCollector) of a 
      combined, threaded version with easy start/stop. This code was 
      mainly written by Philipp Koschorrek.

* 2013: Second version (under the name CombinedDataCollectorV2) 

* 2014: Complete restructuring with addition of client-server structure
      with protobuf communication. This work was performed as a
      bachelor project under the course name TDDD77/TDDD76 at
      Linköping University, Sweden. Contributions by Tony Fredriksson,
      Max Halldén, Viktor Andersson, Emil Berg, Johan Classon, Emanuel
      Bergsten, Niklas Ljungberg and Johan Sjöberg.

* 2014: Bug fixes and stabilizing by Tony Fredriksson and Niklas
      Ljungberg.


Features
----------

* Start a measurement server automatically 
* Start and stop measurements and keeping track of metadata using 
  for example an Android-tablet
* Add extra information in the datastream such as speedbumps and 
  other information
* Timestamps measurements as they arrive according to the linux 
  clock so that the data can later be aligned with data from other sources. 
* Real time visualization of measurement data on Android-tablet

Todo
-----

**Currently the Android client code and common java-backend is missing in the repo**

Since the last restructure of the project the timestamping code has
degradated a bit which mens that each sample is not necessarily
timesamped asap. 

Contributors
------------

Unfortunately not all contributions are reflected by the copyright
notices in each file. A complete list of contributors is shown below in
the hope that all contributions should be acknowledged.

**Thanks to all contributors**
(in order of arrival to the project). 

* Per Öberg  per _at_ familjenoberg _dot_ se
* Philipp Koschorrek
* David Mattson
* Tony Fredriksson 
* Max Halldén
* Viktor Andersson
* Emil Berg
* Johan Classon
* Emanuel Bergsten
* Niklas Ljungberg 
* Johan Sjöberg
* Erik Frisk
