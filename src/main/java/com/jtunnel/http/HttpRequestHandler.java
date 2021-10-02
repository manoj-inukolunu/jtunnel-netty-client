package com.jtunnel.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jtunnel.data.DataStore;
import com.jtunnel.netty.LocalHttpResponseHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
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
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import lombok.extern.java.Log;

@Log
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


  private final DataStore dataStore;
  private static final ObjectMapper mapper = new ObjectMapper();
  private final int localPort;

  public HttpRequestHandler(DataStore dataStore, int localPort) {
    this.dataStore = dataStore;
    this.localPort = localPort;
  }

  private void localHttpRequest(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    String requestId = System.currentTimeMillis() + "";
    dataStore.add(requestId, fullHttpRequest);
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap b = new Bootstrap();
    b.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress("localhost", localPort)).handler(
        new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline p = socketChannel.pipeline();
            p.addLast("log", new LoggingHandler(LogLevel.DEBUG));
            p.addLast("codec", new HttpClientCodec());
            p.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
            p.addLast("handler", new LocalHttpResponseHandler(channelHandlerContext, requestId, dataStore));
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

  //TODO Refactor
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest)
      throws Exception {
    log.info(fullHttpRequest.uri());
    String path = fullHttpRequest.uri();
    if (path.startsWith("/js")) {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream(fullHttpRequest.uri().substring(1));
      ByteBuf content = Unpooled.copiedBuffer(is.readAllBytes());
      returnResponse(ctx, content, HttpResponseStatus.OK, "text/javascript");
    } else if (path.startsWith("/html")) {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream(fullHttpRequest.uri().substring(1));
      ByteBuf content = Unpooled.copiedBuffer(is.readAllBytes());
      returnResponse(ctx, content, HttpResponseStatus.OK, "text/html");
    } else if (path.startsWith("/rest/request")) {
      String requestId = path.substring(path.lastIndexOf("/") + 1);
      HttpRequest request = dataStore.get(requestId);
      ByteBuf content = Unpooled.copiedBuffer(request.toString().getBytes(StandardCharsets.UTF_8));
      returnResponse(ctx, content, HttpResponseStatus.OK, "application/json");
    } else if (path.startsWith("/rest/response")) {
      String requestId = path.substring(path.lastIndexOf("/") + 1);
      HttpResponse request = dataStore.getResponse(requestId);
      ByteBuf content = Unpooled.copiedBuffer(request.toString().getBytes(StandardCharsets.UTF_8));
      returnResponse(ctx, content, HttpResponseStatus.OK, "application/json");
    } else if (path.startsWith("/rest/replay")) {
      String requestId = path.substring(path.lastIndexOf("/") + 1);
      HttpRequest request = dataStore.get(requestId);
      FullHttpRequest fullRequest =
          new DefaultFullHttpRequest(HttpVersion.valueOf(request.getVersion()),
              HttpMethod.valueOf(request.getMethod()), request.getUri(),
              Unpooled.copiedBuffer(request.getContent().getBytes(StandardCharsets.UTF_8)), request.getHeaders(),
              request.getTrailer());
      localHttpRequest(ctx, fullRequest);
    } else if (path.startsWith("/rest")) {
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

      ByteBuf content = Unpooled.copiedBuffer(mapper.writeValueAsString(objectNodes).getBytes(StandardCharsets.UTF_8));
      returnResponse(ctx, content, HttpResponseStatus.OK, "application/json");

    } else {
      ByteBuf content = Unpooled.copiedBuffer("Not Found".getBytes(StandardCharsets.UTF_8));
      returnResponse(ctx, content, HttpResponseStatus.NOT_FOUND, "text/html");
    }


  }

  private void returnResponse(ChannelHandlerContext ctx, ByteBuf content, HttpResponseStatus status,
      String contentType) {
    FullHttpResponse response =
        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
    ctx.writeAndFlush(response);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ByteBuf content =
        Unpooled.copiedBuffer(("Server Error + " + cause.getMessage()).getBytes(StandardCharsets.UTF_8));
    returnResponse(ctx, content, HttpResponseStatus.OK, "text/html");
    ctx.close();
  }
}
