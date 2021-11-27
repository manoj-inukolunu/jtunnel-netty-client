package com.jtunnel.spring;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.util.internal.StringUtil;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.Data;

@Data
public class HttpRequest {


  public String initialLine;
  public List<Entry<String, String>> httpHeaders;
  public List<Entry<String, String>> trailers;
  public String content;
  public String requestId;
  public String version;
  public String uri;
  public String method;
  public Date requestTime;

  public String getLine() {
    int idx = initialLine.indexOf("?");
    if (idx != -1) {
      return initialLine.substring(0, initialLine.indexOf("?"));
    }
    return initialLine;
  }

  @JsonIgnore
  public DefaultHttpHeaders getHeaders() {
    DefaultHttpHeaders headers = new DefaultHttpHeaders();
    for (Entry<String, String> entry : httpHeaders) {
      headers.add(entry.getKey(), entry.getValue());
    }
    return headers;
  }

  @JsonIgnore
  public DefaultHttpHeaders getTrailer() {
    DefaultHttpHeaders headers = new DefaultHttpHeaders();
    for (Entry<String, String> entry : trailers) {
      headers.add(entry.getKey(), entry.getValue());
    }
    return headers;
  }


  public String getUri() {
    if (initialLine != null) {
      uri = initialLine.split(" ")[1];
      return uri;
    }
    return uri;
  }

  public String getMethod() {
    if (initialLine != null) {
      method = initialLine.split(" ")[0];
      return method;
    }
    return method;
  }

  public String getVersion() {
    if (initialLine != null) {
      version = initialLine.split(" ")[2];
      return version;
    }
    return version;
  }

  private static void appendHeaders(StringBuilder builder, List<Entry<String, String>> headers) {
    for (Entry<String, String> header : headers) {
      builder.append(header.getKey());
      builder.append(": ");
      builder.append(header.getValue());
      builder.append(StringUtil.NEWLINE);
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(initialLine).append("\r\n");
    appendHeaders(builder, httpHeaders);
    appendHeaders(builder, trailers);
    builder.append(StringUtil.NEWLINE);
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
    HttpRequest that = (HttpRequest) o;
    return Objects.equals(httpHeaders,
        that.httpHeaders) && Objects.equals(trailers, that.trailers) && Objects.equals(content,
        that.content) && Objects.equals(version,
        that.version) && Objects.equals(uri, that.uri) && Objects.equals(method, that.method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(httpHeaders, trailers, content, version, uri, method);
  }
}
