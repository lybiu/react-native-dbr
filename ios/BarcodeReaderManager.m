
#import "DbrManager.h"
#import <React/RCTBridge.h>
#import "BarcodeReaderManager.h"

@implementation BarcodeReaderManager
{
    RCTPromiseResolveBlock _resolveBlock;
    RCTPromiseRejectBlock _rejectBlock;
}
- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
@synthesize bridge = _bridge;
@synthesize result;
@synthesize callback;
RCT_EXPORT_MODULE(BarcodeReaderManager)

// Single parameter, used in a single callback
//RCT_EXPORT_METHOD(readBarcode:(NSString *)key callback:(RCTResponseSenderBlock)callback){
//    self.callback = callback;
//    // It must be sent with the main thread, otherwise it may fail.
//    dispatch_async(dispatch_get_main_queue(), ^{
//        [[NSNotificationCenter defaultCenter]postNotificationName:@"readBarcode" object:nil userInfo:@{@"inputValue": key}];
//    });
//    // Return results
//    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(result:) name:@"callback" object:nil];
//}

//Promise 
//multiple parameters, used in multiple callbacks
RCT_REMAP_METHOD(readBarcode,key:(NSString *)key resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    _resolveBlock = resolve;
    _rejectBlock = reject;
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter]postNotificationName:@"readBarcode" object:nil userInfo:@{@"inputValue": key}];
    });
    // Return results
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(result:) name:@"callback" object:nil];
}

// Perform listening to get results
-(void)result:(NSNotification *)notification{
    result = [NSString stringWithString:notification.userInfo[@"result"]];
    
/*  single callback
    NSString* error = @"something wrong !";
//    callback
    if(result != NULL && callback !=nil){
        self.callback(@[[NSNull null],result]);
        self.callback = nil;
        [[NSNotificationCenter defaultCenter]postNotificationName:@"backToJs" object:nil];
    }else{
        self.error(@[[NSNull null],error]);
    }
*/
    
    // Promise
    if(result != NULL && _resolveBlock != nil){
        _resolveBlock(@[result]);
        _resolveBlock = nil;
        [[NSNotificationCenter defaultCenter]postNotificationName:@"backToJs" object:nil];
    }
}
@end
  
