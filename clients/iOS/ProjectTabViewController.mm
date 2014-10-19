//
//  ProjectTabViewController.m
//  CDCV2
//
//  Created by Erik Frisk on 30/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "ProjectTabViewController.h"
#import "SensorsViewController.h"
#import "PlotViewController.h"

@interface ProjectTabViewController ()

@end

@implementation ProjectTabViewController

- (void)viewDidLoad
{
  [super viewDidLoad];

  SensorsViewController *svc = (SensorsViewController *)self.viewControllers[0];
  svc.communicationCentral = self.communicationCentral;
  svc.connectionClient = self.connectionClient;


  PlotViewController *pvc = (PlotViewController *)self.viewControllers[2];
  pvc.connectionClient = self.connectionClient;
}

@end
