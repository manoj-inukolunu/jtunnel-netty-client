package com.jtunnel.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jtunnel.data.DataStore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    } else if (path.startsWith("/rest")) {
      HashMap<HttpRequest, HttpResponse> data = dataStore.allRequests();
      List<ObjectNode> list = new ArrayList<>();
      for (HttpRequest request : data.keySet()) {
        ObjectNode object = mapper.createObjectNode();
        object.put("requestId", request.getRequestId());
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
    ByteBuf content = Unpooled.copiedBuffer(("Server Error + " + cause.getMessage()).getBytes(StandardCharsets.UTF_8));
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
    ctx.writeAndFlush(response);
    ctx.close();
  }
}
