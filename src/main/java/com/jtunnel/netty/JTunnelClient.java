package com.jtunnel.netty;

import com.jtunnel.data.DataStore;
import com.jtunnel.data.MapDbDataStore;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import javax.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

@Log
@Component
public class JTunnelClient {


  private final DataStore dataStore;
  private final TunnelConfig tunnelConfig;


  public JTunnelClient(TunnelConfig tunnelConfig, DataStore dataStore) {
    this.tunnelConfig = tunnelConfig;
    this.dataStore = dataStore;
  }


  @PostConstruct
  public void startClientTunnel() throws Exception {
    new Thread(() -> {
      try {
        log.info("Starting JTunnel Client");
        EventLoopGroup group = new NioEventLoopGroup();
        ClientHandler handler =
            new ClientHandler(tunnelConfig.getDestHost(), tunnelConfig.getServerHost(), dataStore,
                tunnelConfig.getDestPort());
        try {
          Bootstrap b = new Bootstrap();
          b.option(ChannelOption.SO_KEEPALIVE, true).group(group).channel(NioSocketChannel.class)
              .remoteAddress(new InetSocketAddress(tunnelConfig.getServerHost(), tunnelConfig.getServerPort()))
              .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                  socketChannel.pipeline().addLast(handler);
                }
              });
          ChannelFuture f = b.connect().sync();
          f.channel().closeFuture().sync();
        } finally {
          group.shutdownGracefully().sync();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
  }


}
