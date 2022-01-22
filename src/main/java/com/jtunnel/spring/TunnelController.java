package com.jtunnel.spring;


import com.jtunnel.client.TunnelClientManager;
import com.jtunnel.client.TunnelClientMessageHandler;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.GenericWebApplicationContext;

@RestController
@Slf4j
public class TunnelController {

  private final GenericWebApplicationContext applicationContext;
  private final TunnelClientManager tunnelClientManager;

  public TunnelController(GenericWebApplicationContext applicationContext, TunnelClientManager tunnelClientManager) {
    this.applicationContext = applicationContext;
    this.tunnelClientManager = tunnelClientManager;
  }


  @PostMapping("/tunnel/create")
  public String createTunnel(@RequestBody RegisterRequest registerRequest) {
    TunnelClientMessageHandler tunnelClientMessageHandler =
        applicationContext.getBean(TunnelClientMessageHandler.class);
    try {
      AtomicReference<Throwable> error =
          tunnelClientManager.register(registerRequest.getSubdomain(), registerRequest.getPort(),
              tunnelClientMessageHandler);
      if (error.get() != null) {
        return error.get().getMessage();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return "Success";
  }
}
