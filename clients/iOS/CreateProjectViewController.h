//
//  CreateProjectViewController.h
//  CDCV2
//
//  Created by Erik Frisk on 26/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol CreateProjectViewControllerDelegate <NSObject>
@optional
-(void)cancelAddProject;
-(void)createNewProjectWithInfo:(NSDictionary *)projectInfo;
@end


@interface CreateProjectViewController : UIViewController
@property (nonatomic, strong) id<CreateProjectViewControllerDelegate> delegate;
@end
