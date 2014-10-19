//
//  CommunicationCentral.m
//  CDCV2
//
//  Created by Erik Frisk on 27/08/14.
//  Copyright (c) 2014 Linköping University. All rights reserved.
//

#import "CommunicationCentral.h"
#import "CommunicationCentralDelegate.h"
#import "GCDAsyncSocket.h"


@interface CommunicationCentral() <GCDAsyncSocketDelegate>
@property (nonatomic, strong) GCDAsyncSocket *socket;
@property (nonatomic) dispatch_semaphore_t readSemaphore;
@end

@implementation CommunicationCentral

#pragma mark - Initialization
-(instancetype)init
{
  self = [super init];
  if (self) {
    self.readSemaphore = dispatch_semaphore_create(0);
  }
  return self;
}

#pragma mark - Properties and lazy instanciation of socket
-(GCDAsyncSocket *)socket
{
  if (!_socket) {
    _socket = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:dispatch_get_main_queue()];
    self.socket.delegate = self;
  }
  
  return _socket;
}

-(BOOL)isConnected
{
  return [self.socket isConnected];
}

#pragma mark - GCDAsyncSocketDelegate methods
#define CDCV2_TAG_HEADER  0
#define CDCV2_TAG_MSGBODY 1
-(void)socket:(GCDAsyncSocket *)sock didReadData:(NSData *)data withTag:(long)tag
{
  switch (tag) {
    case CDCV2_TAG_HEADER:
    {
//      NSLog(@"Got TCP message with tag CDCV2_TAG_HEADER");
      char *buf = (char *)data.bytes;
      google::protobuf::uint32 size;
      google::protobuf::io::ArrayInputStream ais(buf,4);
      google::protobuf::io::CodedInputStream coded_input(&ais);
      coded_input.ReadVarint32(&size);//Decode the HDR and get the size
      
      //      int s = google::protobuf::io::CodedOutputStream::VarintSize32(size);
      //      NSLog(@"A new message with size %d, lets read entire message", size);
      [self.socket readDataToLength:size withTimeout:-1 tag:CDCV2_TAG_MSGBODY];
      break;
    }
    case CDCV2_TAG_MSGBODY: {
//      NSLog(@"Got TCP message with tag CDCV2_TAG_MSGBODY");
      protobuf::GeneralMsg message;
      
      const char *buf = (char *)data.bytes;
      
      google::protobuf::uint32 messageSize = data.length;
      google::protobuf::io::ArrayInputStream ais(buf,messageSize);
      google::protobuf::io::CodedInputStream coded_input(&ais);
      
      google::protobuf::io::CodedInputStream::Limit msgLimit = coded_input.PushLimit(messageSize);
      
      //De-Serialize
      message.ParseFromCodedStream(&coded_input);
      
      //Once the embedded message has been parsed, PopLimit() is called to undo the limit
      coded_input.PopLimit(msgLimit);

      dispatch_semaphore_signal(self.readSemaphore);

      [self.delegate CommmunicationCentral:self didReadMessage:message];
      break;
    }
    default:
      NSLog(@"Strange message, should not happen... tag = %ld", tag);
      break;
  }
}

#pragma mark - Utility functions
-(BOOL)connectToServer:(NSString *)address onPort:(uint16_t)port
{
  NSError *err = nil;
  BOOL ret = YES;
  [self.socket disconnect];
  if (![self.socket connectToHost:address onPort:port error:&err]) {
    NSLog(@"Connection error: %@", err);
    ret = NO;
  }
  return ret;
}

-(void)socket:(GCDAsyncSocket *)sock didConnectToHost:(NSString *)host port:(uint16_t)port
{
  [[NSNotificationCenter defaultCenter] postNotificationName:@"connection"
                                                      object:self
                                                    userInfo:@{@"connect":[NSNumber numberWithInt:1]}];
  NSLog(@"Connection established, start listening");
  [self asyncStartListening];
}


-(void)disconnect
{
  [self.socket disconnect];
  [[NSNotificationCenter defaultCenter] postNotificationName:@"connection"
                                                      object:self
                                                    userInfo:@{@"connect":[NSNumber numberWithInt:0]}];
}

-(void)asyncStartListening
{
  dispatch_queue_t listenQueue = dispatch_queue_create("listen queue", DISPATCH_QUEUE_SERIAL);
  dispatch_async(listenQueue,
    ^{
      while (1) {
#warning Hard coded data length, how to peek?
      [self.socket readDataToLength:1 withTimeout:-1 tag:CDCV2_TAG_HEADER];
      dispatch_semaphore_wait(self.readSemaphore, DISPATCH_TIME_FOREVER);
    }
  });
}

-(BOOL)sendMessage:(protobuf::GeneralMsg)message
{
  BOOL ret = YES;
  
  int siz = message.ByteSize()+4; // Need space also for size byte header
  char *pkt = new char [siz];
  google::protobuf::io::ArrayOutputStream aos(pkt,siz);
  google::protobuf::io::CodedOutputStream *coded_output = new google::protobuf::io::CodedOutputStream(&aos);
  
  coded_output->WriteVarint32(message.ByteSize()); // Write size header to pkt*
  message.SerializeToCodedStream(coded_output);    // Write message to pkt*

  NSData *data = [[NSData alloc] initWithBytes:pkt length:siz];
  
  [self.socket writeData:data withTimeout:-1 tag:0];
  delete[] pkt; // protobuf deallocates, yes/no?
  return ret;
}


@end
