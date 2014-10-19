//
//  SensorsViewController.h
//  CDCV2
//
//  Created by Erik Frisk on 29/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ConnectionClient.h"
#import "CommunicationCentral.h"

@interface SensorsViewController : UIViewController
@property (nonatomic, weak) ConnectionClient *connectionClient;
@property (nonatomic, weak) CommunicationCentral *communicationCentral;
@end
