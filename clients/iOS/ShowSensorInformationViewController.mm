//
//  ShowSensorInformationViewController.m
//  CDCV2
//
//  Created by Erik Frisk on 30/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "ShowSensorInformationViewController.h"
#import "DataPoint.h"

@interface ShowSensorInformationViewController () <UITableViewDataSource,UITableViewDelegate>
@property (weak, nonatomic) IBOutlet UILabel *sensorNameLabel;
@property (weak, nonatomic) IBOutlet UITableView *sensorValueTable;
@property (weak, nonatomic) IBOutlet UILabel *maxAttributesLabel;
@property (weak, nonatomic) IBOutlet UILabel *statusLabel;
@property (weak, nonatomic) IBOutlet UILabel *timeLabel;
@end



@implementation ShowSensorInformationViewController

#define UPDATE_SENSOR_VALUES_INTERVAL 2
- (void)viewDidLoad
{
  [super viewDidLoad];

  self.sensorValueTable.dataSource = self;
  self.sensorValueTable.delegate = self;
  
  self.sensorNameLabel.text = self.sensorConfiguration.name;
  
  if ([self.sensorConfiguration.status isEqualToString:@"Working"]) {
    self.statusLabel.attributedText = [[NSAttributedString alloc] initWithString:self.sensorConfiguration.status attributes:@{NSForegroundColorAttributeName: [UIColor greenColor]}];
  } else {
    self.statusLabel.attributedText = [[NSAttributedString alloc] initWithString:self.sensorConfiguration.status attributes:@{NSForegroundColorAttributeName: [UIColor redColor]}];
  }

  self.maxAttributesLabel.text = [NSString stringWithFormat:@"%d", self.sensorConfiguration.maxAttributes];
  
  [self updateTimeLabel];

  [NSTimer scheduledTimerWithTimeInterval:UPDATE_SENSOR_VALUES_INTERVAL target:self selector:@selector(updateTimer:) userInfo:nil repeats:YES];
}

- (void)updateTimer:(NSTimer *)timer
{
  [self updateTimeLabel];
  [self.sensorValueTable reloadData];
}

-(void)tap:(UITapGestureRecognizer *)gesture
{
  if (gesture.state == UIGestureRecognizerStateEnded) {
    [self updateTimeLabel];
    [self.sensorValueTable reloadData];
  }
}

-(DataPoint *)lastDataPointForSensor:(int)sensorId
{
  return [self.connectionClient.lastData objectForKey:[NSNumber numberWithInt:self.sensorConfiguration.sensorId]];
}

-(void)updateTimeLabel
{
  self.timeLabel.text = [NSString stringWithFormat:@"%.2f", [self lastDataPointForSensor:self.sensorConfiguration.sensorId].t];
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
  [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
  int numberOfAttributes = self.sensorConfiguration.maxAttributes;
  return numberOfAttributes;
}

-(double)valueForAttribute:(int)attributeIndex
{
  double y = 0.0;
  DataPoint *p = [self lastDataPointForSensor:self.sensorConfiguration.sensorId];
  if (p && attributeIndex < [p.attributeValues count]) {
    y = [p.attributeValues[attributeIndex] doubleValue];
  }
  return y;
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
  UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Sensor Attribute Cell" forIndexPath:indexPath];

  int attributeNumber = indexPath.row;
  double y = [self valueForAttribute:attributeNumber];
  cell.textLabel.text = [NSString stringWithFormat:@"Attribute %d: %.3f", attributeNumber+1, y];
  
  return cell;
}

- (IBAction)okPressed:(UIButton *)sender
{
  [self.delegate showSensorDone];
}

@end
