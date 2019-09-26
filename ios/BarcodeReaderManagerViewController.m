//
//  BarcodeReaderManagerViewController.m
//  BarcodeReaderManager
//
//  Created by dynamsoft on 2019/8/23.
//  Copyright © 2019年 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BarcodeReaderManagerViewController.h"
#import "DbrManager.h"
#import <React/RCTBridgeModule.h>

@import DynamsoftBarcodeReader;
@implementation BarcodeReaderManagerViewController
{
    BOOL m_isFlashOn;
    int itrFocusFinish;
}
@synthesize cameraPreview;
@synthesize previewLayer;
@synthesize rectLayerImage;
@synthesize dbrManager;
@synthesize flashButton;
@synthesize helpButton;

- (void)viewDidLoad {
    [super viewDidLoad];
    flashButton = [[UIButton alloc] initWithFrame:CGRectMake(110, 80, 150, 150)];
    [flashButton setImage:[UIImage imageNamed:@"flash_off"] forState:UIControlStateNormal];
    [flashButton addTarget:self action:@selector(onFlashButtonClick) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:flashButton];
    rectLayerImage = [[UIImageView alloc] initWithFrame:CGRectMake(10, 20, 300, 300)];
    rectLayerImage.center = self.view.center;
    [self.view addSubview:rectLayerImage];
    helpButton = [[UIButton alloc] initWithFrame:CGRectMake(110, 50, 150, 150)];
    [helpButton setImage:[UIImage imageNamed:@"help"] forState:UIControlStateNormal];
    [helpButton addTarget:self action:@selector(onAboutInfoClick) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:helpButton];
    
    [[UIApplication sharedApplication] setIdleTimerDisabled: YES];
    
    //register notification for UIApplicationDidBecomeActiveNotification
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(didBecomeActive:)
                                                 name:UIApplicationDidBecomeActiveNotification
                                               object:nil];
    //dbrManager = [[DbrManager alloc] init];
    [dbrManager setRecognitionCallback:self :@selector(onReadImageBufferComplete:)];
    [dbrManager setVideoSession];
    [dbrManager startVideoSession];
    
    itrFocusFinish = 0;
    [self configInterface];
}

- (void)viewDidUnload {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    if(dbrManager != nil)
    {
        dbrManager = nil;
    }
}

- (void)controllerWillPopHandler {
    if(dbrManager != nil)
    {
        dbrManager = nil;
    }
}

- (void)didBecomeActive:(NSNotification *)notification;
{
    if(dbrManager.isPauseFramesComing == NO)
        [self turnFlashOn:m_isFlashOn];
}

-(void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    AVCaptureDevice *camDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    int flags = NSKeyValueObservingOptionNew;
    [camDevice addObserver:self forKeyPath:@"adjustingFocus" options:flags context:nil];
    dbrManager.isPauseFramesComing = NO;
    [self turnFlashOn:m_isFlashOn];
}

-(void)viewWillDisappear:(BOOL)animated{
    AVCaptureDevice*camDevice =[AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    [camDevice removeObserver:self forKeyPath:@"adjustingFocus"];
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (void) turnFlashOn: (BOOL) on {
    // validate whether flashlight is available
    Class captureDeviceClass = NSClassFromString(@"AVCaptureDevice");
    if (captureDeviceClass != nil) {
        AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
        if (device != nil && [device hasTorch] && [device hasFlash]){
            [device lockForConfiguration:nil];
            
            if (on == YES) {
                [device setTorchMode:AVCaptureTorchModeOn];
                [device setFlashMode:AVCaptureFlashModeOn];
                [flashButton setImage:[UIImage imageNamed:@"flash_on"] forState:UIControlStateNormal];
                [flashButton setTitle:@" Flash on" forState:UIControlStateNormal];
                
            } else {
                [device setTorchMode:AVCaptureTorchModeOff];
                [device setFlashMode:AVCaptureFlashModeOff];
                [flashButton setImage:[UIImage imageNamed:@"flash_off"] forState:UIControlStateNormal];
                [flashButton setTitle:@" Flash off" forState:UIControlStateNormal];
            }
            
            [device unlockForConfiguration];
        }
    }
}

- (void)onFlashButtonClick {
    m_isFlashOn = m_isFlashOn == YES ? NO : YES;
    [self turnFlashOn:m_isFlashOn];
}

- (void) customizeAC : (UIAlertController *) ac
{
    if(ac == nil) return;
    
    UIView *subView1 = ac.view.subviews[0];
    UIView *subView2 = subView1.subviews[0];
    UIView *subView3 = subView2.subviews[0];
    UIView *subView4 = subView3.subviews[0];
    UIView *subView5 = subView4.subviews[0];
    
    for (int i = 0; i < subView5.subviews.count; i++) {
        if([subView5.subviews[i] isKindOfClass: [UILabel class]])
        {
            UILabel *alertLabel = (UILabel*)subView5.subviews[i];
            alertLabel.textAlignment = NSTextAlignmentLeft;
        }
    }
}

- (void)onAboutInfoClick{
    dbrManager.isPauseFramesComing = YES;
    
    UIAlertController * ac=   [UIAlertController
                               alertControllerWithTitle:@"About"
                               message:@"\nDynamsoft Barcode Reader Mobile App Demo(Dynamsoft Barcode Reader SDK)\n\n© 2019 Dynamsoft. All rights reserved. \n\nIntegrate Barcode Reader Functionality into Your own Mobile App? \n\nClick 'Overview' button for further info.\n\n"
                               preferredStyle:UIAlertControllerStyleAlert];
    
    [self customizeAC:ac];
    
    UIAlertAction* linkAction = [UIAlertAction actionWithTitle:@"Overview" style:UIAlertActionStyleDefault handler:^(UIAlertAction * action)
                                 {
                                     NSString *urlString = @"http://www.dynamsoft.com/Products/barcode-scanner-sdk-iOS.aspx";
                                     NSURL *url = [NSURL URLWithString:urlString];
                                     if ([[UIApplication sharedApplication] canOpenURL:url])
                                     {
                                         [[UIApplication sharedApplication] openURL:url];
                                     }
                                     self->dbrManager.isPauseFramesComing = NO;
                                 }];
    [ac addAction:linkAction];
    
    UIAlertAction* yesButton = [UIAlertAction
                                actionWithTitle:@"OK"
                                style:UIAlertActionStyleDefault
                                handler:^(UIAlertAction * action)
                                {
                                    self->dbrManager.isPauseFramesComing = NO;
                                }];
    
    [ac addAction:yesButton];
    
    [self presentViewController:ac animated:YES completion:nil];
}

-(void) onReadImageBufferComplete:(NSArray *) readResult{
    
    if(readResult == nil || dbrManager.isPauseFramesComing == YES)
    {
        dbrManager.isCurrentFrameDecodeFinished = YES;
        return;
    }
    
    double timeInterval = [dbrManager.startRecognitionDate timeIntervalSinceNow] * -1;
    iTextResult* barcode = (iTextResult*)readResult.firstObject;
    if (!barcode) {
        dbrManager.isCurrentFrameDecodeFinished = YES;
        return;
    }
    
    double left = FLT_MAX;
    double top = FLT_MAX;
    double right = 0;
    double bottom = 0;
    for (int i = 0; i < barcode.localizationResult.resultPoints.count; ++i) {
        left = left < [barcode.localizationResult.resultPoints[i] CGPointValue].x ? left : [barcode.localizationResult.resultPoints[i] CGPointValue].x;
        top = top < [barcode.localizationResult.resultPoints[i] CGPointValue].y ? top : [barcode.localizationResult.resultPoints[i] CGPointValue].y;
        right = right > [barcode.localizationResult.resultPoints[i] CGPointValue].x ? right : [barcode.localizationResult.resultPoints[i] CGPointValue].x;
        bottom = bottom > [barcode.localizationResult.resultPoints[i] CGPointValue].y ? bottom : [barcode.localizationResult.resultPoints[i] CGPointValue].y;
    }
    NSString* msgText = [NSString stringWithFormat:@"\nType: %@\n\nValue: %@\n\nRegion: {Left: %.f, Top: %.f, Right: %.f, Bottom: %.f}\n\nInterval: %.03f seconds\n\n",
                         barcode.barcodeFormatString, barcode.barcodeText, left, top, right, bottom, timeInterval];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter]postNotificationName:@"callback" object:nil userInfo:@{@"result": msgText}];
    });
}

// callback
-(void)observeValueForKeyPath:(NSString*)keyPath ofObject:(id)object change:(NSDictionary*)change context:(void*)context {
    
    if([keyPath isEqualToString:@"adjustingFocus"]){
        BOOL adjustingFocus =[[change objectForKey:NSKeyValueChangeNewKey] isEqualToNumber:[NSNumber numberWithInt:1]];
        
        
        if (adjustingFocus == NO && itrFocusFinish == 0) {
            dbrManager.adjustingFocus = NO;
            itrFocusFinish++;
        }
    }
}

- (void) configInterface{
    self.navigationController.interactivePopGestureRecognizer.enabled = NO;
    
    CGFloat w = [[UIScreen mainScreen] bounds].size.width;
    CGFloat h = [[UIScreen mainScreen] bounds].size.height;
    CGRect mainScreenLandscapeBoundary = CGRectZero;
    mainScreenLandscapeBoundary.size.width = MIN(w, h);
    mainScreenLandscapeBoundary.size.height = MAX(w, h);
    
    rectLayerImage.frame = mainScreenLandscapeBoundary;
    rectLayerImage.contentMode = UIViewContentModeTopLeft;
    
    [self createRectBorderAndAlignControls];
    
    //show vedio capture
    AVCaptureSession* captureSession = [dbrManager getVideoSession];
    if(captureSession == nil)
        return;
    
    previewLayer = [AVCaptureVideoPreviewLayer layerWithSession:captureSession];
    [previewLayer setVideoGravity:AVLayerVideoGravityResizeAspectFill];
    previewLayer.frame = mainScreenLandscapeBoundary;
    cameraPreview = [[UIView alloc] init];
    [cameraPreview.layer addSublayer:previewLayer];
    [self.view insertSubview:cameraPreview atIndex:0];
}

- (void)createRectBorderAndAlignControls {
    int width = rectLayerImage.bounds.size.width;
    int height = rectLayerImage.bounds.size.height;
    
    int widthMargin = width * 0.1;
    int heightMargin = (height - width + 2 * widthMargin) / 2;
    
    UIGraphicsBeginImageContext(self.rectLayerImage.bounds.size);
    CGContextRef ctx = UIGraphicsGetCurrentContext();
    
    //1. draw gray rect
    [[UIColor colorWithRed:0 green:0 blue:0 alpha:0.02] setFill];
    CGContextFillRect(ctx, (CGRect){{0,      0}, {widthMargin, height}});
    CGContextFillRect(ctx, (CGRect){{0,      0}, {width, heightMargin}});
    CGContextFillRect(ctx, (CGRect){{width - widthMargin, 0}, {widthMargin, height}});
    CGContextFillRect(ctx, (CGRect){{0, height - heightMargin}, {width, heightMargin}});
    
    //2. draw red line
    CGPoint points[2];
    [[UIColor redColor] setStroke];
    CGContextSetLineWidth(ctx, 2.0);
    points[0]=(CGPoint){widthMargin + 5,height/2}; points[1]=(CGPoint){width-widthMargin-5,height/2};
    CGContextStrokeLineSegments(ctx, points, 2);
    
    //3. draw white rect
    [[UIColor whiteColor] setStroke];
    CGContextSetLineWidth(ctx, 1.0);
    // draw left side
    points[0]=(CGPoint){widthMargin,heightMargin}; points[1]=(CGPoint){widthMargin,height - heightMargin};
    CGContextStrokeLineSegments(ctx, points, 2);
    // draw right side
    points[0]=(CGPoint){width - widthMargin,heightMargin}; points[1]=(CGPoint){width - widthMargin,height - heightMargin};
    CGContextStrokeLineSegments(ctx, points, 2);
    // draw top side
    points[0]=(CGPoint){widthMargin,heightMargin}; points[1]=(CGPoint){width - widthMargin,heightMargin};
    CGContextStrokeLineSegments(ctx, points, 2);
    // draw bottom side
    points[0]=(CGPoint){widthMargin,height - heightMargin}; points[1]=(CGPoint){width - widthMargin,height - heightMargin};
    CGContextStrokeLineSegments(ctx, points, 2);
    
    //4. draw orange corners
    [[UIColor orangeColor] setStroke];
    CGContextSetLineWidth(ctx, 2.0);
    // draw left up corner
    points[0]=(CGPoint){widthMargin - 2,heightMargin - 2}; points[1]=(CGPoint){widthMargin + 18,heightMargin - 2};
    CGContextStrokeLineSegments(ctx, points, 2);
    points[0]=(CGPoint){widthMargin - 2,heightMargin - 2}; points[1]=(CGPoint){widthMargin - 2,heightMargin + 18};
    CGContextStrokeLineSegments(ctx, points, 2);
    // draw left bottom corner
    points[0]=(CGPoint){widthMargin - 2,height - heightMargin + 2}; points[1]=(CGPoint){widthMargin + 18,height - heightMargin + 2};
    CGContextStrokeLineSegments(ctx, points, 2);
    points[0]=(CGPoint){widthMargin - 2,height - heightMargin + 2}; points[1]=(CGPoint){widthMargin - 2,height - heightMargin - 18};
    CGContextStrokeLineSegments(ctx, points, 2);
    // draw right up corner
    points[0]=(CGPoint){width - widthMargin + 2,heightMargin - 2}; points[1]=(CGPoint){width - widthMargin - 18,heightMargin - 2};
    CGContextStrokeLineSegments(ctx, points, 2);
    points[0]=(CGPoint){width - widthMargin + 2,heightMargin - 2}; points[1]=(CGPoint){width - widthMargin + 2,heightMargin + 18};
    CGContextStrokeLineSegments(ctx, points, 2);
    // draw right bottom corner
    points[0]=(CGPoint){width - widthMargin + 2,height - heightMargin + 2}; points[1]=(CGPoint){width - widthMargin - 18,height - heightMargin + 2};
    CGContextStrokeLineSegments(ctx, points, 2);
    points[0]=(CGPoint){width - widthMargin + 2,height - heightMargin + 2}; points[1]=(CGPoint){width - widthMargin + 2,height - heightMargin - 18};
    CGContextStrokeLineSegments(ctx, points, 2);
    
    //5. set image
    rectLayerImage.image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    //6. align helpButton horizontal center
    CGRect tempFrame = helpButton.frame;
    tempFrame.origin.x = (width - helpButton.bounds.size.width) / 2;
    tempFrame.origin.y = heightMargin * 0.3;
    [helpButton setFrame:tempFrame];
    
    //7. align flashButton horizontal center
    tempFrame = flashButton.frame;
    tempFrame.origin.x = (width - flashButton.bounds.size.width) / 2;
    tempFrame.origin.y = (heightMargin + (width - widthMargin * 2) + height) * 0.5 - flashButton.bounds.size.height * 0.5;
    [flashButton setFrame:tempFrame];
    return;
}

@end
