package com.jtunnel.client;


import com.google.common.primitives.Bytes;
import com.jtunnel.data.DataStore;
import com.jtunnel.proto.MessageType;
import com.jtunnel.proto.ProtoMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.support.GenericWebApplicationContext;


@Slf4j
public final class TunnelClientMessageHandler extends ChannelInboundHandlerAdapter {

  private final DataStore dataStore;
  private final String destHost;
  private final EventLoopGroup clientHttpEventLoopGroup;
  private final GenericWebApplicationContext applicationContext;
  private ChannelHandlerContext channelHandlerContext;
  private final Map<String, Map<String, List<ProtoMessage>>> domainMap = new ConcurrentHashMap<>();
  public final Map<String, Integer> subDomainToPortMap = new ConcurrentHashMap<>();


  public TunnelClientMessageHandler(EventLoopGroup clientHttpGroup, String destHost, DataStore dataStore,
      GenericWebApplicationContext context) {
    this.dataStore = dataStore;
    this.destHost = destHost;
    this.clientHttpEventLoopGroup = clientHttpGroup;
    applicationContext = context;
  }


  @Override
  public void channelActive(ChannelHandlerContext tunnelClientContext) {
    this.channelHandlerContext = tunnelClientContext;
    applicationContext.registerBean(TunnelClientMessageHandler.class, () -> this);
  }

  @Override
  public void channelRead(ChannelHandlerContext tunnelClientContext, Object obj) {
    ProtoMessage msg = (ProtoMessage) obj;
    log.debug("Received Message {}", msg);
    if (msg.getMessageType().equals(MessageType.FIN)) {
      //initiate local http request
      log.info("Initiating Http Request for  subdomain={}", msg.getSubDomain());
      HttpRequestDecoder decoder = new HttpRequestDecoder();
      EmbeddedChannel embeddedChannel = new EmbeddedChannel(decoder, new HttpObjectAggregator(Integer.MAX_VALUE),
          new EmbeddedHttpRequestInboundHandler(clientHttpEventLoopGroup, tunnelClientContext, msg.getSessionId(),
              dataStore, destHost, subDomainToPortMap.get(msg.getSubDomain())));
      String subDomain = msg.getSubDomain();
      Map<String, List<ProtoMessage>> messagesMap = domainMap.get(subDomain);
      if (messagesMap.containsKey(msg.getSessionId())) {
        log.info("Writing to embedded channel with sessionId={}", msg.getSessionId());
        embeddedChannel.writeInbound(Unpooled.copiedBuffer(getBytes(messagesMap.get(msg.getSessionId()))));
      } else {
        log.info("Message Map={}", domainMap);
        log.warn("Not writing for sessionId={}", msg.getSessionId());
      }
      embeddedChannel.close();
      domainMap.get(subDomain).remove(msg.getSessionId());
    } else {
      log.info("Saving message with sessionId={}", msg.getSessionId());
      Map<String, List<ProtoMessage>> messagesMap =
          domainMap.getOrDefault(msg.getSubDomain(), new ConcurrentHashMap<>());
      List<ProtoMessage> messagesList = messagesMap.getOrDefault(msg.getSessionId(), new ArrayList<>());
      messagesList.add(msg);
      messagesMap.put(msg.getSessionId(), messagesList);
      domainMap.put(msg.getSubDomain(), messagesMap);
    }
    log.debug("Received Message " + msg.getSessionId());
    ProtoMessage message = new ProtoMessage();
    message.setSessionId(msg.getSessionId());
    message.setMessageType(MessageType.ACK);
    tunnelClientContext.writeAndFlush(message);
  }


  private ByteBuf getBytes(List<ProtoMessage> protoMessages) {
    try {
      byte[] data = protoMessages.get(0).getBody().getBytes(StandardCharsets.UTF_8);
      for (int i = 1; i < protoMessages.size(); i++) {
        data = Bytes.concat(data, protoMessages.get(i).getBody().getBytes());
      }
      return Unpooled.copiedBuffer(data);
    } catch (RuntimeException e) {
      log.error("Data = {}", protoMessages);
    }
    return Unpooled.copiedBuffer(new byte[] {});
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext tunnelClientContext, Throwable cause) throws Exception {
    super.exceptionCaught(tunnelClientContext, cause);
    cause.printStackTrace();
  }

  @Override
  public void channelInactive(ChannelHandlerContext tunnelClientContext) throws Exception {
    super.channelInactive(tunnelClientContext);
    log.info("Channel Inactive");
  }

  public ChannelHandlerContext getChannelHandlerContext() {
    return channelHandlerContext;
  }

  public void updateSubDomainPortMapping(String subDomain, Integer port) {
    subDomainToPortMap.put(subDomain, port);
  }
}







