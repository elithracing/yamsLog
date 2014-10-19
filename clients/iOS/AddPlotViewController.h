//
//  AddPlotViewController.h
//  CDCV2
//
//  Created by Erik Frisk on 02/09/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ConnectionClient.h"

@protocol AddPlotViewControllerDelegate <NSObject>

@required -(void)addPlot:(NSDictionary *)newPlotDescription;

@end

@interface AddPlotViewController : UIViewController
@property (nonatomic, weak) ConnectionClient *connectionClient;
@property (nonatomic, weak) id<AddPlotViewControllerDelegate> delegate;
@end
