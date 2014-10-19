//
//  CircularDataArray.m
//  GraphPlotter
//
//  Created by Erik Frisk on 23/08/14.
//  Copyright (c) 2014 Erik Frisk. All rights reserved.
//

#import "CircularDataArray.h"

@interface CircularDataArray()
@property NSMutableArray *data; // of DataPoint
@property (nonatomic) int  currentIndex;
@property (nonatomic) BOOL bufferFull;
@property (nonatomic) int  size;
@end

@implementation CircularDataArray

-(instancetype)initWithBufferSize:(int)bufferSize
{
  self = [super init];
  
  if( self ) {
    // Allocate data buffers for data
    self.data = [[NSMutableArray alloc] initWithCapacity:bufferSize];
    
    self.currentIndex = -1;
    self.bufferFull = NO;
    self.size = bufferSize;
  }
  return self;
}

-(void)addDataPoint:(DataPoint *)dataPoint
{
  self.currentIndex++;
  if (self.currentIndex>=self.size) {
    self.currentIndex = 0;
    self.bufferFull = YES;
  }
  self.data[self.currentIndex] = dataPoint;
}

-(DataPoint *)valueAtIndexFromEnd:(int)index
{
  DataPoint *ret = nil;
  
  if ( index < self.currentIndex || self.bufferFull ) {
    int idx = self.currentIndex - index;
    if (idx < 0 && self.bufferFull) idx += self.size;
    
    ret = self.data[idx];
  }
  
  return ret;
}


@end
