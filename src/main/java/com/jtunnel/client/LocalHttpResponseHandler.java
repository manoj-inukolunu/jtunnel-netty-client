package com.jtunnel.client;


import com.jtunnel.proto.MessageType;
import com.jtunnel.proto.ProtoMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseEncoder;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Sharable
@Slf4j
public class LocalHttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

  private final String sessionId;
  private final ChannelHandlerContext parentContext;

  class HttpMessageHandler extends ChannelOutboundHandlerAdapter {

    private final ChannelPipeline pipeline;
    private final String sessionId;
    ChannelHandlerContext localHandlerContext;

    public HttpMessageHandler(ChannelHandlerContext localHandlerContext, ChannelPipeline channel,
        String sessionId) {
      this.pipeline = channel;
      this.sessionId = sessionId;
      this.localHandlerContext = localHandlerContext;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      if (msg instanceof ProtoMessage) {
        log.debug("Received Fin Message on channelId " + localHandlerContext.channel().id().toString());
        pipeline.writeAndFlush(msg);
      } else {
        ByteBuf byteBuf = (ByteBuf) msg;
        int len = byteBuf.readableBytes();
        byte[] data = new byte[len];
        byteBuf.readBytes(data);
        ProtoMessage message = new ProtoMessage();
        message.setSessionId(sessionId);
        message.setMessageType(MessageType.HTTP_RESPONSE);
        message.setBody(new String(data));
        pipeline.writeAndFlush(message).addListener(future -> log.debug("Sent Proto Message"));
      }
      ctx.writeAndFlush(msg);
    }
  }

  public LocalHttpResponseHandler(ChannelHandlerContext parentContext, String sessionId) {
    this.sessionId = sessionId;
    this.parentContext = parentContext;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse fullHttpResponse)
      throws Exception {
    EmbeddedChannel embeddedChannel =
        new EmbeddedChannel(new HttpMessageHandler(channelHandlerContext, parentContext.pipeline(), sessionId),
            new HttpObjectAggregator(Integer.MAX_VALUE), new HttpResponseEncoder());
    ChannelFuture f = embeddedChannel.writeAndFlush(fullHttpResponse.retain());
    embeddedChannel.writeAndFlush(ProtoMessage.finResponseMessage(sessionId));
    channelHandlerContext.close();
    embeddedChannel.close();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    log.debug("Channel Inactive");
    super.channelInactive(ctx);
    ctx.close();
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    log.debug("Channel ChannelUn Registered");
    super.channelUnregistered(ctx);
    ctx.close();
  }
}

