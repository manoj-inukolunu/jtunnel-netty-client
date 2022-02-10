package com.jtunnel.spring;

import lombok.Data;

@Data
public class RegisterRequest {

  private String subdomain;
  private String tunnelName;
  private Integer port;
}
