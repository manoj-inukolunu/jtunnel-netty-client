package com.jtunnel.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jtunnel.data.DataStore;
import com.jtunnel.netty.LocalHttpResponseHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import lombok.extern.java.Log;

@Log
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


  private final DataStore dataStore;
  private static final ObjectMapper mapper = new ObjectMapper();

  public HttpRequestHandler(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  private void localHttpRequest(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest)
      throws Exception {
    String requestId = System.currentTimeMillis() + "";
    dataStore.add(requestId, fullHttpRequest);
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap b = new Bootstrap();
    b.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress("localhost", 8080)).handler(
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
      FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
      response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/javascript");
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
      ctx.writeAndFlush(response);
    } else if (path.startsWith("/html")) {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream(fullHttpRequest.uri().substring(1));
      ByteBuf content = Unpooled.copiedBuffer(is.readAllBytes());
      FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
      response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
      ctx.writeAndFlush(response);
    } else if (path.startsWith("/rest/request")) {
      String requestId = path.substring(path.lastIndexOf("/") + 1);
      HttpRequest request = dataStore.get(requestId);
      ByteBuf content = Unpooled.copiedBuffer(request.toString().getBytes(StandardCharsets.UTF_8));
      FullHttpResponse response =
          new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
      response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
      ctx.writeAndFlush(response);
    } else if (path.startsWith("/rest/response")) {
      String requestId = path.substring(path.lastIndexOf("/") + 1);
      HttpResponse request = dataStore.getResponse(requestId);
      ByteBuf content = Unpooled.copiedBuffer(request.toString().getBytes(StandardCharsets.UTF_8));
      FullHttpResponse response =
          new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
      response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
      ctx.writeAndFlush(response);
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
      List<ObjectNode> list = new ArrayList<>();
      for (HttpRequest request : data.keySet()) {
        ObjectNode object = mapper.createObjectNode();
        object.put("requestId", request.getRequestId());
        object.put("requestTime", String.valueOf(new Date(Long.parseLong(request.getRequestId()))));
        object.put("line", request.getInitialLine());
        list.add(object);
      }
      ByteBuf content = Unpooled.copiedBuffer(mapper.writeValueAsString(list).getBytes(StandardCharsets.UTF_8));
      FullHttpResponse response =
          new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
      response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
      ctx.writeAndFlush(response);

    } else {
      ByteBuf content = Unpooled.copiedBuffer("Not Found".getBytes(StandardCharsets.UTF_8));
      FullHttpResponse response =
          new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, content);
      response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
      ctx.writeAndFlush(response);
    }


  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ByteBuf content =
        Unpooled.copiedBuffer(("Server Error + " + cause.getMessage()).getBytes(StandardCharsets.UTF_8));
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
    ctx.writeAndFlush(response);
    ctx.close();
  }
}
