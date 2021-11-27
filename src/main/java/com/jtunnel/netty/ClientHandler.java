package com.jtunnel.netty;

import com.jtunnel.data.DataStore;
import com.jtunnel.data.index.SearchIndex;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import java.net.InetSocketAddress;
import lombok.extern.java.Log;

@Log
public class ClientHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


  private final DataStore dataStore;
  private final String registerAddress;
  private final int localPort;
  private final String destHost;

  public ClientHandler(String destHost, String registerAddress, DataStore dataStore, int localPort) {
    this.dataStore = dataStore;
    this.registerAddress = registerAddress;
    this.localPort = localPort;
    this.destHost = destHost;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    log.info("Registering with JTunnel Server with host " + registerAddress);
    ctx.writeAndFlush(Unpooled.copiedBuffer("register " + registerAddress, CharsetUtil.UTF_8));
    ctx.pipeline().addFirst(new HttpObjectAggregator(Integer.MAX_VALUE));
    ctx.pipeline().addFirst(new HttpServerCodec());
    super.channelActive(ctx);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    FullHttpRequest request = fullHttpRequest.retainedDuplicate();
    try {
      localHttpRequest(channelHandlerContext, fullHttpRequest, request);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void localHttpRequest(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest,
      FullHttpRequest request) throws Exception {
    String requestId = System.currentTimeMillis() + "";
    dataStore.add(requestId, request);
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap b = new Bootstrap();
    b.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress(destHost, localPort)).handler(
        new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline p = socketChannel.pipeline();
            p.addLast("log", new LoggingHandler(LogLevel.DEBUG));
            p.addLast("codec", new HttpClientCodec());
            p.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
            p.addLast("handler", new DestHttpResponseHandler(channelHandlerContext, requestId, dataStore));
          }
        });
    Channel channel = b.connect().sync().channel();
    ChannelFuture f = channel.writeAndFlush(fullHttpRequest);
    f.addListener(future -> {
      if (!future.isSuccess()) {
        future.cause().printStackTrace();
      }
    });
  }


}
