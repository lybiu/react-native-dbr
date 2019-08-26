//
//  BarcodeReaderManagerViewController.h
//  BarcodeReaderManager
//
//  Created by dynamsoft on 2019/8/23.
//  Copyright © 2019年 Facebook. All rights reserved.
//

#ifndef BarcodeReaderManagerViewController_h
#define BarcodeReaderManagerViewController_h
#import <UIKit/UIKit.h>
#import <React/RCTComponent.h>
#import "DbrManager.h"
#import "BarcodeReaderManager.h"
#import <AssetsLibrary/AssetsLibrary.h>
#import <DynamsoftBarcodeReader/DynamsoftBarcodeSDK.h>
NS_ASSUME_NONNULL_BEGIN
@interface BarcodeReaderManagerViewController : UIViewController{
    UIView *cameraPreview;
    AVCaptureVideoPreviewLayer *previewLayer;
    UIImageView *rectLayerImage;
    DbrManager *dbrManager;
    UIButton *flashButton;
    UIButton *helpButton;
}

@property (nonatomic, strong, nullable) UIView *cameraPreview;
@property (nonatomic, strong, nullable) AVCaptureVideoPreviewLayer *previewLayer;
@property (nonatomic, strong, nullable) UIImageView *rectLayerImage;
@property (nonatomic, strong, nullable) DbrManager *dbrManager;
@property (nonatomic, strong, nullable) UIButton *  flashButton;
@property (nonatomic, strong, nullable) UILabel *detectDescLabel;
@property (nonatomic, strong, nullable) UIButton *helpButton;
@end
NS_ASSUME_NONNULL_END
#endif /* ViewControl_h */
