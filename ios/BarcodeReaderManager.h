
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

@interface BarcodeReaderManager : NSObject <RCTBridgeModule>{
    NSString *result;
    RCTResponseSenderBlock callback;
}

@property (nonatomic, strong, nullable) NSString *result;
@property (nonatomic, strong, nullable) RCTResponseSenderBlock callback;

@end
  
