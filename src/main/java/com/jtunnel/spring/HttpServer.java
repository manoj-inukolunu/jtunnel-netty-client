package com.jtunnel.spring;


import com.jtunnel.data.DataStore;
import com.jtunnel.data.MapDbDataStore;
import com.jtunnel.netty.TunnelConfig;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
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
@ComponentScan(basePackages = {"com.jtunnel.netty", "com.jtunnel.spring", "com.jtunnel.file"})
@EnableAutoConfiguration
public class HttpServer {

  public static void main(String args[]) throws Exception {
    log.info("Starting Http Server");
    String runAs = args[0];
    SpringApplication application = new SpringApplication(HttpServer.class);
    if (runAs.equalsIgnoreCase("share")) {
      if (args.length != 2) {
        System.out.println(
            "Usage : java -jar jTunnel.jar <share|tunnel> <fileLocation>");
        return;
      }
      Properties properties = new Properties();
      properties.put("mode", "share");
      //https://8b5b81e8-f29a-4777-b38a-7a90d5b0bf3f.jtunnel.net/
      properties.put("tunnel.server-host", "8b5b81e8-f29a-4777-b38a-7a90d5b0bf3f");
      properties.put("tunnel.server-port", 1234);
      properties.put("file", args[1]);
      properties.put("tunnel.dest-host", "localhost");
      properties.put("tunnel.dest-port", "5050");
      properties.put("server.port", "5050");
      application.setDefaultProperties(properties);
      application.run(args);
    } else if (runAs.equalsIgnoreCase("tunnel")) {
      if (args.length != 4) {
        System.out.println(
            "Usage : java -jar jTunnel.jar <destination-host> <destination-port> <subdomain> <db storage location>");
        return;
      }
      Properties properties = new Properties();
      properties.put("mode", "tunnel");
      properties.put("server.port", "5050");
      properties.put("tunnel.dest-host", args[0]);
      properties.put("tunnel.dest-port", args[1]);
      properties.put("tunnel.server-host", args[2]);
      properties.put("tunnel.server-port", 1234);
      properties.put("data-store-location", args[3]);
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
  public DataStore dataStore(Environment environment) {
    String mode = environment.getProperty("mode");
    if (Objects.requireNonNull(mode).equalsIgnoreCase("tunnel")) {
      return new MapDbDataStore(environment.getProperty("data-store-location"));
    } else {
      return new DataStore() {
        @Override
        public void add(String requestId, FullHttpRequest request) throws Exception {

        }

        @Override
        public void saveFullTrace(String requestId, FullHttpResponse response) throws Exception {

        }

        @Override
        public HashMap<HttpRequest, HttpResponse> allRequests() throws Exception {
          return null;
        }

        @Override
        public HttpRequest get(String requestId) throws Exception {
          return null;
        }

        @Override
        public HttpResponse getResponse(String requestId) throws Exception {
          return null;
        }
      };
    }
  }
}
