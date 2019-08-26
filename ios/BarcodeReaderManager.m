
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

//单参数 单回调时使用
//RCT_EXPORT_METHOD(readBarcode:(NSString *)key callback:(RCTResponseSenderBlock)callback){
//    self.callback = callback;
//    //主要这里必须使用主线程发送,不然有可能失效
//    dispatch_async(dispatch_get_main_queue(), ^{
//        [[NSNotificationCenter defaultCenter]postNotificationName:@"readBarcode" object:nil userInfo:@{@"inputValue": key}];
//    });
//    //返回结果
//    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(result:) name:@"callback" object:nil];
//}

//Promise 多参数 多回调时使用
RCT_REMAP_METHOD(readBarcode,key:(NSString *)key resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    _resolveBlock = resolve;
    _rejectBlock = reject;
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter]postNotificationName:@"readBarcode" object:nil userInfo:@{@"inputValue": key}];
    });
    //返回结果
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(result:) name:@"callback" object:nil];
}

//执行获取结果的监听
-(void)result:(NSNotification *)notification{
    result = [NSString stringWithString:notification.userInfo[@"result"]];
    
/*  单回调
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
    
    //Promise
    if(result != NULL && _resolveBlock != nil){
        _resolveBlock(@[result]);
        _resolveBlock = nil;
        [[NSNotificationCenter defaultCenter]postNotificationName:@"backToJs" object:nil];
    }
}
@end
  
