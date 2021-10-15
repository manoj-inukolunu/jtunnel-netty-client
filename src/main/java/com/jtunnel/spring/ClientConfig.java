package com.jtunnel.spring;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtunnel.data.DataStore;
import com.jtunnel.data.MapDbDataStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {




  @Bean
  public ObjectMapper mapper() {
    return new ObjectMapper();
  }
}
