package com.jtunnel.netty;

import com.google.common.base.Stopwatch;
import com.jtunnel.client.TunnelClientMessageHandler;
import com.jtunnel.data.SearchableDataStore;
import com.jtunnel.spring.HttpRequest;
import com.jtunnel.spring.HttpResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.GenericWebApplicationContext;

@Log
@Component
public class JTunnelClient {


  private final GenericWebApplicationContext context;
  private final SearchableDataStore dataStore;
  private final TunnelConfig tunnelConfig;
  private final EventLoopGroup clientHttpGroup = new NioEventLoopGroup();


  public JTunnelClient(TunnelConfig tunnelConfig, SearchableDataStore dataStore, GenericWebApplicationContext context) {
    this.tunnelConfig = tunnelConfig;
    this.dataStore = dataStore;
    this.context = context;
  }


  @PostConstruct
  public void startClientTunnel() throws Exception {
    buildIndex(dataStore);

    new Thread(() -> {
      try {
        log.info("Starting JTunnel Client");
        EventLoopGroup group = new NioEventLoopGroup();
        try {
          final SslContext sslCtx = SslContextBuilder.forClient()
              .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
          Bootstrap b = new Bootstrap();
          String host = "localhost";//tunnelConfig.getServerHost();
          b.option(ChannelOption.SO_KEEPALIVE, true).group(group).channel(NioSocketChannel.class)
              .remoteAddress(new InetSocketAddress(host, tunnelConfig.getServerPort()))
              .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                  ChannelPipeline pipeline = socketChannel.pipeline();
                  pipeline.addLast(sslCtx.newHandler(socketChannel.alloc(), host, tunnelConfig.getServerPort()));
//                  pipeline.addLast(new IdleStateHandler(1, 2, 0));
                  pipeline.addLast(new ObjectEncoder());
                  pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                  pipeline.addLast(
                      new TunnelClientMessageHandler(clientHttpGroup, tunnelConfig.getDestHost(), dataStore, context));
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
      /*for (HttpRequest request : map.keySet()) {
        dataStore.indexJsonContent(request.getContent());
      }*/
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
