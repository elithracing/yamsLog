//
//  PlotViewController.h
//  CDCV2
//
//  Created by Erik Frisk on 02/09/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ConnectionClient.h"

@interface PlotViewController : UIViewController
@property (nonatomic, weak) ConnectionClient *connectionClient;
@end
