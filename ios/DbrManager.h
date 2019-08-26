//
//  DbrManager.h
//  BarcodeReaderManager
//
//  Created by dynamsoft on 2019/8/23.
//  Copyright © 2019年 Facebook. All rights reserved.
//

#ifndef DbrManager_h
#define DbrManager_h

#import <UIKit/UIKit.h>
#import <CoreMedia/CoreMedia.h>
#import <AVFoundation/AVFoundation.h>
#import <DynamsoftBarcodeReader/DynamsoftBarcodeSDK.h>

@interface DbrManager : NSObject<AVCaptureVideoDataOutputSampleBufferDelegate, NSURLConnectionDelegate>

@property (nonatomic) long barcodeFormat;

@property (strong, nonatomic) NSDate *startRecognitionDate;

@property (strong, nonatomic) NSDate *startVidioStreamDate;

@property BOOL isPauseFramesComing;
@property BOOL isCurrentFrameDecodeFinished;
@property BOOL adjustingFocus;

@property CGSize cameraResolution;

-(id)initWithLicense:(NSString *)license;

-(void)setVideoSession;

-(void)startVideoSession;

-(AVCaptureSession*) getVideoSession;

-(void)setRecognitionCallback :(id)sender :(SEL)callback;

-(void)setLicense:(NSString *)license;

@end

#endif /* DbrManager_h */
