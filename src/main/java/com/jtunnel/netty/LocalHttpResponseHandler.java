package com.jtunnel.netty;


import com.jtunnel.data.DataStore;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.java.Log;

@Sharable
@Log
public class LocalHttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

  private final ChannelHandlerContext parentContext;
  private final String requestId;
  private final DataStore dataStore;


  public LocalHttpResponseHandler(ChannelHandlerContext parentContext, String requestId,
      DataStore dataStore) {
    this.parentContext = parentContext;
    this.dataStore = dataStore;
    this.requestId = requestId;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse fullHttpResponse)
      throws Exception {
    ChannelFuture f = parentContext.writeAndFlush(fullHttpResponse.retain());
    dataStore.saveFullTrace(requestId, fullHttpResponse);
    f.addListener(future -> {
      if (!future.isSuccess()) {
        future.cause().printStackTrace();
      }
    });
  }
}

