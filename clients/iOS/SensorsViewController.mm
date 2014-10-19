//
//  SensorsViewController.m
//  CDCV2
//
//  Created by Erik Frisk on 29/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "SensorsViewController.h"
#import "SensorConfiguration.h"
#import "ShowSensorInformationViewController.h"

@interface SensorsViewController () <UITableViewDataSource,UITableViewDelegate,ShowSensorInformationViewControllerDelegate>
@property (weak, nonatomic) IBOutlet UITableView *sensorTableView;
@property (nonatomic, strong) id messageObserver;
@end

@implementation SensorsViewController


- (void)viewDidLoad
{
  [super viewDidLoad];
  
  self.sensorTableView.dataSource = self;
  self.sensorTableView.delegate = self;
  
//  self.debugLabel.text = [NSString stringWithFormat:@"Server has %d sensors configured", [self.connectionClient.sensors count]];
}

-(void)viewWillAppear:(BOOL)animated
{
  [super viewWillAppear:animated];
  NSOperationQueue *mainQueue = [NSOperationQueue mainQueue];
  self.messageObserver = [[NSNotificationCenter defaultCenter]
                          addObserverForName:@"Project list updated"
                          object:self.connectionClient
                          queue:mainQueue
                          usingBlock:^(NSNotification *note) {
                            [self.sensorTableView reloadData];
                          }];
}

-(void)viewWillDisappear:(BOOL)animated
{
  [[NSNotificationCenter defaultCenter] removeObserver:self.messageObserver];
  [super viewWillDisappear:animated];
}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
  [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
  return (NSInteger)[self.connectionClient.sensors count];
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
  UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Sensor Configuration Cell" forIndexPath:indexPath];
  SensorConfiguration *sensorConfig = (SensorConfiguration *)self.connectionClient.sensors[indexPath.row];
  cell.textLabel.text = sensorConfig.name;
  
  if ([sensorConfig.status isEqualToString:@"Working"]) {
    cell.detailTextLabel.attributedText = [[NSAttributedString alloc] initWithString:sensorConfig.status attributes:@{NSForegroundColorAttributeName: [UIColor greenColor]}];
  } else {
    cell.detailTextLabel.attributedText = [[NSAttributedString alloc] initWithString:sensorConfig.status attributes:@{NSForegroundColorAttributeName: [UIColor redColor]}];
  }
  
  return cell;
  
}

-(void)showSensorDone
{
  [self dismissViewControllerAnimated:YES completion: nil];
}

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
  if ([segue.destinationViewController isKindOfClass:[ShowSensorInformationViewController class]]) {
    ShowSensorInformationViewController *ssivc = (ShowSensorInformationViewController *)segue.destinationViewController;
    ssivc.connectionClient = self.connectionClient;
    ssivc.sensorConfiguration = (SensorConfiguration *)self.connectionClient.sensors[[self.sensorTableView indexPathForSelectedRow].row];
    ssivc.delegate = self;
  }
}

@end
