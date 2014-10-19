//
//  RealTimeGraphViewDataSource.h
//  GraphPlotter
//
//  Created by Erik Frisk on 18/08/14.
//  Copyright (c) 2014 Erik Frisk. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RealTimeGraphView.h"
#import "AxisLimits.h"

@protocol RealTimeGraphViewDataSource <NSObject>
@required -(BOOL)realTimeGraphView:(RealTimeGraphView *)view plot:(int)plot valueAtIndexFromEnd:(int)index time:(double *)t value:(double *)y;
@required -(AxisLimits *)axisLimitsForRealTimeGraphView:(RealTimeGraphView*)view;
@required -(int)numberOfPlotsinRealTimeGraphView:(RealTimeGraphView *)view;
@end
