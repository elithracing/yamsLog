//
//  PlotViewController.m
//  CDCV2
//
//  Created by Erik Frisk on 02/09/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "PlotViewController.h"
#import "AddPlotViewController.h"
#import "PlotPageViewController.h"

@interface PlotViewController () <UIAlertViewDelegate,AddPlotViewControllerDelegate>
@property (weak, nonatomic) IBOutlet UIPageControl *plotIndicator;
@property (nonatomic, weak) PlotPageViewController *plotPageViewController;

@end

@implementation PlotViewController

- (void)viewDidLoad
{
  [super viewDidLoad];
  UIBarButtonItem *test = [[UIBarButtonItem alloc] initWithTitle:@"+" style:UIBarButtonItemStylePlain target:nil action:nil];
  
  self.navigationItem.rightBarButtonItem = test;
}

- (IBAction)deleteCurrentPlot:(UIButton *)sender
{
  UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Delete plot" message:@"Are you sure you want to delete current plot?" delegate:self cancelButtonTitle:@"Cancel" otherButtonTitles:@"Delete", nil];
  [alert show];
}

-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
  if ([segue.destinationViewController isKindOfClass:[AddPlotViewController class]]) {
    AddPlotViewController *apvc = (AddPlotViewController *)segue.destinationViewController;
    apvc.connectionClient = self.connectionClient;
    apvc.delegate = self;
  } else if ([segue.destinationViewController isKindOfClass:[PlotPageViewController class]]) {
    self.plotPageViewController = (PlotPageViewController *)segue.destinationViewController;
    self.plotPageViewController.pageControl = self.plotIndicator;
  }
}

-(void)addPlot:(NSDictionary *)newPlotDescription
{
  [self.plotPageViewController addAPlot];
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
  if (buttonIndex==1) {
    [self.plotPageViewController deleteCurrentPlot];
  }
}

@end
