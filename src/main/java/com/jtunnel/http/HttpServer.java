package com.jtunnel.http;


import com.jtunnel.data.DataStore;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import java.net.InetSocketAddress;
import lombok.extern.java.Log;

@Log
public class HttpServer implements Runnable {


  private final DataStore dataStore;

  public HttpServer(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  @Override
  public void run() {
    log.info("Starting Http Server");
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(group).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(5050)).childHandler(
          new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
              socketChannel.pipeline().addLast(new HttpServerCodec());
              socketChannel.pipeline().addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
              socketChannel.pipeline().addLast(new HttpRequestHandler(dataStore));

            }
          });
      ChannelFuture f = b.bind().sync();
      f.channel().closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      try {
        group.shutdownGracefully().sync();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
