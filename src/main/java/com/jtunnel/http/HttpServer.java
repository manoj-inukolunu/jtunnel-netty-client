package com.jtunnel.http;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtunnel.data.DataStore;
import com.jtunnel.data.MapDbDataStore;
//import com.jtunnel.data.RocksDbDataStore;
import com.jtunnel.netty.LocalClientHandler;
import java.util.Collections;
import lombok.extern.java.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.context.support.GenericApplicationContext;

@Log
public class HttpServer implements Runnable {


  private final MapDbDataStore dataStore;
  private final LocalClientHandler handler;
  private int localPort;
  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.setSerializationInclusion(Include.NON_NULL);
  }

  public HttpServer(MapDbDataStore dataStore, LocalClientHandler channel, int localPort) {
    this.dataStore = dataStore;
    this.handler = channel;
    this.localPort = localPort;
  }


  @Override
  public void run() {
    log.info("Starting Http Server");
    SpringApplication app = new SpringApplication(SpringServer.class);
    app.setDefaultProperties(Collections.singletonMap("server.port", "5050"));
    GenericApplicationContext applicationContext = (GenericApplicationContext) app.run();
    applicationContext.publishEvent(dataStore);
    applicationContext.publishEvent(handler);
    applicationContext.publishEvent(mapper);
    applicationContext.publishEvent(localPort);
  }
}
