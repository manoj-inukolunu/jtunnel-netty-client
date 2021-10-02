package com.jtunnel.netty;

import com.jtunnel.data.DataStore;
import com.jtunnel.data.MapDbDataStore;
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
  private int localPort;

  public JTunnelClient(String host, int port, int localPort) {
    this.host = host;
    this.port = port;
    this.localPort = localPort;
  }


  public void startClientTunnel(DataStore dataStore) throws Exception {
    log.info("Starting JTunnel Client");
    EventLoopGroup group = new NioEventLoopGroup();
    LocalClientHandler handler = new LocalClientHandler(host, dataStore, localPort);
    try {
      Bootstrap b = new Bootstrap();
      b.option(ChannelOption.SO_KEEPALIVE, true).group(group).channel(NioSocketChannel.class)
          .remoteAddress(new InetSocketAddress(host, port))
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
              socketChannel.pipeline().addLast(handler);
            }
          });
      ChannelFuture f = b.connect().sync();
      new Thread(new HttpServer(dataStore, handler, localPort)).start();
      f.channel().closeFuture().sync();
    } finally {
      group.shutdownGracefully().sync();
    }
  }


  public static void main(String args[]) throws Exception {
    if (args.length != 2) {
      log.info("Need a subdomain and local port to connect  Usage : java -jar jtunnel.jar subdomain port ");
      return;
    }
    DataStore dataStore = new MapDbDataStore();
    String host = args[0] + ".jtunnel.net";
    JTunnelClient tunnelClient = new JTunnelClient(host, 1234, Integer.parseInt(args[1]));
    tunnelClient.startClientTunnel(dataStore);
  }
}
