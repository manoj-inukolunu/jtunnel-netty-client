package com.jtunnel.netty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TunnelConfig {

  private String destHost;
  private String serverHost;
  private int destPort;
  private int serverPort;
}
