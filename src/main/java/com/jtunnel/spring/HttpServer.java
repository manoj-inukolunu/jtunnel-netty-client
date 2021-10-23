package com.jtunnel.spring;


import com.jtunnel.data.DataStore;
import com.jtunnel.data.MapDbDataStore;
import com.jtunnel.netty.TunnelConfig;
import java.util.Objects;
import java.util.Properties;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

@Log
@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.jtunnel.netty", "com.jtunnel.spring"})
@EnableAutoConfiguration
public class HttpServer {

  public static void main(String args[]) throws Exception {
    log.info("Starting Http Server");
    if (args.length != 4) {
      System.out.println(
          "Usage : java -jar jTunnel.jar <destination-host> <destination-port> <subdomain> <db storage location>");
      return;
    }
    SpringApplication application = new SpringApplication(HttpServer.class);
    Properties properties = new Properties();
    properties.put("server.port", "5050");
    properties.put("tunnel.dest-host", args[0]);
    properties.put("tunnel.dest-port", args[1]);
    properties.put("tunnel.server-host", args[2]);
    properties.put("tunnel.server-port", 1234);
    properties.put("data-store-location", args[3]);
    application.setDefaultProperties(properties);
    application.run(args);
  }

  @Bean
  public TunnelConfig tunnelConfig(Environment environment) {
    TunnelConfig tunnelConfig = new TunnelConfig();
    tunnelConfig.setServerHost(environment.getProperty("tunnel.server-host") + ".jtunnel.net");
    tunnelConfig.setServerPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("tunnel.server-port"))));
    tunnelConfig.setDestHost(environment.getProperty("tunnel.dest-host"));
    tunnelConfig.setDestPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("tunnel.dest-port"))));
    return tunnelConfig;
  }

  @Bean
  public DataStore dataStore(Environment environment) {
    return new MapDbDataStore(environment.getProperty("data-store-location"));
  }
}
