//
//  ShowProjectsTableViewController.m
//  CDCV2
//
//  Created by Erik Frisk on 26/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "ShowProjectsTableViewController.h"
#import "CreateProjectViewController.h"
#import "ProjectTabViewController.h"

@interface ShowProjectsTableViewController () <CreateProjectViewControllerDelegate>
@property (nonatomic, strong) id messageObserver;
@end

@implementation ShowProjectsTableViewController

- (void)viewDidLoad
{
  [super viewDidLoad];
  
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
                            [self.tableView reloadData];
                          }];
}

-(void)viewWillDisappear:(BOOL)animated
{
  [[NSNotificationCenter defaultCenter] removeObserver:self.messageObserver];
  [super viewWillDisappear:animated];
}

- (IBAction)createProjectButtonPressed:(UIBarButtonItem *)sender
{
  [self performSegueWithIdentifier:@"Create Project" sender:self];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
  return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
  return [self.connectionClient.projectNames count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Project Name Cell" forIndexPath:indexPath];
  cell.textLabel.text = self.connectionClient.projectNames[indexPath.row];
  
  return cell;
}

-(void)createNewProjectWithInfo:(NSDictionary *)projectInfo
{
  NSString *projectName = [projectInfo objectForKey:@"name"];
  
  if ([projectName length] > 0) {
    NSLog(@"I should add a new project with name %@!", projectName);
    NSString *email = [projectInfo objectForKey:@"email"];
    NSString *description = [projectInfo objectForKey:@"description"];

    BOOL anyMetadata = ([email length]>0) || ([description length]>0);
    
    protobuf::GeneralMsg message;
    protobuf::CreateNewProjectRequestMsg   *createProjectMessage = new protobuf::CreateNewProjectRequestMsg;
    protobuf::SetProjectMetadataRequestMsg *metadataRequestMessage = new protobuf::SetProjectMetadataRequestMsg;
    protobuf::ProjectMetadataStruct        *metadataStruct = new protobuf::ProjectMetadataStruct;

    // Configure GeneralMsg
    message.set_sub_type(protobuf::GeneralMsg_SubType_CREATE_NEW_PROJECT_REQUEST_T);

    // Configure CreateNewProjectRequestMsg and add to GeneralMsg
    createProjectMessage->set_name([projectName cStringUsingEncoding:NSASCIIStringEncoding]);
    message.set_allocated_create_new_project_request(createProjectMessage);

    // Are there any metadata?
    if ( anyMetadata) {
      // Configure ProjectMetadataStruct
      if ([description length]>0) {
        metadataStruct->set_description([description cStringUsingEncoding:NSASCIIStringEncoding]);
      }
      if ([email length]>0) {
        metadataStruct->set_email([email cStringUsingEncoding:NSASCIIStringEncoding]);
      }
      // Add to SetProjectMetadataRequestMsg
      metadataRequestMessage->set_allocated_metadata(metadataStruct);

      // Add SetProjectMetadataRequestMsg to GeneralMsg
      message.set_allocated_set_project_metadata_request(metadataRequestMessage);
    }
    
    // Send it!
    [self.communicationCentral sendMessage:message];
  }
  
  
  [self dismissViewControllerAnimated:YES completion: nil];
}

-(void)cancelAddProject
{
  [self dismissViewControllerAnimated:YES completion: nil];
}


-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
  if ([segue.destinationViewController isKindOfClass:[CreateProjectViewController class]]) {
    CreateProjectViewController *cpvc = (CreateProjectViewController *)segue.destinationViewController;
    cpvc.delegate = self;
  } else if( [segue.destinationViewController isKindOfClass:[ProjectTabViewController class]]) {
    ProjectTabViewController *ptvc = (ProjectTabViewController *)segue.destinationViewController;

    ptvc.title = [self.connectionClient.projectNames objectAtIndex:[self.tableView indexPathForSelectedRow].row];
    ptvc.connectionClient = self.connectionClient;
    ptvc.communicationCentral = self.communicationCentral;
  }
}



@end
