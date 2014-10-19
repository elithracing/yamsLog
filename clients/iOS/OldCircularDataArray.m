//
//  CircularDataArray.m
//  GraphPlotter
//
//  Created by Erik Frisk on 23/08/14.
//  Copyright (c) 2014 Erik Frisk. All rights reserved.
//

#import "OldCircularDataArray.h"

@interface OldCircularDataArray()
@property NSArray *data; // of NSArray
@property (nonatomic) int  currentIndex;
@property (nonatomic) BOOL bufferFull;
@property (nonatomic) int  size;
@end

@implementation OldCircularDataArray

-(instancetype)initWithNSignal:(int)numberOfSignals withBufferSize:(int)bufferSize
{
  self = [super init];
  
  if( self ) {
    // Allocate data buffers for data
    NSMutableArray *data = [[NSMutableArray alloc] initWithCapacity:numberOfSignals];
    for (int i=0; i < numberOfSignals; i++) {
      data[i] = [[NSMutableArray alloc] initWithCapacity:bufferSize];
    }
    self.data = (NSArray *)data;
    
    self.currentIndex = -1;
    self.bufferFull = NO;
    self.size = bufferSize;
  }
  return self;
}

-(void)addDataPoint:(double *)data
{
  self.currentIndex++;
  if (self.currentIndex>=self.size) {
    self.currentIndex = 0;
    self.bufferFull = YES;
  }
  for (int i=0; i < [self.data count]; i++) {
    NSMutableArray *signalArray = (NSMutableArray *)self.data[i];
    signalArray[self.currentIndex] = [NSNumber numberWithDouble:data[i]];
  }
}

-(BOOL)valueAtIndexFromEnd:(int)index values:(double *)values
{
  BOOL ret = NO;
  
  if ( index < self.currentIndex || self.bufferFull ) {
    int idx = self.currentIndex - index;
    if (idx < 0 && self.bufferFull) idx += self.size;
    for (int i=0; i < [self.data count]; i++) {
      values[i] = [[self.data[i] objectAtIndex:idx] doubleValue];
    }
    ret = YES;
  }
  
  return ret;
}


@end
