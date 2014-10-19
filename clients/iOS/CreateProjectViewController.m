//
//  CreateProjectViewController.m
//  CDCV2
//
//  Created by Erik Frisk on 26/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "CreateProjectViewController.h"

@interface CreateProjectViewController () <UITextFieldDelegate, UITextViewDelegate, UIAlertViewDelegate>
@property (weak, nonatomic) IBOutlet UITextField *projectNameTextField;
@property (weak, nonatomic) IBOutlet UITextView *descriptionTextView;
@property (weak, nonatomic) IBOutlet UITextField *emailTextField;
@end

@implementation CreateProjectViewController

- (void)viewDidLoad
{
  [super viewDidLoad];
  self.projectNameTextField.delegate = self;
  self.emailTextField.delegate = self;
  self.descriptionTextView.delegate = self;
  
  [self.view addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapInBackground:)]];
  
  self.descriptionTextView.layer.borderWidth = 1.0f;
  self.descriptionTextView.layer.borderColor = [[UIColor grayColor] CGColor];
  
  self.descriptionTextView.clipsToBounds = YES;
  self.descriptionTextView.layer.cornerRadius = 10.0f;
}


- (void)tapInBackground:(UIGestureRecognizer *)gestureRecognizer
{
  if (gestureRecognizer.state==UIGestureRecognizerStateEnded) {
    [self.projectNameTextField resignFirstResponder];
    [self.emailTextField resignFirstResponder];
    [self.descriptionTextView resignFirstResponder];
  }
}


- (IBAction)createProject:(UIBarButtonItem *)sender
{
  if ([self.projectNameTextField.text length]>0) {
    NSMutableDictionary *projectInfo = [[NSMutableDictionary alloc] init];
    
    [projectInfo setObject:self.projectNameTextField.text forKey:@"name"];
    if ([self.emailTextField.text length]>0) {
      [projectInfo setObject:self.emailTextField.text forKey:@"email"];
    }
    if ([self.descriptionTextView.text length]>0) {
      [projectInfo setObject:self.emailTextField.text forKey:@"description"];
    }
    [self.delegate createNewProjectWithInfo:projectInfo];
  } else {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Create Message"
                                                    message:@"Project name can not be empty"
                                                   delegate:self
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];
  }
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
  [alertView dismissWithClickedButtonIndex:buttonIndex animated:YES];
}

- (IBAction)cancelAddProject:(UIBarButtonItem *)sender
{
  [self.delegate cancelAddProject];
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
  [textField resignFirstResponder];
  return YES;
}

-(BOOL)textViewShouldEndEditing:(UITextView *)textView
{
  [textView resignFirstResponder];
  return YES;
}


@end
