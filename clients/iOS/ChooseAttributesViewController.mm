//
//  ChooseAttributesViewController.m
//  CDCV2
//
//  Created by Erik Frisk on 02/09/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "ChooseAttributesViewController.h"

@interface ChooseAttributesViewController () <UITableViewDataSource>
@property (weak, nonatomic) IBOutlet UITableView *attributesTableView;
@end

@implementation ChooseAttributesViewController

- (void)viewDidLoad
{
  [super viewDidLoad];
  self.attributesTableView.dataSource = self;
  
  for (NSNumber *attribute in self.chosenAttributes) {
    [self.attributesTableView selectRowAtIndexPath:[NSIndexPath indexPathForRow:[attribute intValue] inSection:0] animated:NO scrollPosition:UITableViewScrollPositionNone];
  }
  [self.attributesTableView setContentOffset:CGPointZero animated:NO];
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
  return self.sensor.maxAttributes;
}

-(void)viewWillDisappear:(BOOL)animated
{
  NSArray *selectedIndexPaths = [self.attributesTableView indexPathsForSelectedRows];
  [self.chosenAttributes removeAllObjects];
  if ([selectedIndexPaths count]>0) {
    for (int i=0; i < [selectedIndexPaths count]; i++) {
      NSInteger selectedAttributeNumber = ((NSIndexPath *)selectedIndexPaths[i]).row;
      NSNumber *attribute = [NSNumber numberWithInt:selectedAttributeNumber];
      [self.chosenAttributes addObject:attribute];
    }
  }
}
- (IBAction)clearPressed:(UIBarButtonItem *)sender
{
  NSArray *selectedIndexPaths = [self.attributesTableView indexPathsForSelectedRows];
  for (NSIndexPath *selectedCellPath in selectedIndexPaths) {
    [self.attributesTableView deselectRowAtIndexPath:selectedCellPath animated:YES];
  }
}


-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
  UITableViewCell *cell = [self.attributesTableView dequeueReusableCellWithIdentifier:@"Attribute Plot Cell"];
  cell.textLabel.text = [NSString stringWithFormat:@"Attribute %d", (int)indexPath.row + 1];
  
  return cell;
}

@end
