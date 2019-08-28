/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "AppDelegate.h"

#import <React/RCTBundleURLProvider.h>
#import "../../../ios/BarcodeReaderManagerViewController.h"
#import "../../../ios/DbrManager.h"
@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  NSURL *jsCodeLocation;

  jsCodeLocation = [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index.ios" fallbackResource:nil];

  _rootView = [[RCTRootView alloc] initWithBundleURL:jsCodeLocation
                                                      moduleName:@"Example"
                                               initialProperties:nil
                                                   launchOptions:launchOptions];
  _rootView.backgroundColor = [[UIColor alloc] initWithRed:1.0f green:1.0f blue:1.0f alpha:1];
 
  
  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  _rootViewController = [UIViewController new];
  _rootViewController.view = _rootView;
  
//  self.window.rootViewController = rootViewController;
  _nav = [[UINavigationController alloc] initWithRootViewController:_rootViewController];
  self.window.rootViewController = _nav;
  _nav.navigationBarHidden = YES;
  [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(doNotification:) name:@"readBarcode" object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(backToJs:) name:@"backToJs" object:nil];
  [self.window makeKeyAndVisible];
  return YES;
}

-(void)doNotification:(NSNotification *)notification{
  BarcodeReaderManagerViewController* dbrMangerController = [[BarcodeReaderManagerViewController alloc] init];
  dbrMangerController.dbrManager = [[DbrManager alloc] initWithLicense:notification.userInfo[@"inputValue"]];
  [self.nav pushViewController:dbrMangerController animated:YES];
}

-(void)backToJs:(NSNotification *)notification{
  [self.nav popToViewController:self.rootViewController animated:YES];
}

// remove listeners
-(void)dealloc{
  [[NSNotificationCenter defaultCenter] removeObserver:self name:@"readBarcode" object:nil];
  [[NSNotificationCenter defaultCenter] removeObserver:self name:@"backToJs" object:nil];
}
@end
