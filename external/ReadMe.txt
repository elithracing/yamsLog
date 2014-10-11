This directory holds, or rather will hold any external packages that
are used together with this software.

The "libs" dir contains 
 * ProtobufJava.jar  
   -- Would be much happier if this was from the platform protobuf installation
      but for now this is packaged with this code
 * android-support-v4.jar 
  -- It seems like common practise is to copy these libs from the
     downloaded android SDK. I'd much rather declare it to use that
     version directly instead, if possible to set up with eclipse

