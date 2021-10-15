package com.jtunnel.spring;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jtunnel.data.DataStore;
import com.jtunnel.netty.LocalClientHandler;
import com.jtunnel.netty.LocalHttpResponseHandler;
import com.jtunnel.netty.TunnelConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log
public class RequestRestController {

  @Autowired
  private DataStore dataStore;
  @Autowired
  private ObjectMapper mapper;
  @Autowired
  TunnelConfig tunnelConfig;

  @GetMapping("/rest/test")
  public String test() {
    return "test";
  }

  @GetMapping("/rest/history")
  public Response getHistory(@RequestParam("start") int start, @RequestParam("end") int end) throws Exception {
    HashMap<HttpRequest, HttpResponse> data = dataStore.allRequests();
    List<HttpRequest> list = new ArrayList<>(data.keySet());
    list.sort((o1, o2) -> -Long.compare(Long.parseLong(o1.requestId), Long.parseLong(o2.requestId)));

    List<ObjectNode> objectNodes = new ArrayList<>();
    for (HttpRequest request : list) {
      ObjectNode object = mapper.createObjectNode();
      object.put("requestId", request.getRequestId());
      object.put("requestTime", String.valueOf(new Date(Long.parseLong(request.getRequestId()))));
      object.put("line", request.getInitialLine());
      objectNodes.add(object);
    }

    Response response = new Response();
    response.setTotal(list.size());
    response.setCountPerPage(end - start);
    response.setList(objectNodes.subList(start, end >= objectNodes.size() ? objectNodes.size() - 1 : end));
    return response;
  }

  @GetMapping("/rest/request/{requestId}")
  public String getRequest(@PathVariable("requestId") String requestId) throws Exception {
    return dataStore.get(requestId).toString();
  }

  @GetMapping("/rest/response/{requestId}")
  public String getResponse(@PathVariable("requestId") String requestId) throws Exception {
    return dataStore.getResponse(requestId).toString();
  }

  @GetMapping("/rest/replay/{requestId}")
  public void replayRequest(@PathVariable("requestId") String requestId) throws Exception {
    log.info("Replaying request= " + requestId);
    HttpRequest request = dataStore.get(requestId);
    FullHttpRequest fullRequest =
        new DefaultFullHttpRequest(HttpVersion.valueOf(request.getVersion()),
            HttpMethod.valueOf(request.getMethod()), request.getUri(),
            Unpooled.copiedBuffer(request.getContent().getBytes(StandardCharsets.UTF_8)), request.getHeaders(),
            request.getTrailer());
    localHttpRequest(fullRequest);
  }

  private void localHttpRequest(FullHttpRequest fullHttpRequest) throws Exception {
    String requestId = System.currentTimeMillis() + "";
    dataStore.add(requestId, fullHttpRequest);
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap b = new Bootstrap();
    b.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress(tunnelConfig.getDestHost(),
        tunnelConfig.getDestPort())).handler(
        new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline p = socketChannel.pipeline();
            p.addLast("log", new LoggingHandler(LogLevel.DEBUG));
            p.addLast("codec", new HttpClientCodec());
            p.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
            p.addLast("handler", new LocalHttpResponseHandler(null, requestId, dataStore));
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
