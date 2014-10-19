//
//  ShowProjectsTableViewController.h
//  CDCV2
//
//  Created by Erik Frisk on 26/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CommunicationCentral.h"
#import "ConnectionClient.h"
@interface ShowProjectsTableViewController : UITableViewController
@property (nonatomic, weak) ConnectionClient *connectionClient;
@property (nonatomic, weak) CommunicationCentral *communicationCentral;
@end
