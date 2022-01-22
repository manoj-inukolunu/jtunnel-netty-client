package com.jtunnel.client;


import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

import com.jtunnel.proto.MessageType;
import com.jtunnel.proto.ProtoMessage;
import io.netty.channel.ChannelFuture;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.GenericWebApplicationContext;

@Slf4j
@Component
public class TunnelClientManager {


  public AtomicReference<Throwable> register(String subDomain, Integer port,
      TunnelClientMessageHandler tunnelClientMessageHandler) throws InterruptedException {
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
        tunnelClientMessageHandler.updateSubDomainPortMapping(subDomain, port);
      } else {
        log.error("Error Registering", future1.cause());
        error.set(future1.cause());
      }
    });
    future.await();
    return error;
  }

}
