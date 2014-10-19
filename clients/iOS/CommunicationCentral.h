//
//  CommunicationCentral.h
//  CDCV2
//
//  Created by Erik Frisk on 27/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <Foundation/Foundation.h>
#include "protocol.pb.h"
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl.h>

@protocol CommunicationCentralDelegate;

@interface CommunicationCentral : NSObject
@property (nonatomic,getter = isConnected, readonly) BOOL connected;
@property (nonatomic, strong) id<CommunicationCentralDelegate> delegate;

-(BOOL)connectToServer:(NSString *)address onPort:(uint16_t)port;
-(void)disconnect;
-(void)asyncStartListening;
-(BOOL)sendMessage:(protobuf::GeneralMsg)message;
@end
