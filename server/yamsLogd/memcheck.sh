#!/bin/bash

DIR=`dirname $0`
# Memory check with valgrind. Some threads, from e.g. boost::thread, may be marked as "Still reachable", even though the memory is freed.
valgrind --tool=memcheck --leak-check=full $DIR/yasmLogd $1 $2
