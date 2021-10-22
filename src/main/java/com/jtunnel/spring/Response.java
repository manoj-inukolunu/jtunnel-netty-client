package com.jtunnel.spring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import lombok.Data;

@Data
public class Response {

  @JsonProperty("recordsTotal")
  private int recordsTotal;
  @JsonProperty("recordsFiltered")
  private int recordsFiltered;
  @JsonProperty("total")
  public int total;
  @JsonProperty("countPerPage")
  public int countPerPage;
  @JsonProperty("data")
  public List<ObjectNode> list;

}
