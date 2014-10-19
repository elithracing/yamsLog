//
//  SensorConfiguration.h
//  CDCV2
//
//  Created by Erik Frisk on 30/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SensorConfiguration : NSObject
@property (nonatomic) int sensorId;
@property (nonatomic) int maxAttributes;
@property (nonatomic, strong) NSString *name;
@property (nonatomic, strong) NSString *status;
@property (nonatomic, strong) NSArray  *attributes;
@end
