package com.jtunnel.client;


import com.google.common.primitives.Bytes;
import com.jtunnel.data.DataStore;
import com.jtunnel.proto.MessageType;
import com.jtunnel.proto.ProtoMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TunnelClientMessageHandler extends SimpleChannelInboundHandler<ProtoMessage> {

  private final DataStore dataStore;
  private final int localPort;
  private final String destHost;
  private final String subDomain;
  private final EventLoopGroup clientHttpEventLoopGroup;


  public TunnelClientMessageHandler(EventLoopGroup clientHttpGroup, String destHost,
      DataStore dataStore, int localPort, String subDomain) {
    this.dataStore = dataStore;
    this.localPort = localPort;
    this.destHost = destHost;
    this.subDomain = subDomain;
    this.clientHttpEventLoopGroup = clientHttpGroup;
  }

  private final Map<String, List<ProtoMessage>> map = new ConcurrentHashMap<>();

  @Override
  public void channelActive(ChannelHandlerContext tunnelClientContext) throws Exception {
    ProtoMessage registerMessage = new ProtoMessage();
    registerMessage.setAttachments(new HashMap<>());
    registerMessage.addAttachment("subdomain", subDomain);
    registerMessage.setMessageType(MessageType.REGISTER);
    registerMessage.setSessionId(UUID.randomUUID().toString());
    tunnelClientContext.writeAndFlush(registerMessage);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext tunnelClientContext, ProtoMessage msg) throws Exception {
    if (msg.getMessageType().equals(MessageType.FIN)) {
      //initiate local http request
      HttpRequestDecoder decoder = new HttpRequestDecoder();
      EmbeddedChannel embeddedChannel =
          new EmbeddedChannel(decoder, new HttpObjectAggregator(Integer.MAX_VALUE),
              new EmbeddedHttpRequestInboundHandler(clientHttpEventLoopGroup,tunnelClientContext, msg.getSessionId(), dataStore, destHost,
                  localPort));
      embeddedChannel.writeInbound(Unpooled.copiedBuffer(getBytes(map.get(msg.getSessionId()))));
      embeddedChannel.close();
    } else {
      List<ProtoMessage> messages = map.getOrDefault(msg.getSessionId(), new ArrayList<>());
      messages.add(msg);
      map.put(msg.getSessionId(), messages);
    }
    System.out.println("Received Message " + msg.getSessionId());
    ProtoMessage message = new ProtoMessage();
    message.setSessionId(msg.getSessionId());
    message.setMessageType(MessageType.ACK);
    tunnelClientContext.writeAndFlush(message);
  }


  private ByteBuf getBytes(List<ProtoMessage> protoMessages) {
    byte[] data = protoMessages.get(0).getBody().getBytes(StandardCharsets.UTF_8);
    for (int i = 1; i < protoMessages.size(); i++) {
      data = Bytes.concat(data, protoMessages.get(i).getBody().getBytes());
    }
    return Unpooled.copiedBuffer(data);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext tunnelClientContext, Throwable cause) throws Exception {
    super.exceptionCaught(tunnelClientContext, cause);
    cause.printStackTrace();
  }

  @Override
  public void channelInactive(ChannelHandlerContext tunnelClientContext) throws Exception {
    super.channelInactive(tunnelClientContext);
    System.out.println("Channel Inactive");
  }
}







