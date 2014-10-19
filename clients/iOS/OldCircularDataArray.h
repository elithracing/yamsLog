//
//  CircularDataArray.h
//  GraphPlotter
//
//  Created by Erik Frisk on 23/08/14.
//  Copyright (c) 2014 Erik Frisk. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface OldCircularDataArray : NSObject

// designated initializer
-(instancetype)initWithNSignal:(int)numberOfSignals withBufferSize:(int)bufferSize;

-(void)addDataPoint:(double *)data;
-(BOOL)valueAtIndexFromEnd:(int)index values:(double *)values;
@end
