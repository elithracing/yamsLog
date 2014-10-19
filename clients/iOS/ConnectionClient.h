//
//  ConnectionClient.h
//  CDCV2
//
//  Created by Erik Frisk on 28/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CommunicationCentralDelegate.h"
#import "CircularDataArray.h"
#include "protocol.pb.h"
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl.h>

typedef enum : NSUInteger {
  IDLE,
  DATA_COLLECTION,
  EXPERIMENT_PLAYBACK,
} ProjectStatus;

@interface ConnectionClient : NSObject <CommunicationCentralDelegate>
@property (nonatomic, strong, readonly) NSArray *projectNames;
@property (nonatomic, strong, readonly) NSArray *sensors; // of SensorConfiguration
@property (nonatomic, readonly) ProjectStatus projectStatus;
@property (nonatomic, strong, readonly) NSMutableDictionary *lastData;

-(BOOL)registerDataSink:(CircularDataArray *)data forSensor:(int)sensorId;
-(void)deregisterSensor:(int)sensorId;
-(void)CommmunicationCentral:(CommunicationCentral *)central didReadMessage:(protobuf::GeneralMsg)message;
@end
