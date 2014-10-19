//
//  DynamicEventsViewController.m
//  CDCV2
//
//  Created by Erik Frisk on 29/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "DynamicEventsViewController.h"

@interface DynamicEventsViewController () <UITableViewDataSource,UIAlertViewDelegate,UITableViewDelegate>
@property (weak, nonatomic) IBOutlet UITableView *dynamicEventTable;
@property (nonatomic, strong) NSArray *dynamicEventNames; // of NSString
@end

@implementation DynamicEventsViewController

-(NSArray *)dynamicEventNames
{
  if(!_dynamicEventNames) _dynamicEventNames = [[NSArray alloc] init];
  
  return _dynamicEventNames;
}

- (void)viewDidLoad
{
  [super viewDidLoad];
  
  self.dynamicEventTable.dataSource = self;
  self.dynamicEventTable.delegate = self;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
  if (indexPath.row < [self.dynamicEventNames count]) {
    NSLog(@"Send a dynamic event message: %@", self.dynamicEventNames[indexPath.row]);
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
  }
}

- (IBAction)newDynamicEvent:(UIButton *)sender
{
  UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Describe dynamic event"
                                                  message:nil
                                                 delegate:self
                                        cancelButtonTitle:@"Cancel"
                                        otherButtonTitles:@"Add", nil];
  [alert setAlertViewStyle:UIAlertViewStylePlainTextInput];
  [alert show];

}

-(void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
  if (buttonIndex==1) {
    NSString *eventMessage = [alertView textFieldAtIndex:0].text;
    self.dynamicEventNames  = [self.dynamicEventNames arrayByAddingObject:eventMessage];
    [self.dynamicEventTable reloadData];
  }
}


-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
  return [self.dynamicEventNames count];
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
  UITableViewCell *cell = nil;
  if (indexPath.row < [self.dynamicEventNames count]) {
    cell = [tableView dequeueReusableCellWithIdentifier:@"Dynamic Event Cell" forIndexPath:indexPath];
    cell.textLabel.text = (NSString *)self.dynamicEventNames[indexPath.row];
  }
  
  return cell;
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
