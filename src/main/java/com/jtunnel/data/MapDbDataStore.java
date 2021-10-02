package com.jtunnel.data;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtunnel.http.HttpRequest;
import com.jtunnel.http.HttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import java.util.HashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

@Log
public class MapDbDataStore implements DataStore {


  private final DB mapDb;
  private final ConcurrentNavigableMap<String, String> requests;
  private final ConcurrentNavigableMap<String, String> responses;
  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.setSerializationInclusion(Include.NON_NULL);
  }


  public MapDbDataStore(String arg) {
    mapDb = DBMaker.fileDB(arg + "/jtunnel.db").closeOnJvmShutdown().make();
    requests = mapDb.treeMap("requests", Serializer.STRING, Serializer.STRING).createOrOpen();
    responses = mapDb.treeMap("responses", Serializer.STRING, Serializer.STRING).createOrOpen();
  }

  @Override
  public void add(String requestId, FullHttpRequest request) throws Exception {
    HttpRequest httpRequest = buildHttpRequest(request, requestId);
    requests.put(requestId, mapper.writeValueAsString(httpRequest));
    mapDb.commit();
  }

  @Override
  public void saveFullTrace(String requestId, FullHttpResponse response) throws Exception {
    responses.put(requestId, mapper.writeValueAsString(buildHttpResponse(response, requestId)));
    mapDb.commit();
  }

  @Override
  public HashMap<HttpRequest, HttpResponse> allRequests() throws Exception {
    HashMap<HttpRequest, HttpResponse> map = new HashMap<>();
    for (String requestId : requests.keySet()) {
//      log.info("Getting for requestId=" + requestId);
      map.put(get(requestId), getResponse(requestId));
    }
    return map;
  }

  @Override
  public HttpRequest get(String requestId) throws Exception {
    return mapper.readValue(requests.get(requestId), HttpRequest.class);
  }

  @Override
  public HttpResponse getResponse(String requestId) throws Exception {
    if (responses.containsKey(requestId)) {
      return mapper.readValue(responses.get(requestId), HttpResponse.class);
    }
    return new HttpResponse();
  }
}
