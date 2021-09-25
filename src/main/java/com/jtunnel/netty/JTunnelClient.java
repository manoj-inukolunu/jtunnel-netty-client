package com.jtunnel.netty;

import com.jtunnel.data.DataStore;
import com.jtunnel.data.RocksDbDataStore;
import com.jtunnel.http.HttpServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import lombok.extern.java.Log;

@Log
public class JTunnelClient {

  private int port;
  private String host;

  public JTunnelClient(String host, int port) {
    this.host = host;
    this.port = port;
  }


  public void startClientTunnel(DataStore dataStore) throws Exception {
    log.info("Starting JTunnel Client");
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.option(ChannelOption.SO_KEEPALIVE, true).group(group).channel(NioSocketChannel.class)
          .remoteAddress(new InetSocketAddress(host, port))
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
              socketChannel.pipeline().addLast(new LocalClientHandler(host,dataStore));
            }
          });
      ChannelFuture f = b.connect().sync();
      f.channel().closeFuture().sync();
    } finally {
      group.shutdownGracefully().sync();
    }
  }


  public static void main(String args[]) throws Exception {
    if (args.length != 1) {
      log.info("Need a subdomain to connect");
      return;
    }
    DataStore dataStore = new RocksDbDataStore();
    String host = args[0] + ".jtunnel.net";
    JTunnelClient tunnelClient = new JTunnelClient(host, 1234);
    new Thread(new HttpServer(dataStore)).start();
    tunnelClient.startClientTunnel(dataStore);
  }
}
