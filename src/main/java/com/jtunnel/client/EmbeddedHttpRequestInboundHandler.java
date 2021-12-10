package com.jtunnel.client;


import com.jtunnel.data.DataStore;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import java.net.InetSocketAddress;
import java.util.UUID;

public class EmbeddedHttpRequestInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private ChannelHandlerContext tunnelClientContext;
  private final String sessionId;
  private final DataStore dataStore;
  private final String destHost;
  private final int localPort;
  private final EventLoopGroup clientHttpEventLoopGroup;

  public EmbeddedHttpRequestInboundHandler(EventLoopGroup clientHttpEventLoopGroup,
      ChannelHandlerContext tunnelClientContext, String sessionId,
      DataStore dataStore,
      String destHost, int localPort) {
    this.tunnelClientContext = tunnelClientContext;
    this.sessionId = sessionId;
    this.dataStore = dataStore;
    this.destHost = destHost;
    this.localPort = localPort;
    this.clientHttpEventLoopGroup = clientHttpEventLoopGroup;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext embeddedChannelContext, FullHttpRequest msg) throws Exception {
    FullHttpRequest request = msg.retainedDuplicate();
    try {
      localHttpRequest(msg, request);
      embeddedChannelContext.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void localHttpRequest(FullHttpRequest fullHttpRequest, FullHttpRequest request) throws Exception {
    String requestId = System.currentTimeMillis() + "";
    dataStore.add(requestId, request);
    Bootstrap b = new Bootstrap();
    b.group(clientHttpEventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_REUSEADDR, true)
        .remoteAddress(new InetSocketAddress(destHost, localPort)).handler(
            new ChannelInitializer<SocketChannel>() {
              @Override
              protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline p = socketChannel.pipeline();
                p.addLast("codec", new HttpClientCodec());
                p.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
                p.addLast("handler", new LocalHttpResponseHandler(tunnelClientContext, sessionId));
              }
            });
    Channel channel = b.connect().sync().channel();
    ChannelFuture f = channel.writeAndFlush(fullHttpRequest);
    f.addListener(future -> {
      if (!future.isSuccess()) {
        future.cause().printStackTrace();
      } else {
        System.out.println("Finished Writing Full Http Request to " + channel.id().toString());
      }
    });
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    super.exceptionCaught(ctx, cause);
  }
}






