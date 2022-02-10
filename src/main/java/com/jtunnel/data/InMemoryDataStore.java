package com.jtunnel.data;

import com.jtunnel.spring.HttpRequest;
import com.jtunnel.spring.HttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.java.Log;

@Log
public class InMemoryDataStore implements DataStore {

  HashMap<HttpRequest, HttpResponse> map = new HashMap<>();
  HashMap<String, HttpRequest> requestIdMap = new HashMap<>();


  public InMemoryDataStore() {

  }


  @Override
  public void add(String requestId, FullHttpRequest request) {
    HttpRequest httpRequest = buildHttpRequest(request, requestId);
    map.put(httpRequest, null);
    requestIdMap.put(requestId, httpRequest);
  }

  @Override
  public void saveFullTrace(String requestId, FullHttpResponse response) {
    HttpRequest request = requestIdMap.get(requestId);
    map.put(request, buildHttpResponse(response, requestId));
  }

  @Override
  public HashMap<HttpRequest, HttpResponse> allRequestsFull() {
    return map;
  }

  @Override
  public Map<String, String> allRequests() throws Exception {
    return null;
  }

  @Override
  public HttpRequest get(String requestId) {
    return requestIdMap.get(requestId);
  }

  @Override
  public HttpResponse getResponse(String requestId) {
    return map.get(requestIdMap.get(requestId));
  }

}
