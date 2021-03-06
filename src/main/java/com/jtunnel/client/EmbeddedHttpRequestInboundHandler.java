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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmbeddedHttpRequestInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private final ChannelHandlerContext tunnelClientContext;
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
    log.info("Received http request for uri={}", request.uri());
    try {
      localHttpRequest(msg, request);
      embeddedChannelContext.close();
    } catch (Exception e) {
      log.error("Exception while making local http request", e);
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
        log.debug("Finished Writing Full Http Request to " + channel.id().toString());
      }
    });
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    super.exceptionCaught(ctx, cause);
  }
}






