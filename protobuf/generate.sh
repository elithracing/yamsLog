#!/bin/bash

DIR=`dirname $0`
protoc --java_out=$DIR/gen/java --cpp_out=$DIR/gen/cpp -I$DIR $DIR/protocol.proto
