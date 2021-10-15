package com.jtunnel.netty;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "tunnel")
@Getter
@Setter
@Configuration
public class TunnelConfig {

  private String destHost;
  private String serverHost;
  private int destPort;
  private int serverPort;
}
