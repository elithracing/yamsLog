//
//  CircularDataArray.h
//  GraphPlotter
//
//  Created by Erik Frisk on 23/08/14.
//  Copyright (c) 2014 Erik Frisk. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DataPoint.h"

@interface CircularDataArray : NSObject

// designated initializer
-(instancetype)initWithBufferSize:(int)bufferSize;

-(void)addDataPoint:(DataPoint *)data;
-(DataPoint *)valueAtIndexFromEnd:(int)index;
@end
