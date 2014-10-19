//
//  CommunicationCentralDelegate.h
//  CDCV2
//
//  Created by Erik Frisk on 27/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CommunicationCentral.h"

#include "protocol.pb.h"
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl.h>

@protocol CommunicationCentralDelegate <NSObject>
@required -(void)CommmunicationCentral:(CommunicationCentral *)central didReadMessage:(protobuf::GeneralMsg)msg;
@end
