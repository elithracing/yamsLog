//
//  ShowSensorInformationViewController.h
//  CDCV2
//
//  Created by Erik Frisk on 30/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ConnectionClient.h"
#import "SensorConfiguration.h"

@protocol ShowSensorInformationViewControllerDelegate <NSObject>
@required -(void)showSensorDone;
@end

@interface ShowSensorInformationViewController : UIViewController
@property (nonatomic, weak) ConnectionClient *connectionClient;
@property (nonatomic, strong) SensorConfiguration *sensorConfiguration;
@property (nonatomic, weak) id<ShowSensorInformationViewControllerDelegate> delegate;
@end
