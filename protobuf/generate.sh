#!/bin/bash

DIR=`dirname $0`
protoc --java_out=$DIR/gen/java --cpp_out=$DIR/gen/cpp -I$DIR $DIR/protocol.proto

rm -rf gen/classes
mkdir gen/classes

javac -target 1.6 -source 1.6 gen/java/protobuf/Protocol.java  -classpath ../external/libs/ProtobufJava.jar -d gen/classes

cd gen/classes/

jar -cf ../Protocol.jar protobuf
cd ../..
rm -rf gen/classes


