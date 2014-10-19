//
//  RealTimeGraphView.h
//  GraphPlotter
//
//  Created by Erik Frisk on 18/08/14.
//  Copyright (c) 2014 Erik Frisk. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol RealTimeGraphViewDataSource;

@interface RealTimeGraphView : UIView

@property (nonatomic, weak) id <RealTimeGraphViewDataSource>dataSource;
@property (nonatomic) double quality;
@property (nonatomic) BOOL xTicks;
@property (nonatomic) CGFloat lineWidth;

-(CGPoint)viewDistanceToDataDistance:(CGPoint)delta;
-(void)redraw;
@end
