//
//  PlotPageViewController.m
//  CDCV2
//
//  Created by Erik Frisk on 03/09/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "PlotPageViewController.h"
#import "GraphPlotterViewController.h"

@interface PlotPageViewController () <UIPageViewControllerDataSource, UIPageViewControllerDelegate>
@property (nonatomic, strong) NSMutableArray *activeViewControllers;
@property (nonatomic) int pageDirection;
@property (nonatomic) BOOL plotPages;
@property (nonatomic, weak) UIViewController *backdropController;

@end

@implementation PlotPageViewController

#pragma mark - Properties
-(UIViewController *)backdropController
{
  if (!_backdropController) _backdropController = [self.storyboard instantiateViewControllerWithIdentifier:@"NoPlotBackdropView"];
  return _backdropController;
}

-(NSMutableArray *)activeViewControllers
{
  if (!_activeViewControllers) _activeViewControllers = [[NSMutableArray alloc] init];
  
  return _activeViewControllers;
}

#pragma mark - Initialization
- (void)viewDidLoad
{
  [super viewDidLoad];
  [self setup];
}

-(void)setup
{
  self.dataSource = self;
  self.delegate = self;
  
  // Start with the backdrop
  self.activeViewControllers = [@[self.backdropController] mutableCopy];
  self.plotPages = NO;
  self.pageControl.hidesForSinglePage = YES;
  
  // Initialize pageviewcontroller with first page
  [self setViewControllers:@[self.activeViewControllers[0]] direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
  
  self.pageControl.numberOfPages = [self.activeViewControllers count];
}

#pragma mark - Add and delete plots

-(GraphPlotterViewController *)newPlot
{
  return [[GraphPlotterViewController alloc] init];
}

-(void)addAPlot
{
  if (!self.plotPages) {
    // Remove the "no plots" backdrop from set of active viewcontrollers
    [self.activeViewControllers removeAllObjects];
    self.pageControl.currentPage = 0;
  }
  
  [self.activeViewControllers addObject:[self newPlot]];
  
  // Configure pageControl
  self.pageControl.numberOfPages = [self.activeViewControllers count];
  self.pageControl.hidesForSinglePage = NO;
  
  if (!self.plotPages) {
    // Set first page to be the active page
    [self setViewControllers:@[self.activeViewControllers[0]] direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
    self.pageControl.currentPage = 0;
  } else {
    [self setViewControllers:@[[self.activeViewControllers lastObject]] direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
    self.pageControl.currentPage = [self.activeViewControllers count]-1;
  }

  // (possibly) handle bug with scroll transition
  self.dataSource = nil;
  self.dataSource = self;

  [self.pageControl updateCurrentPageDisplay];
     self.pageControl.numberOfPages = [self.activeViewControllers count];
  self.plotPages = YES;
}

-(void)deleteCurrentPlot
{
  if (self.plotPages) {
    // Remove current viewController from list of active viewControllers
    [self.activeViewControllers removeObjectAtIndex:self.pageControl.currentPage];
    
    // Did we remove the last viewController?
    if ([self.activeViewControllers count]==0) {
      // Add the backdrop viewController to the set of acrtive viewControllers
      self.activeViewControllers = [@[self.backdropController] mutableCopy];
      self.pageControl.hidesForSinglePage = YES;
      self.plotPages = NO;
    }
    
    // Update current page
    if (self.pageControl.currentPage > 0) {
      // If we're not at the first page, take one step back. Otherwise, stand still.
      self.pageControl.currentPage = self.pageControl.currentPage -1;
    }

    // Configure pageControl
    self.pageControl.numberOfPages = [self.activeViewControllers count];
    
    // Set new visible viewController
    [self setViewControllers:@[self.activeViewControllers[self.pageControl.currentPage]] direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
  }
}


#pragma mark - Navigation
-(UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerAfterViewController:(UIViewController *)viewController
{
  UIViewController *ret = nil;
  NSUInteger idx = [self.activeViewControllers indexOfObject:viewController];
  if (idx != NSNotFound && idx+1 < [self.activeViewControllers count]) {
    ret = self.activeViewControllers[idx+1];
  }
  return ret;
}

-(UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerBeforeViewController:(UIViewController *)viewController
{
  UIViewController *ret = nil;
  NSUInteger idx = [self.activeViewControllers indexOfObject:viewController];
  if (idx != NSNotFound && (int)idx-1 >= 0) {
    ret = self.activeViewControllers[idx-1];
  }
  return ret;
}

-(void)pageViewController:(UIPageViewController *)pageViewController willTransitionToViewControllers:(NSArray *)pendingViewControllers
{
  self.pageDirection = [self.activeViewControllers indexOfObject:[pendingViewControllers firstObject]] - self.pageControl.currentPage;
}

-(void)pageViewController:(UIPageViewController *)pageViewController didFinishAnimating:(BOOL)finished previousViewControllers:(NSArray *)previousViewControllers transitionCompleted:(BOOL)completed
{
  if (completed) {
    self.pageControl.currentPage += self.pageDirection;
  }
}
@end
