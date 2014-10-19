//
//  PlotPageViewController.h
//  CDCV2
//
//  Created by Erik Frisk on 03/09/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PlotPageViewController : UIPageViewController
@property (nonatomic, weak) UIPageControl *pageControl;

-(void)addAPlot;
-(void)deleteCurrentPlot;
@end
