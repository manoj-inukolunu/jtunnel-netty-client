package com.jtunnel.netty;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectEncoder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.java.Log;

@Log
@Sharable
public class LocalHttpRequestHandler extends ChannelOutboundHandlerAdapter {

  public LocalHttpRequestHandler(ChannelHandlerContext channelHandlerContext) {
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    FullHttpRequest request = (FullHttpRequest) msg;
    log.info("About to write to local channel");
    log.info(request.content().toString(CharsetUtil.UTF_8));
    HttpObjectEncoder<HttpRequest> encoder = new HttpRequestEncoder();
    encoder.write(ctx, msg, promise);
    encoder.flush(ctx);

  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
//    super.exceptionCaught(ctx, cause);
  }
}
