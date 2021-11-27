package com.jtunnel.netty;

import com.google.common.base.Stopwatch;
import com.jtunnel.data.DataStore;
import com.jtunnel.data.SearchableDataStore;
import com.jtunnel.data.SearchableMapDbDataStore;
import com.jtunnel.data.index.SearchIndex;
import com.jtunnel.spring.HttpRequest;
import com.jtunnel.spring.HttpResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log
@Component
public class JTunnelClient {


  private final SearchableDataStore dataStore;
  private final TunnelConfig tunnelConfig;


  public JTunnelClient(TunnelConfig tunnelConfig, SearchableDataStore dataStore) {
    this.tunnelConfig = tunnelConfig;
    this.dataStore = dataStore;
  }


  @PostConstruct
  public void startClientTunnel() throws Exception {
    buildIndex(dataStore);
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

  public static void buildIndex(SearchableDataStore dataStore) {
    try {
      Stopwatch stopwatch = Stopwatch.createStarted();
      HashMap<HttpRequest, HttpResponse> map = dataStore.allRequests();
      map.forEach(dataStore::index);
      log.info("Time Taken to Index all requests=" + stopwatch.elapsed(TimeUnit.SECONDS));
      stopwatch.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

//  public static void main(String[] args) {
//    SearchableDataStore dataStore = new SearchableMapDbDataStore("/Users/manoj");
//    JTunnelClient.buildIndex(dataStore);
//    Stopwatch stopwatch = Stopwatch.createStarted();
//    List<String> list = dataStore.search(Arrays.asList("/sentiment-collector/"));
//    log.info("Time Taken to search all requests=" + stopwatch.elapsed(TimeUnit.SECONDS));
//    System.out.println(list.size());
//  }


}
