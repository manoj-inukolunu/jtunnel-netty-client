package com.jtunnel.spring;


import com.jtunnel.data.DataStore;
import com.jtunnel.data.SearchableDataStore;
import com.jtunnel.data.SearchableMapDbDataStore;
import com.jtunnel.netty.TunnelConfig;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import lombok.extern.java.Log;
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
@ComponentScan(basePackages = {"com.jtunnel.netty", "com.jtunnel.spring", "com.jtunnel.file"})
@EnableAutoConfiguration
public class HttpServer {

  public static void main(String args[]) throws Exception {
    log.info("Starting Http Server");
    if (args.length <= 0) {
      System.out.println(
          "Usage : java -jar jTunnel.jar <mode:share|tunnel> <destination-host> <destination-port> <subdomain> <db storage location>");
      return;
    }
    String runAs = args[0];
    SpringApplication application = new SpringApplication(HttpServer.class);
    if (runAs.equalsIgnoreCase("share")) {
      if (args.length != 2) {
        System.out.println(
            "Usage : java -jar jTunnel.jar share <fileLocation>");
        return;
      }
      Properties properties = new Properties();
      properties.put("mode", "share");
      properties.put("tunnel.server-host", UUID.randomUUID().toString());
      properties.put("tunnel.server-port", 8585);
      properties.put("file", args[1]);
      properties.put("tunnel.dest-host", "localhost");
      properties.put("tunnel.dest-port", "5050");
      properties.put("server.port", "5050");
      application.setDefaultProperties(properties);
      application.run(args);
    } else if (runAs.equalsIgnoreCase("tunnel")) {
      if (args.length != 6) {
        System.out.println(
            "Usage : java -jar jTunnel.jar <destination-host> <destination-port> <subdomain> <db storage location> stats-port");
        return;
      }
      Properties properties = new Properties();
      properties.put("mode", "tunnel");
      properties.put("tunnel.dest-host", args[1]);
      properties.put("tunnel.dest-port", args[2]);
      properties.put("tunnel.server-host", args[3]);
      properties.put("tunnel.server-port", 8585);
      properties.put("data-store-location", args[4]);
      properties.put("server.port", args[5]);
      application.setDefaultProperties(properties);
      application.run(args);
    }
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
  public SearchableDataStore dataStore(Environment environment) {
    String mode = environment.getProperty("mode");
    if (Objects.requireNonNull(mode).equalsIgnoreCase("tunnel")) {
      return new SearchableMapDbDataStore(environment.getProperty("data-store-location"));
    } else {
      return null;
    }
  }
}
