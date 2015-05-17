
cmake_minimum_required(VERSION 2.8)

# Turn on to see cmake output
set(CMAKE_VERBOSE_MAKEFILE off)

# Uncomment for JOHAN S TURBO 2000 MODE.
# The number after '=' is how many normal sensor values for one sensor
# that will be followed by an illegal sensor value, that will change
# the sensor status. The illegal one will be the sensor max value +
# 1. If the next sensor value is OK, it will, of course, change back
# the sensor status.  Note that this doesn't take the index in the
# repeated float structure sent into consideration, so every nth data
# point is modified for a sensor, not all sensor values in every nth
# data message.  Also, a max limit must of course be set for that
# sensor, otherwise, JOHAN S TURBO 2000 MODE will have no effect.
# add_definitions(-DJOHAN_S=1000)

project(yamsLogd)

# Eclipse hack
find_path(STDIO_INCLUDE_PATH stdio.h)
include_directories("${STDIO_INCLUDE_PATH}/")

# include_directories (SYSTEM "${CMAKE_SOURCE_DIR}/../../protobuf/src")
include_directories (SYSTEM "${CMAKE_SOURCE_DIR}/../../protobuf/gen/cpp")

include_directories("${CMAKE_SOURCE_DIR}/src")

find_package(Protobuf REQUIRED)
include_directories(${PROTOBUF_INCLUDE_DIRS})
PROTOBUF_GENERATE_CPP(PROTO_SRCS PROTO_HDRS ${CMAKE_SOURCE_DIR}/../../protobuf/protocol.proto)
add_library(ProtobufProto ${PROTO_SRCS} ${PROTO_HDRS})

file(GLOB_RECURSE MUPARSER_SRCS "${CMAKE_SOURCE_DIR}/external/muparser/src/**")
file(GLOB_RECURSE MUPARSER_HDRS "${CMAKE_SOURCE_DIR}/external/muparser/include/**")
add_library(muParser ${MUPARSER_SRCS} ${MUPARSER_HDRS})
target_include_directories(muParser PUBLIC "${CMAKE_SOURCE_DIR}/external/muparser/include/")

add_library(DataLogger src/DataLogger.cpp src/DataLogger.h)
add_library(CAN src/sensors/CAN.cpp src/sensors/CAN.h)
add_library(MTComm src/sensors/MTComm.cpp src/sensors/MTComm.h)
add_library(SerialLib src/sensors/SerialPortLib.cpp src/sensors/SerialPortLib.h)
add_library(XSensIMU src/sensors/XSensIMU.cpp src/sensors/XSensIMU.h)

add_library(CommunicationServer src/CommunicationServer.cpp src/CommunicationServer.h)
add_library(ClientConnection src/ClientConnection.cpp src/ClientConnection.h)
add_library(StatusObserver src/StatusObserver.cpp src/StatusObserver.h)
add_library(ClientCallbackImpl src/ClientCallbackImpl.cpp src/ClientCallbackImpl.h)
add_library(SensorDefinitionReader src/SensorDefinitionReader.cpp src/SensorDefinitionReader.h)
add_library(ModificationCallbackImpl src/ModificationCallbackImpl.cpp src/ModificationCallbackImpl.h)

add_library(AbstractService src/services/AbstractService.cpp src/services/AbstractService.h)
add_library(CommunicationServerService src/services/CommunicationServerService.cpp src/services/CommunicationServerService.h)
add_library(KeyboardInputService src/services/KeyboardInputService.cpp src/services/KeyboardInputService.h)
add_library(ProtobufFileOutputService src/services/ProtobufFileOutputService.cpp src/services/ProtobufFileOutputService.h)
add_library(ProtobufTcpOutputService src/services/ProtobufTcpOutputService.cpp src/services/ProtobufTcpOutputService.h)
add_library(TextFileOutputService src/services/TextFileOutputService.cpp src/services/TextFileOutputService.h)

add_library(AbstractSensor src/sensors/AbstractSensor.cpp src/sensors/AbstractSensor.h)
add_library(CanSensor src/sensors/CanSensor.cpp src/sensors/CanSensor.h)
add_library(CorrsysSensor src/sensors/CorrsysSensor.cpp src/sensors/CorrsysSensor.h)
add_library(ERDataLogger src/sensors/ERDataLogger.cpp src/sensors/ERDataLogger.h)
add_library(GpsSensor src/sensors/GpsSensor.cpp src/sensors/GpsSensor.h)
add_library(ImuSensor src/sensors/ImuSensor.cpp src/sensors/ImuSensor.h)
add_library(VirtualSensor src/sensors/VirtualSensor.cpp src/sensors/VirtualSensor.h)

add_library(OBDIIElm src/sensors/OBDII-ELM327/OBDReader.cpp src/sensors/OBDII-ELM327/obdservicecommands.cpp src/sensors/OBDII-ELM327/obdconfigfile.cpp src/sensors/OBDII-ELM327/obdconvertfunctions.cpp src/sensors/OBDII-ELM327/obdrevconvertfunctions.cpp src/sensors/OBDII-ELM327/obdserial.cpp src/sensors/OBDII-ELM327/supportedcommands.cpp src/sensors/OBDII-ELM327/OBDReader.h src/sensors/OBDII-ELM327/obdconfigfile.h src/sensors/OBDII-ELM327/obdconvertfunctions.h  src/sensors/OBDII-ELM327/obdserial.h src/sensors/OBDII-ELM327/obdservicecommands.h src/sensors/OBDII-ELM327/supportedcommands.cpp)

add_library(OBDIIElmSensor src/sensors/OBDIIElmSensor.cpp src/sensors/OBDIIElmSensor.h)

add_library(Experiment src/Experiment.cpp src/Experiment.h)
add_library(Project src/Project.cpp src/Project.h)
add_library(ProjectHandler src/ProjectHandler.cpp src/ProjectHandler.h)

# Use c++11
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++0x")
# If using GCC
if("${CMAKE_CXX_COMPILER_ID}" STREQUAL "GNU")
  # Add compiler flags for all files
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -pedantic -Wshadow -Wpointer-arith -Wcast-qual")
  # But ignore warnings for the following
  set_source_files_properties(
      src/sensors/MTComm.cpp src/sensors/MTComm.h
      src/sensors/SerialPortLib.cpp src/sensors/SerialPortLib.h
      src/sensors/XSensIMU.cpp src/sensors/XSensIMU.h
      src/sensors/OBDII-ELM327/obdconvertfunctions.cpp
      src/sensors/OBDII-ELM327/obdrevconvertfunctions.cpp
      src/sensors/OBDII-ELM327/obdconfigfile.cpp
      src/sensors/OBDII-ELM327/obdserial.cpp
      PROPERTIES COMPILE_FLAGS "-Wno-unused-parameter -Wno-maybe-uninitialized -Wno-cast-qual -Wno-type-limits -Wno-shadow -Wno-switch -Wno-format-contains-nul") 
# If using Clang
elseif("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
  # Add compiler flags for all files
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -pedantic -Wshadow -Wpointer-arith -Wcast-qual")
  # But ignore warnings for the following
  set_source_files_properties(
      src/sensors/MTComm.cpp src/sensors/MTComm.h
      src/sensors/SerialPortLib.cpp src/sensors/SerialPortLib.h
      src/sensors/XSensIMU.cpp src/sensors/XSensIMU.h
      PROPERTIES COMPILE_FLAGS "-Wno-unused-parameter -Wno-maybe-uninitialized -Wno-type-limits -Wno-shadow -Wno-cast-qual -Wno-switch")
endif()

add_executable(yamsLogd src/Main.cpp)
add_executable(OBDtool src/sensors/OBDII-ELM327/OBDtool.cpp)

target_link_libraries(yamsLogd DataLogger ProjectHandler Project
  Experiment ProtobufProto CommunicationServer ClientConnection
  StatusObserver ClientCallbackImpl AbstractService
  CommunicationServerService KeyboardInputService
  ProtobufFileOutputService ProtobufTcpOutputService
  TextFileOutputService AbstractSensor CanSensor CorrsysSensor
  ERDataLogger GpsSensor OBDIIElmSensor OBDIIElm VirtualSensor 
  ImuSensor CAN MTComm SerialLib XSensIMU SensorDefinitionReader 
  ModificationCallbackImpl 
  ${PROTOBUF_LIBRARIES} muParser
  boost_system boost_filesystem boost_thread pthread rt)


target_link_libraries(OBDtool OBDIIElm)
