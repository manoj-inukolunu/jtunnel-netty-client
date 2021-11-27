package com.jtunnel.spring;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {




  @Bean
  public ObjectMapper mapper() {
    return new ObjectMapper();
  }
}
