package com.jtunnel.data;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.jtunnel.spring.HttpRequest;
import com.jtunnel.spring.HttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import java.util.HashMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MapDbDataStore implements DataStore {


  private final DB mapDb;
  private final ConcurrentNavigableMap<String, String> requests;
  private final ConcurrentNavigableMap<String, String> responses;
  private final NavigableSet<String> searchTerms;
  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }


  public MapDbDataStore(String directory) {
    mapDb = DBMaker.fileDB(directory + "/jtunnel.db").closeOnJvmShutdown().make();
    requests = mapDb.treeMap("requests", Serializer.STRING, Serializer.STRING).createOrOpen();
    responses = mapDb.treeMap("responses", Serializer.STRING, Serializer.STRING).createOrOpen();
    searchTerms = mapDb.treeSet("searchTerms", Serializer.STRING).createOrOpen();
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
    Stopwatch stopwatch = Stopwatch.createStarted();
    HashMap<HttpRequest, HttpResponse> map = new HashMap<>();
    for (String requestId : requests.keySet()) {
      map.put(get(requestId), getResponse(requestId));
    }
    log.info("Took {} milliseconds to get all requests from mapdp", stopwatch.elapsed(TimeUnit.MILLISECONDS));
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

  public void addSearchTerm(String term) {
    searchTerms.add(term);
  }

  public Set<String> getSearchTerms() {
    return searchTerms;
  }

  @Override
  public void remove(String requestId) {
    requests.remove(requestId);
    responses.remove(requestId);
    mapDb.commit();
  }

  /*public static void main(String[] args) throws Exception {
    MapDbDataStore dataStore = new MapDbDataStore("/Users/manoj");
    HashMap<HttpRequest, HttpResponse> all = dataStore.allRequests();
    DB mapDb = DBMaker.fileDB("/Users/manoj/test/" + "/jtunnel.db").closeOnJvmShutdown().make();
    ConcurrentNavigableMap<String, String> requests =
        mapDb.treeMap("requests", Serializer.STRING, Serializer.STRING).createOrOpen();
    ConcurrentNavigableMap<String, String> responses =
        mapDb.treeMap("responses", Serializer.STRING, Serializer.STRING).createOrOpen();
    *//*for (HttpRequest request : all.keySet()) {
      requests.put()
    }*//*
  }*/
}
