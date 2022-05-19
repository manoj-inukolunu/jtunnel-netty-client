package com.jtunnel.spring;


import com.jtunnel.client.Tunnel;
import com.jtunnel.client.TunnelClientManager;
import com.jtunnel.data.SearchableDataStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TunnelController {

  private final TunnelClientManager tunnelClientManager;


  public TunnelController(TunnelClientManager tunnelClientManager) {
    this.tunnelClientManager = tunnelClientManager;
  }

  @Scheduled(initialDelay = 1000, fixedDelay = 5000)
  public void heartbeat() {
//    log.info("Ping");
    tunnelClientManager.ping();
  }

  @PostMapping("/tunnel/create")
  public String createTunnel(@RequestBody RegisterRequest registerRequest) {
    try {
      AtomicReference<Throwable> error =
          tunnelClientManager.register(registerRequest.getTunnelName(), registerRequest.getSubdomain(),
              registerRequest.getPort());
      if (error.get() != null) {
        return error.get().getMessage();
      }


    } catch (Exception e) {
      e.printStackTrace();
    }
    return "Success";
  }

  @GetMapping("/tunnel/all")
  public Set<Tunnel> allTunnels() {
    return tunnelClientManager.getTunnels();
  }
}
