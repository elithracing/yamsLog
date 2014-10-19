//
//  CDMiniClientViewController.m
//  iOSMiniClient
//
//  Created by Erik Frisk on 26/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "CDMiniClientViewController.h"
#import "GCDAsyncSocket.h"
#import "ConnectionClient.h"
#import "CommunicationCentral.h"
#import "ShowProjectsTableViewController.h"

@interface CDMiniClientViewController () <UITextFieldDelegate>
@property (weak, nonatomic) IBOutlet UIButton *disconnectButton;
@property (weak, nonatomic) IBOutlet UIButton *connectButton;
@property (weak, nonatomic) IBOutlet UITextField *serverAddressTextField;

@property (nonatomic, strong) CommunicationCentral *communicationCentral;
@property (nonatomic, strong) ConnectionClient *connectionClient;

@property (nonatomic, strong) id connectionObserver;
@end

#define CDCV2_SERVER_PORT 2001

@implementation CDMiniClientViewController

#pragma mark - properties
-(CommunicationCentral *)communicationCentral
{
  if (!_communicationCentral) _communicationCentral = [[CommunicationCentral alloc] init];
  
  return _communicationCentral;
}

-(ConnectionClient *)connectionClient
{
  if (!_connectionClient) {
    _connectionClient = [[ConnectionClient alloc] init];
  }
  
  return _connectionClient;
}

#pragma mark - Initialization and controller life cycle

- (void)viewDidLoad
{
  [super viewDidLoad];
  
  self.serverAddressTextField.delegate = self;

  self.communicationCentral.delegate = self.connectionClient;
  
  NSString *serverAdress = [[NSUserDefaults standardUserDefaults] objectForKey:@"server address"];
  if (serverAdress) {
    self.serverAddressTextField.text = serverAdress;
  }
  self.connectButton.enabled = !self.communicationCentral.isConnected;
  self.disconnectButton.enabled = self.communicationCentral.isConnected;
}

-(void)viewWillAppear:(BOOL)animated
{
  [super viewWillAppear:animated];
  NSOperationQueue *mainQueue = [NSOperationQueue mainQueue];
  self.connectionObserver = [[NSNotificationCenter defaultCenter]
                             addObserverForName:@"connection"
                                         object:self.communicationCentral queue:mainQueue
                                     usingBlock:^(NSNotification *note)
  {
    if ([[[note userInfo] objectForKey:@"connect"] isEqualToNumber:[NSNumber numberWithInt:0]]) {
      self.connectButton.enabled = YES;
      self.disconnectButton.enabled = NO;
    } else {
      self.connectButton.enabled = NO;
      self.disconnectButton.enabled = YES;
      
      [self performSegueWithIdentifier:@"Show projects" sender:self];
      
    }
    if ([[[note userInfo] objectForKey:@"failed connect"] isEqualToNumber:[NSNumber numberWithInt:1]]) {
      NSLog(@"Connection failed");
    }
  }];
}

-(void)viewWillDisappear:(BOOL)animated
{
  [[NSNotificationCenter defaultCenter] removeObserver:self.connectionObserver];
  [super viewWillDisappear:animated];
}

#pragma mark - User interaction
- (IBAction)connectButtonPressed:(UIButton *)sender
{
  [self connectToServer];
}

- (IBAction)disconnectButtonPressed:(UIButton *)sender
{
  [self.communicationCentral disconnect];
}

-(BOOL)connectToServer
{
  if (![self.communicationCentral connectToServer:self.serverAddressTextField.text onPort:CDCV2_SERVER_PORT]) {
    return NO;
  }
  
  [[NSUserDefaults standardUserDefaults] setObject:self.serverAddressTextField.text forKey:@"server address"];
  [[NSUserDefaults standardUserDefaults] synchronize];

  return YES;
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
  return [textField resignFirstResponder];
}
#pragma mark - Navigation
-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
  if ([segue.destinationViewController isKindOfClass:[ShowProjectsTableViewController class]]) {
    ShowProjectsTableViewController *sptvc = segue.destinationViewController;
    sptvc.connectionClient = self.connectionClient;
    sptvc.communicationCentral = self.communicationCentral;
  }
}

@end
