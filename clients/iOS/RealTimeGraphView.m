//
//  RealTimeGraphView.m
//  GraphPlotter
//
//  Created by Erik Frisk on 18/08/14.
//  Copyright (c) 2014 Erik Frisk. All rights reserved.
//

#import "RealTimeGraphView.h"
#import "RealTimeGraphViewDataSource.h"

@interface RealTimeGraphView ()
@property (nonatomic) int lastNumberOfPoints;
@end


@implementation RealTimeGraphView

#pragma mark - Class methods
+(NSArray *)colors
{
  return @[[UIColor blueColor], [UIColor redColor], [UIColor blackColor], [UIColor purpleColor], [UIColor magentaColor]];
}


#pragma mark - Initialization and setup
-(void)awakeFromNib
{
  [self setup];
}

- (id)initWithFrame:(CGRect)frame
{
  self = [super initWithFrame:frame];
  
  if (self) {
    [self setup];
    if (self.quality<=0) {
      self.quality = 1.0;
    }
    self.xTicks = NO;
    self.lineWidth = 1.0;
    [self setNeedsDisplay];
  }
  return self;
}

-(void)setup
{
  self.backgroundColor = [UIColor whiteColor];
}


#pragma mark - Draw methods
-(void)redraw
{
  [self setNeedsDisplay];
}

- (void)drawRect:(CGRect)rect
{
//  NSLog(@"drawRect in view %@", self);
  UIBezierPath *box = [UIBezierPath bezierPathWithRect:self.bounds];
  box.lineWidth = 2.0;
  [box stroke];
  
  CGContextRef context = UIGraphicsGetCurrentContext();
  
  [self drawAxisInContext:context];
  [self drawGraphInContext:context];
}

-(CGPoint)viewPointFromDataPointAtTime:(double)t withValue:(double)y
{
  AxisLimits *l = [self.dataSource axisLimitsForRealTimeGraphView:self];
  
  double cgx = (t-l.xmin)/(l.xmax - l.xmin)*0.9*self.bounds.size.width + self.bounds.size.width*0.05;
  double cgy = self.bounds.size.height - ((y-l.ymin)/(l.ymax - l.ymin)*0.9*self.bounds.size.height + self.bounds.size.height*0.05);

  return CGPointMake(cgx, cgy);
}

-(CGPoint)viewDistanceToDataDistance:(CGPoint)delta
{
  AxisLimits *l = [self.dataSource axisLimitsForRealTimeGraphView:self];
  return CGPointMake(delta.x/0.9/self.bounds.size.width*(l.xmax-l.xmin),delta.y/0.9/self.bounds.size.height*(l.ymax-l.ymin));
}

#pragma mark - Draw plots
-(void)drawGraphInContext:(CGContextRef)context
{
  CGContextSaveGState(context);
  
  int maxNumberOfPoints = 0;
  AxisLimits *l = [self.dataSource axisLimitsForRealTimeGraphView:self];

  for (int i=0; i < [self.dataSource numberOfPlotsinRealTimeGraphView:self]; i++) {
    BOOL pointsAvailable = YES;
    int idx = 0;
    double t=0.0;
    double y=0.0;
    CGPoint lastPoint;

    // Search for a startpoint with time less than xmax
    BOOL firstPointSearchFinished = NO;
    BOOL foundFirstPoint = NO;
    while (!firstPointSearchFinished) {
      if( [self.dataSource realTimeGraphView:self plot:i valueAtIndexFromEnd:idx time:&t value:&y] ) {
        idx++;
        if (t<=l.xmax) {
          firstPointSearchFinished = YES;
          foundFirstPoint = YES;
        }
      } else {
        firstPointSearchFinished = YES;
      }
    }

    if (foundFirstPoint) {
      self.lastNumberOfPoints=1;
      CGPoint p = [self viewPointFromDataPointAtTime:t withValue:y];
      lastPoint = p;
      CGContextMoveToPoint(context, p.x, p.y);
    } else {
      pointsAvailable = NO;
    }
    
    // Step back into data source until time less than xmin or no more data
    while (pointsAvailable) {
      pointsAvailable = [self.dataSource realTimeGraphView:self plot:i valueAtIndexFromEnd:idx++ time:&t value:&y];
      
      if (t<l.xmin) {
        pointsAvailable = NO;
      }
      if (pointsAvailable ) {
        CGPoint p = [self viewPointFromDataPointAtTime:t withValue:y];
        if ([RealTimeGraphView drawPoint:&p whenLastPoint:&lastPoint withQuality:self.quality]) {
          CGContextAddLineToPoint(context, p.x, p.y);
          lastPoint = p;
          self.lastNumberOfPoints++;
        }
      }
      if (self.lastNumberOfPoints>maxNumberOfPoints) {
        maxNumberOfPoints = self.lastNumberOfPoints;
      }
    }
    UIColor *plotColor = [[RealTimeGraphView colors] objectAtIndex:i % [[RealTimeGraphView colors] count]];
    CGContextSetStrokeColorWithColor(context, plotColor.CGColor);
    CGContextSetLineWidth(context, self.lineWidth);
    CGContextStrokePath(context);
  }
  [[NSString stringWithFormat:@"%.1f sec (%d points)", l.xmax-l.xmin, maxNumberOfPoints] drawAtPoint:CGPointMake(20,10) withAttributes:nil];
  
  CGContextRestoreGState(context);
}

#pragma mark - Quality control
#define PLOT_QUALITY_CONSTANT 15
+(BOOL)drawPoint:(CGPoint *)p whenLastPoint:(CGPoint *)lastPoint withQuality:(double)quality
{
  return [RealTimeGraphView distanceBetweenPoint:p and:lastPoint]>(1-quality)*PLOT_QUALITY_CONSTANT;
}

+(double)distanceBetweenPoint:(CGPoint *)p1 and:(CGPoint *)p2
{
  CGPoint delta = CGPointMake((*p1).x-(*p2).x, (*p1).y-(*p2).y);
  
  return sqrt(delta.x*delta.x + delta.y*delta.y);
}

#pragma mark - Draw axis
-(void)drawAxisInContext:(CGContextRef)context
{
  CGContextSaveGState(context);
  
  AxisLimits *l = [self.dataSource axisLimitsForRealTimeGraphView:self];
  
  if (0.0 >= l.ymin && 0<=l.ymax ) {
    CGPoint p1 = [self viewPointFromDataPointAtTime:0.0 withValue:0.0];
    p1.x = self.bounds.size.width*0.05;
    CGPoint p2 = p1;
    p2.x = self.bounds.size.width*0.95;
    CGContextMoveToPoint(context, p1.x, p1.y);
    CGContextAddLineToPoint(context, p2.x, p2.y);
    CGContextStrokePath(context);

    CGFloat arrowDelta = 0.02*self.bounds.size.height;
    UIBezierPath *arrowHead = [[UIBezierPath alloc] init];
    [arrowHead moveToPoint:CGPointMake(p2.x + arrowDelta, p2.y)];
    [arrowHead addLineToPoint:CGPointMake(p2.x, p2.y-arrowDelta)];
    [arrowHead addLineToPoint:CGPointMake(p2.x, p2.y+arrowDelta)];
    [arrowHead closePath];
    [[UIColor blackColor] setFill];
    [arrowHead fill];
  }
  
  if (self.xTicks) {
    for (int x=(int)l.xmin; x<=(int)(l.xmax + 0.5); x++) {
      CGPoint p1 = [self viewPointFromDataPointAtTime:(double)x withValue:0.0];
      CGPoint p2 = p1;
      p1.y += 0.015*self.bounds.size.height;
      p2.y -= 0.015*self.bounds.size.height;
      CGContextMoveToPoint(context, p1.x, p1.y);
      CGContextAddLineToPoint(context, p2.x, p2.y);
      CGContextStrokePath(context);
    }
  }
  
//  if (0.0 >= l.xmin && 0<=l.xmax ) {
//    CGPoint p1 = [self viewPointFromDataPointAtTime:0.0 withValue:0.0];
//    p1.y = self.bounds.size.height*0.05;
//    CGPoint p2 = p1;
//    p2.y = self.bounds.size.height*0.95;
//    CGContextMoveToPoint(context, p1.x, p1.y);
//    CGContextAddLineToPoint(context, p2.x, p2.y);
//    CGContextStrokePath(context);
//  }

  CGContextRestoreGState(context);
}

@end
