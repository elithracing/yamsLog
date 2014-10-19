//
//  ConnectionClient.m
//  CDCV2
//
//  Created by Erik Frisk on 28/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "ConnectionClient.h"
#import "SensorConfiguration.h"
#import "DataPoint.h"
#import "CircularDataArray.h"

@interface ConnectionClient ()
@property (nonatomic, strong, readwrite) NSArray *projectNames;
@property (nonatomic, strong, readwrite) NSArray *sensors; // of SensorConfiguration
@property (nonatomic, readwrite) ProjectStatus projectStatus;
@property (nonatomic, strong, readwrite) NSMutableDictionary *lastData;
@property (nonatomic, strong) NSMutableDictionary *dataSinks; // Key: sensorId, Obj: CircularDataArray
@end

@implementation ConnectionClient

#pragma mark - Properties
-(NSMutableDictionary *)lastData
{
  if (!_lastData) _lastData = [[NSMutableDictionary alloc] init];
  
  return _lastData;
}

-(NSMutableDictionary *)dataSinks
{
  if (!_dataSinks) _dataSinks = [[NSMutableDictionary alloc] init];
  
  return _dataSinks;
}

#pragma mark - Manage data sinks
-(BOOL)registerDataSink:(CircularDataArray *)data forSensor:(int)sensorId
{
  BOOL ret = YES;
  
  if ([self.dataSinks objectForKey:[NSNumber numberWithInt:sensorId]]) {
    // Datasink already registred for the sensorId
    ret = NO;
  } else {
    [self.dataSinks setObject:data forKey:[NSNumber numberWithInt:sensorId]];
  }
  return ret;
}

-(void)deregisterSensor:(int)sensorId
{
  [self.dataSinks removeObjectForKey:[NSNumber numberWithInt:sensorId]];
}

-(void)dataSinksAddDataPoint:(DataPoint *)dataPoint
{
  CircularDataArray *circularDataArray = [self.dataSinks objectForKey:[NSNumber numberWithInt:dataPoint.sensorId]];
  if (circularDataArray) {
    [circularDataArray addDataPoint:dataPoint];
  }
}

#pragma mark - Handle incoming message from server
-(void)CommmunicationCentral:(CommunicationCentral *)central didReadMessage:(protobuf::GeneralMsg)message
{
//  NSLog(@"Read a message from server\n%s", message.DebugString().c_str());
  switch(message.sub_type()) {
    case protobuf::GeneralMsg::DATA_T:
//      NSLog(@"A DATA message");
      [self manageDataMessage:&message];
      break;
    case protobuf::GeneralMsg::CONFIGURATION_T:
      NSLog(@"A CONFIGURATION message");
      [self manageSensorConfigurations:&message];
      [[NSNotificationCenter defaultCenter] postNotificationName:@"Sensor list updated" object:self];
      break;
    case protobuf::GeneralMsg::SENSOR_STATUS_T:
      NSLog(@"A SENSOR_STATUS_T message");
      [self manageSensorStatus:&message];
      [[NSNotificationCenter defaultCenter] postNotificationName:@"Sensor list updated" object:self];
      break;
    case protobuf::GeneralMsg::STATUS_T:
      NSLog(@"A STATUS_T message");
      [self manageStatus:&message];
      [[NSNotificationCenter defaultCenter] postNotificationName:@"Status" object:self];
      break;
    case protobuf::GeneralMsg::ACTIVE_PROJECT_T:
      NSLog(@"An ACTIVE_PROJECT message");
      break;
    case protobuf::GeneralMsg::PROJECT_LIST_T:
    {
      NSLog(@"A PROJECT_LIST message" );
      [self manageProjectList:&message];
      [[NSNotificationCenter defaultCenter] postNotificationName:@"Project list updated" object:self];
      break;
    }
    default:
      break;
  }
}

-(void)manageDataMessage:(protobuf::GeneralMsg *)message
{
  protobuf::DataMsg dataMessage = message->data();
  
  double t = dataMessage.time();
  int32_t sensorId = dataMessage.type_id();
  int n = dataMessage.data_size();
  
  DataPoint *dataPoint = [self.lastData objectForKey:[NSNumber numberWithInt:sensorId]];
  if (!dataPoint) {
    dataPoint = [[DataPoint alloc] init];
    dataPoint.attributeValues = [[NSMutableArray alloc] initWithCapacity:n];
    [self.lastData setObject:dataPoint forKey:[NSNumber numberWithInt:sensorId]];
  }

  dataPoint.t = t;
  dataPoint.sensorId = sensorId;
  for( int i=0; i < n; i++ ) {
    double y = dataMessage.data(i);
    dataPoint.attributeValues[i] = [NSNumber numberWithDouble:y];
  }
  
  [self dataSinksAddDataPoint:dataPoint];
}

-(void)manageStatus:(protobuf::GeneralMsg *)message
{
  protobuf::StatusMsg statusMessage = message->status();
  
  switch (statusMessage.status_type()) {
    case protobuf::StatusMsg_StatusType_IDLE:
      self.projectStatus = IDLE;
      break;
    case protobuf::StatusMsg_StatusType_DATA_COLLECTION:
      self.projectStatus = DATA_COLLECTION;
      break;
    case protobuf::StatusMsg_StatusType_EXPERIMENT_PLAYBACK:
      self.projectStatus = EXPERIMENT_PLAYBACK;
      break;
    default:
      break;
  }
}

-(void)manageSensorStatus:(protobuf::GeneralMsg *)message
{
  protobuf::SensorStatusMsg statusMessage = message->sensor_status();

  int numberOfSensors = statusMessage.sensors_size();
  
  for (int i=0; i < numberOfSensors; i++) {
    protobuf::SensorStatusMsg::Sensor sensor = statusMessage.sensors(i);
    int idx = [self indexOfSensorWithID:sensor.sensor_id()];
    SensorConfiguration *sensorConfiguration = (SensorConfiguration *)self.sensors[idx];
    
    // SensorStatus protobuf::SensorStatusMsg::SensorStatusType::SensorStatusMsg_SensorStatusType_WORKING
    if (sensor.status() == protobuf::SensorStatusMsg_SensorStatusType_WORKING) {
      sensorConfiguration.status = @"Working";
    } else {
      sensorConfiguration.status = @"Not working";
    }
    
    // Attributes
    NSMutableArray *attributes = [[NSMutableArray alloc] initWithCapacity:sensor.attributes_size()];
    NSLog(@"Number of attributes = %d", sensor.attributes_size());
    
    for (int j=0; j < sensor.attributes_size(); j++) {
      if (sensor.attributes(j).status() == protobuf::SensorStatusMsg_AttributeStatusType_INSIDE_LIMITS) {
        attributes[j] = @{@"index": [NSNumber numberWithInt:sensor.attributes(j).index()],
                          @"status": [NSNumber numberWithInt:1]};
      } else if (sensor.attributes(j).status()==protobuf::SensorStatusMsg::AttributeStatusType::SensorStatusMsg_AttributeStatusType_OUTSIDE_LIMITS) {
        attributes[j] = @{@"index": [NSNumber numberWithInt:sensor.attributes(j).index()],
                          @"status": [NSNumber numberWithInt:2]};
      } else {
        attributes[j] = @{@"index": [NSNumber numberWithInt:sensor.attributes(j).index()],
                          @"status": [NSNumber numberWithInt:0]};
      }
    }
    sensorConfiguration.attributes = attributes;
  }
}

-(int)indexOfSensorWithID:(int)sensorId
{
  BOOL found = NO;
  int idx = 0;
  while( !found && idx < [self.sensors count]) {
    if (((SensorConfiguration *)self.sensors[idx]).sensorId == sensorId) {
      found = YES;
    } else {
      idx++;
    }
  }
  if (!found) {
    idx = -1;
  }
  return idx;
}

-(void)manageSensorConfigurations:(protobuf::GeneralMsg *)message
{
  protobuf::ConfigurationMsg sensorConfiguration = message->configuration();
  
  int n = sensorConfiguration.sensor_configurations_size();
  NSMutableArray *sensors = [[NSMutableArray alloc] initWithCapacity:n];
  
  for (int i=0; i < n; i++) {
    protobuf::SensorConfiguration protobufSensorConf = sensorConfiguration.sensor_configurations(i);
    SensorConfiguration *sensorConfig = [[SensorConfiguration alloc] init];
    sensorConfig.sensorId = (int)protobufSensorConf.sensor_id();
    sensorConfig.maxAttributes = protobufSensorConf.max_attributes();
    sensorConfig.name = [NSString stringWithFormat:@"%s", protobufSensorConf.name().c_str()];
    
    NSLog(@"Sensor (%d) %@\n", sensorConfig.sensorId, sensorConfig.name);
    int numberOfAttributeConfigurations = protobufSensorConf.attribute_configurations_size();
    for (int j=0; j < numberOfAttributeConfigurations; j++) {
      protobuf::AttributeConfiguration attributeConfiguration = protobufSensorConf.attribute_configurations(j);
      NSLog(@"  attribute (%d): %s\n",attributeConfiguration.index(), attributeConfiguration.name().c_str());
    }
    
    [sensors addObject:sensorConfig];
    
    sensorConfig.status = @"Unknown status";
  }
  self.sensors = sensors;
}

-(void)manageProjectList:(protobuf::GeneralMsg *)message
{
  protobuf::ProjectListMsg msg = message->project_list();
  
  NSMutableArray *projectNames = [[NSMutableArray alloc] initWithCapacity:msg.projects_size()];
  for (int i=0; i < msg.projects_size(); i++) {
    [projectNames addObject:[NSString stringWithFormat:@"%s", msg.projects(i).c_str()]];
  }
  [projectNames sortUsingComparator:^NSComparisonResult(id s1, id s2) {
    return [s1 localizedStandardCompare:s2];
  }];
  self.projectNames = projectNames;
}

@end
