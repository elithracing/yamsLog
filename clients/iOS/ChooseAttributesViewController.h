//
//  ChooseAttributesViewController.h
//  CDCV2
//
//  Created by Erik Frisk on 02/09/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SensorConfiguration.h"

@interface ChooseAttributesViewController : UIViewController
@property (nonatomic, weak) SensorConfiguration *sensor;
@property (nonatomic, weak) NSMutableArray *chosenAttributes;
@end
