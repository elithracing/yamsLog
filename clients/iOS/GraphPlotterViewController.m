//
//  GraphPlotterViewController.m
//  GraphPlotter
//
//  Created by Erik Frisk on 18/08/14.
//  Copyright (c) 2014 Erik Frisk. All rights reserved.
//

#import "GraphPlotterViewController.h"
#import "RealTimeGraphView.h"
#import "RealTimeGraphViewDataSource.h"
#import "OldCircularDataArray.h"

@interface GraphPlotterViewController () <RealTimeGraphViewDataSource>
@property (nonatomic, strong) RealTimeGraphView *rtGraphView;

@property (nonatomic) double currentTime;
@property (nonatomic) BOOL   pause;
@property (nonatomic) double panDelta;

@property (nonatomic) double ymin;
@property (nonatomic) double ymax;
@property (nonatomic) double plotWindow;

@property (nonatomic, strong) AxisLimits *plotRange;
@property (nonatomic, strong) OldCircularDataArray *circularDataArray;
@property (nonatomic) double *dataPoint; // area for storing a datapoint

@property (nonatomic) BOOL isInView;
@end

@implementation GraphPlotterViewController

#pragma mark - Properties

#define DATA_BUFFER_SIZE 1200 // 60 sec @ 50 Hz
#define NUMBER_OF_SIGNALS 2
-(OldCircularDataArray *)circularDataArray
{
  if( !_circularDataArray ) {
    _circularDataArray = [[OldCircularDataArray alloc] initWithNSignal:NUMBER_OF_SIGNALS+1 // +1 for time
                                                     withBufferSize:DATA_BUFFER_SIZE];
  }
  return _circularDataArray;
}

-(AxisLimits *)plotRange
{
  if (!_plotRange) _plotRange = [[AxisLimits alloc] init];
  
  return _plotRange;
}

-(RealTimeGraphView *)rtGraphView
{
  if (!_rtGraphView) {
    _rtGraphView = [[RealTimeGraphView alloc] initWithFrame:CGRectZero];
  }

  return _rtGraphView;
}

#pragma mark - Initialization and viewcontroller life cycle
#define SAMPLE_TIME 0.05
#define GRAPH_UPDATE_TIME 0.05
-(void)setup
{
  self.dataPoint = malloc((NUMBER_OF_SIGNALS + 1)*sizeof(double));
  
  // Initialize controller
  self.currentTime = 0.0;
  self.plotWindow = 10.0;
  self.ymin = 0.0;
  self.ymax = 0.0;
  self.panDelta = 0.0;
  
  
  self.isInView = YES;
  // Add and configure RealTimeGraphView
  //  self.rtGraphView.quality = 0.8;
  self.rtGraphView.dataSource = self;
  self.rtGraphView.xTicks = YES;
  self.rtGraphView.lineWidth = 1.5;
  [self.view addSubview:self.rtGraphView];
  
  // Add gestures
  UIPinchGestureRecognizer *pinchRecognizer = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(graphZoom:)];
  [self.rtGraphView addGestureRecognizer:pinchRecognizer];
  
  UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(pause:)];
  tapRecognizer.numberOfTapsRequired = 1;
  UITapGestureRecognizer *doubleTapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(resetView:)];
  doubleTapRecognizer.numberOfTouchesRequired = 2;

  [self.rtGraphView addGestureRecognizer:tapRecognizer];
  [self.rtGraphView addGestureRecognizer:doubleTapRecognizer];

//  UIPanGestureRecognizer *panRecognizer = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(pan:)];
//  [self.rtGraphView addGestureRecognizer:panRecognizer];

  // Setup data timer
  [NSTimer scheduledTimerWithTimeInterval:SAMPLE_TIME target:self selector:@selector(newData:) userInfo:nil repeats:YES];

  // Setup update timer
  [NSTimer scheduledTimerWithTimeInterval:GRAPH_UPDATE_TIME target:self selector:@selector(updateTimer:) userInfo:nil repeats:YES];
}

-(void)viewWillLayoutSubviews
{
  [super viewWillLayoutSubviews];
  CGFloat plotSize;
  if (self.view.bounds.size.width<self.view.bounds.size.height) {
    plotSize = self.view.bounds.size.width*0.9;
  } else {
    plotSize = self.view.bounds.size.height*0.9;
  }
  self.rtGraphView.frame = CGRectMake((self.view.bounds.size.width - plotSize)/2.0, (self.view.bounds.size.height-plotSize)/2.0,
                                      plotSize, plotSize);
}

-(void)viewDidLoad
{
  [super viewDidLoad];
  [self setup];
}

-(void)dealloc
{
  // De-allocate data point area
  if (self.dataPoint) {
    free( self.dataPoint );
  }
}

-(void)viewWillAppear:(BOOL)animated
{
  [super viewWillAppear:animated];
  self.isInView = YES;
}


-(void)viewWillDisappear:(BOOL)animated
{
  self.isInView = NO;
  [super viewWillDisappear:animated];
}



#pragma mark - User interaction
- (void)pause:(UITapGestureRecognizer *)gesture
{
  if (gesture.state == UIGestureRecognizerStateRecognized) {
    self.pause = !self.pause;
  }
}

- (void)resetView:(UITapGestureRecognizer *)gesture
{
  if (gesture.state == UIGestureRecognizerStateRecognized) {
    self.plotWindow = 10.0;
  }
}

#define MAX_ZOOM_OUT_RANGE 20.0 // seconds
#define MIN_ZOOM_IN_RANGE  1.0 // seconds
- (void)graphZoom:(UIPinchGestureRecognizer *)gesture
{
  if( gesture.state == UIGestureRecognizerStateChanged) {
    self.plotWindow = MIN(self.plotWindow / gesture.scale, MAX_ZOOM_OUT_RANGE/1.1);
    self.plotWindow = MAX(self.plotWindow, MIN_ZOOM_IN_RANGE/1.1);
    gesture.scale = 1.0;
  }
}

//-(void)pan:(UIPanGestureRecognizer *)gesture
//{
//  if (gesture.state == UIGestureRecognizerStateChanged) {
//    self.panDelta -= [self.rtGraphView viewDistanceToDataDistance:[gesture translationInView:self.rtGraphView]].x;
//    [gesture setTranslation:CGPointZero inView:self.rtGraphView];
//    
//    NSLog(@"DeltaT = %.2f s", self.panDelta);
//  }
//}

- (void)updateTimer:(NSTimer *)timer
{
  if (!self.pause && self.isInView) {
    [self.rtGraphView redraw];
  }
}

#pragma mark - New data

- (void)newData:(NSTimer *)timer
{
  self.currentTime += SAMPLE_TIME;

  // Compute new datapoint (t,y1,y2)
  double t = self.currentTime;
  
  double e  = (((float)(arc4random()%10000))/10000.0-0.5)*0.3;
  double y1 = sin(2*M_PI*1/3*self.currentTime) + e;
  
  e = (((float)(arc4random()%10000))/10000.0-0.5)*0.3;
  double y2 = sin(2*M_PI*1/2*self.currentTime) + e;

  // Add point to circularDataBuffer
  self.dataPoint[0] = t;
  self.dataPoint[1] = y1;
  self.dataPoint[2] = y2;
  [self.circularDataArray addDataPoint:self.dataPoint];
  
  // Update max/min-values
  self.ymin = MIN(self.ymin, y1);
  self.ymin = MIN(self.ymin, y2);

  self.ymax = MAX(self.ymax, y1);
  self.ymax = MAX(self.ymax, y2);
}

#pragma mark - RealTimeGraphView data source protocol

-(int)numberOfPlotsinRealTimeGraphView:(RealTimeGraphView *)view
{
  return NUMBER_OF_SIGNALS;
}

-(AxisLimits *)axisLimitsForRealTimeGraphView:(RealTimeGraphView *)view
{
  double xmin;
  double xmax;
  double ymin;
  double ymax;

  xmax = MAX(self.plotWindow, self.currentTime) + self.plotWindow*0.1;
  xmin = MAX(0.0, xmax-1.1*self.plotWindow);

  if (self.ymin < 0) {
    ymin = self.ymin * 1.2;
  } else {
    ymin = self.ymin * 0.8;
  }
  if (self.ymax > 0) {
    ymax = self.ymax * 1.2;
  } else {
    ymax = self.ymax * 0.8;
  }

  if (abs(ymax - ymin)<0.05) {
    ymax = (ymax + ymin)/2.0 + 0.025;
    ymin = ymax - 0.05;
  }
  
  self.plotRange.xmin = xmin;
  self.plotRange.xmax = xmax;
  self.plotRange.ymin = ymin;
  self.plotRange.ymax = ymax;
  return self.plotRange;
}

-(BOOL)realTimeGraphView:(RealTimeGraphView *)view plot:(int)plot valueAtIndexFromEnd:(int)index time:(double *)t value:(double *)y
{
  BOOL ret = NO;
  
  if( [self.circularDataArray valueAtIndexFromEnd:index values:self.dataPoint] ) {
    *t = self.dataPoint[0];
    *y = self.dataPoint[plot+1];
    ret = YES;
  }
  return ret;
}

@end
