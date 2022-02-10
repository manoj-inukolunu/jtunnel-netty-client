package com.jtunnel.client;


import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

import com.jtunnel.proto.MessageType;
import com.jtunnel.proto.ProtoMessage;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.GenericWebApplicationContext;

@Slf4j
@Component
public class TunnelClientManager {

  private TunnelClientMessageHandler tunnelClientMessageHandler;
  private final GenericWebApplicationContext applicationContext;
  private Map<String, Integer> registeredSubDomains = new ConcurrentHashMap<>();
  private List<Tunnel> tunnels = new CopyOnWriteArrayList<>();

  public List<Tunnel> getTunnels() {
    return tunnels;
  }

  public TunnelClientManager(GenericWebApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public AtomicReference<Throwable> register(String name, String subDomain, Integer port) throws InterruptedException {
    if (tunnelClientMessageHandler == null) {
      tunnelClientMessageHandler =
          applicationContext.getBean(TunnelClientMessageHandler.class);
    }
    ProtoMessage registerMessage = new ProtoMessage();
    registerMessage.setAttachments(new HashMap<>());
    registerMessage.setMessageType(MessageType.REGISTER);
    registerMessage.setSubDomain(subDomain);
    registerMessage.setSessionId(UUID.randomUUID().toString());
    ChannelFuture future = tunnelClientMessageHandler.getChannelHandlerContext().writeAndFlush(registerMessage);
    future.addListener(FIRE_EXCEPTION_ON_FAILURE);
    AtomicReference<Throwable> error = new AtomicReference<>();
    future.addListener(future1 -> {
      if (future1.isSuccess()) {
        log.info("Successfully registered {} ", subDomain);
        registeredSubDomains.put(subDomain, port);
        tunnels.add(new Tunnel(name, subDomain, port));
        tunnelClientMessageHandler.updateSubDomainPortMapping(subDomain, port);
      } else {
        log.error("Error Registering", future1.cause());
        error.set(future1.cause());
      }
    });
    future.await();
    return error;
  }

  public void ping() {
    if (tunnelClientMessageHandler == null) {
      tunnelClientMessageHandler = applicationContext.getBean(TunnelClientMessageHandler.class);
    }
    ChannelFuture future = tunnelClientMessageHandler.getChannelHandlerContext().writeAndFlush(ProtoMessage.ping());
    future.addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) {
        if (!future.isSuccess()) {
          future.cause().printStackTrace();
        } else {
//          log.info("Alive");
        }
      }
    });
  }
}
