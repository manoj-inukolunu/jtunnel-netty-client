package com.jtunnel.spring;


import com.jtunnel.data.DataStore;
import com.jtunnel.data.MapDbDataStore;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@Log
@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.jtunnel.netty", "com.jtunnel.spring"})
@EnableAutoConfiguration
public class HttpServer {


  public static void main(String args[]) throws Exception {
    log.info("Starting Http Server");
    new SpringApplication(HttpServer.class).run(args);
  }

  @Bean
  public DataStore dataStore(@Value("${data-store-location}") String dir) {
    return new MapDbDataStore(dir);
  }
}
