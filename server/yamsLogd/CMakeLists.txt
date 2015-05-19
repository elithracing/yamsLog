
cmake_minimum_required(VERSION 2.8)

# Turn on to see cmake output
set(CMAKE_VERBOSE_MAKEFILE off)

project(yamsLogd)

# Use c++11
set(CMAKE_CXX_STANDARD 11)
# Static linking
set(BUILD_SHARED_LIBS OFF)
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/cmake-modules")
set(BOOST_MIN_VERSION "1.54")

if (CYGWIN)
    add_definitions(-D_WIN32_WINNT=0x0601) # Target win7
endif ()
if (UNIX)
    set(CMAKE_CXX_FLAGS ${CMAKE_CXX_FLAGS} "-std=c++11")
endif ()

# Eclipse hack
find_path(STDIO_INCLUDE_PATH stdio.h)
include_directories("${STDIO_INCLUDE_PATH}/")

include_directories("${CMAKE_SOURCE_DIR}/src")

find_package(Librt REQUIRED)
include_directories(${LIBRT_INCLUDE_DIRS})

find_package (Threads REQUIRED)
include_directories(${THREADS_INCLUDE_DIRS})

find_package(Boost COMPONENTS system filesystem thread REQUIRED)
include_directories(${Boost_INCLUDE_DIRS})

find_package(Protobuf REQUIRED)
include_directories(${PROTOBUF_INCLUDE_DIRS})
# Path to generated protocol source files 
include_directories(${CMAKE_CURRENT_BINARY_DIR})
PROTOBUF_GENERATE_CPP(PROTO_SRCS PROTO_HDRS ${CMAKE_SOURCE_DIR}/../../protobuf/protocol.proto)
add_library(ProtobufProto ${PROTO_SRCS} ${PROTO_HDRS})

include_directories("${CMAKE_SOURCE_DIR}/external/muparser/include/")
file(GLOB_RECURSE MUPARSER_SRCS "${CMAKE_SOURCE_DIR}/external/muparser/src/**")
file(GLOB_RECURSE MUPARSER_HDRS "${CMAKE_SOURCE_DIR}/external/muparser/include/**")
add_library(muParser ${MUPARSER_SRCS} ${MUPARSER_HDRS})

set(CORE_FILES src/DataLogger.cpp src/DataLogger.h src/CommunicationServer.cpp src/CommunicationServer.h
    src/ClientConnection.cpp src/ClientConnection.h src/StatusObserver.cpp src/StatusObserver.h
    src/ClientCallbackImpl.cpp src/ClientCallbackImpl.h src/SensorDefinitionReader.cpp src/SensorDefinitionReader.h
    src/ModificationCallbackImpl.cpp src/ModificationCallbackImpl.h src/Experiment.cpp src/Experiment.h
    src/Project.cpp src/Project.h src/ProjectHandler.cpp src/ProjectHandler.h)
source_group(core FILES ${CORE_FILES})
add_library(core ${CORE_FILES})

set(SERVICES_FILES src/services/AbstractService.cpp src/services/AbstractService.h 
    src/services/CommunicationServerService.cpp src/services/CommunicationServerService.h
    src/services/KeyboardInputService.cpp src/services/KeyboardInputService.h src/services/ProtobufFileOutputService.cpp 
    src/services/ProtobufFileOutputService.h src/services/ProtobufTcpOutputService.cpp src/services/ProtobufTcpOutputService.h
    src/services/TextFileOutputService.cpp src/services/TextFileOutputService.h)
source_group(services FILES ${SERVICES_FILES})
add_library(services ${SERVICES_FILES})

set(SENSORLIBS_FILES src/sensors/CAN.cpp src/sensors/CAN.h src/sensors/MTComm.cpp src/sensors/MTComm.h
    src/sensors/SerialPortLib.cpp src/sensors/SerialPortLib.h src/sensors/XSensIMU.cpp src/sensors/XSensIMU.h)
source_group(sensorlibs FILES ${SENSORLIBS_FILES})
add_library(sensorlibs ${SENSORLIBS_FILES})

set(OBDIIELM_FILES src/sensors/OBDII-ELM327/OBDReader.cpp src/sensors/OBDII-ELM327/obdservicecommands.cpp src/sensors/OBDII-ELM327/obdconfigfile.cpp src/sensors/OBDII-ELM327/obdconvertfunctions.cpp src/sensors/OBDII-ELM327/obdrevconvertfunctions.cpp src/sensors/OBDII-ELM327/obdserial.cpp src/sensors/OBDII-ELM327/supportedcommands.cpp src/sensors/OBDII-ELM327/OBDReader.h src/sensors/OBDII-ELM327/obdconfigfile.h src/sensors/OBDII-ELM327/obdconvertfunctions.h src/sensors/OBDII-ELM327/obdserial.h src/sensors/OBDII-ELM327/obdservicecommands.h src/sensors/OBDII-ELM327/supportedcommands.cpp)
source_group(OBDIIElm FILES ${OBDIIELM_FILES})
add_library(OBDIIElm ${OBDIIELM_FILES})

set(SENSORS_FILES src/sensors/AbstractSensor.cpp src/sensors/AbstractSensor.h src/sensors/ERDataLogger.cpp
    src/sensors/ERDataLogger.h src/sensors/GpsSensor.cpp src/sensors/GpsSensor.h src/sensors/ImuSensor.cpp 
    src/sensors/ImuSensor.h src/sensors/VirtualSensor.cpp src/sensors/VirtualSensor.h src/sensors/OBDIIElmSensor.cpp 
    src/sensors/OBDIIElmSensor.h src/sensors/CorrsysSensor.cpp src/sensors/CorrsysSensor.h src/sensors/CanSensor.cpp
    src/sensors/CanSensor.h)
source_group(sensors FILES ${SENSORS_FILES})
add_library(sensors ${SENSORS_FILES})

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

target_link_libraries(yamsLogd core services sensors sensorlibs OBDIIElm
    muParser ProtobufProto ${PROTOBUF_LIBRARIES}
  ${Boost_LIBRARIES} ${THREADS_LIBRARIES} ${LIBRT_LIBRARIES})


target_link_libraries(OBDtool OBDIIElm)
