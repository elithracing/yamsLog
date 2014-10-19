//
//  DataPoint.h
//  CDCV2
//
//  Created by Erik Frisk on 01/09/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DataPoint : NSObject
@property (nonatomic) double t;
@property (nonatomic) int sensorId;
@property (nonatomic, strong) NSMutableArray *attributeValues; // of NSNumber
@end
