//
//  AddPlotViewController.m
//  CDCV2
//
//  Created by Erik Frisk on 02/09/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "AddPlotViewController.h"
#import "SensorConfiguration.h"
#import "ChooseAttributesViewController.h"

@interface AddPlotViewController () <UITableViewDataSource>
@property (weak, nonatomic) IBOutlet UITableView *sensorTableView;
@property (nonatomic, strong) NSMutableDictionary *addedPlotConfiguration;
@end

@implementation AddPlotViewController

-(NSMutableDictionary *)addedPlotConfiguration
{
  if (!_addedPlotConfiguration) {
    _addedPlotConfiguration = [[NSMutableDictionary alloc] init];
    for( int i=0; i < [self.connectionClient.sensors count]; i++) {
      SensorConfiguration *sensor = self.connectionClient.sensors[i];
      [_addedPlotConfiguration setObject:[[NSMutableArray alloc] init] forKey:[NSNumber numberWithInt:sensor.sensorId]];
    }
  }
  
  return _addedPlotConfiguration;
}

- (IBAction)addPlotPressed:(UIBarButtonItem *)sender
{
  [self.delegate addPlot:self.addedPlotConfiguration];
  [self.navigationController popViewControllerAnimated:YES];
}

-(void)viewWillAppear:(BOOL)animated
{
  [super viewWillAppear:animated];
  [self.sensorTableView reloadData];
}

- (void)viewDidLoad
{
  [super viewDidLoad];
    // Do any additional setup after loading the view.
  
  self.sensorTableView.dataSource = self;
}


-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
  UITableViewCell *cell = [self.sensorTableView dequeueReusableCellWithIdentifier:@"Add Sensor Cell"];
  
  SensorConfiguration *sensor = (SensorConfiguration *)self.connectionClient.sensors[indexPath.row];
  cell.textLabel.text = sensor.name;
  
  int numberOfAttributesSelected = 0;
  NSArray *selectedAttributes = [self.addedPlotConfiguration objectForKey:[NSNumber numberWithInt:sensor.sensorId]];
  if (selectedAttributes) {
    numberOfAttributesSelected = [selectedAttributes count];
  }
  
  cell.detailTextLabel.text = [NSString stringWithFormat:@"%d attributes (%d selected)", sensor.maxAttributes, numberOfAttributesSelected];
  return cell;
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
  return [self.connectionClient.sensors count];
}

-(void)attributesChosen:(NSArray *)chosenAttributes forSensor:(SensorConfiguration *)sensor
{
  [self.addedPlotConfiguration setObject:chosenAttributes forKey:[NSNumber numberWithInt:sensor.sensorId]];
  [self.sensorTableView reloadData];
}

-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
  if( [segue.destinationViewController isKindOfClass:[ChooseAttributesViewController class]]) {
    ChooseAttributesViewController *cavc = (ChooseAttributesViewController *)segue.destinationViewController;
    SensorConfiguration *sensor = self.connectionClient.sensors[[self.sensorTableView indexPathForSelectedRow].row];
    cavc.sensor = sensor;
    cavc.chosenAttributes = [self.addedPlotConfiguration objectForKey:[NSNumber numberWithInt:sensor.sensorId]];
  }
}
@end
