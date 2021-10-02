package com.jtunnel.http;


import io.netty.util.internal.StringUtil;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.Data;

@Data
public class HttpResponse {

  public String initialLine;
  public List<Entry<String, String>> httpHeaders;
  public List<Entry<String, String>> trailers;
  public String content;
  public String requestId;


  private static void appendHeaders(StringBuilder builder, List<Entry<String, String>> headers) {
    if (headers != null) {
      for (Entry<String, String> header : headers) {
        builder.append(header.getKey());
        builder.append(": ");
        builder.append(header.getValue());
        builder.append(StringUtil.NEWLINE);
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(initialLine).append("\r\n");
    appendHeaders(builder, httpHeaders);
    appendHeaders(builder, trailers);
    builder.append(content);
    return builder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HttpResponse that = (HttpResponse) o;
    return Objects.equals(requestId, that.requestId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId);
  }
}
