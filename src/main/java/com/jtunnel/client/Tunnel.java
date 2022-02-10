package com.jtunnel.client;


import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Tunnel {

  private String name;
  private String subdomain;
  private Integer port;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Tunnel tunnel = (Tunnel) o;
    return Objects.equals(subdomain, tunnel.subdomain);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subdomain);
  }
}
