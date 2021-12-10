package com.jtunnel.spring;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jayway.jsonpath.JsonPath;
import com.jtunnel.data.SearchableDataStore;
import com.jtunnel.netty.DestHttpResponseHandler;
import com.jtunnel.netty.TunnelConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.java.Log;
import net.minidev.json.JSONArray;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log
public class RequestRestController {

  @Autowired
  private SearchableDataStore dataStore;
  @Autowired
  private ObjectMapper mapper;
  @Autowired
  TunnelConfig tunnelConfig;

  LoadingCache<String, List<HttpRequest>> jsonPathCache = CacheBuilder.newBuilder().maximumSize(100).build(
      new CacheLoader<String, List<HttpRequest>>() {
        @Override
        public List<HttpRequest> load(String jsonPath) throws Exception {
          HashMap<HttpRequest, HttpResponse> map = dataStore.allRequests();
          List<HttpRequest> list = new ArrayList<>();
          for (HttpRequest request : map.keySet()) {
            try {
              if (request.getContent() != null) {
                JSONArray array = JsonPath.read(request.getContent(), jsonPath);
                if (array != null && array.size() != 0) {
                  list.add(request);
                }
              }
            } catch (Exception e) {
            }
          }
          return list;
        }
      });

  @GetMapping("/rest/test")
  public String test() {
    return "test";
  }

  private List<HttpRequest> search(String term) throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    Set<String> requestIds = dataStore.search(Arrays.asList(term));
    Set<HttpRequest> set = new HashSet<>();
    for (String requestId : requestIds) {
      set.add(dataStore.get(requestId));
    }
    set.addAll(jsonPathCache.get(term));
    log.info("Time Taken to search=" + stopwatch.elapsed(TimeUnit.SECONDS));
    stopwatch.stop();
    return new ArrayList<>(set);
  }


  @GetMapping("/rest/data/history")
  public Response search(@RequestParam("start") int start, @RequestParam("length") int end,
      @RequestParam("search[value]") String term) throws Exception {
    if (!Strings.isNullOrEmpty(term)) {
      end += start;
      return getResponseFromList(start, end, search(term));
    } else {
      end += start;
      HashMap<HttpRequest, HttpResponse> data = dataStore.allRequests();
      List<HttpRequest> list = new ArrayList<>(data.keySet());
      return getResponseFromList(start, end, list);
    }
  }

  @GetMapping("/rest/data/search_terms")
  public List<String> searchTerms(@RequestParam("start") int start, @RequestParam("length") int end,
      @RequestParam("search[value]") String term) throws Exception {
    return new ArrayList<>(dataStore.getSearchTerms());
  }

  @NotNull
  private Response getResponseFromList(int start, int end, List<HttpRequest> list) {
    list.sort((o1, o2) -> -Long.compare(Long.parseLong(o1.requestId), Long.parseLong(o2.requestId)));

    List<ObjectNode> objectNodes = new ArrayList<>();
    for (HttpRequest request : list) {
      ObjectNode object = mapper.createObjectNode();
      object.put("requestId", request.getRequestId());
      object.put("requestTime", String.valueOf(new Date(Long.parseLong(request.getRequestId()))));
      object.put("line", request.getLine());
      objectNodes.add(object);
    }

    Response response = new Response();
    response.setRecordsTotal(list.size());
    response.setRecordsFiltered(list.size());
    response.setTotal(list.size());
    response.setCountPerPage(end - start);
    response.setList(objectNodes.subList(start, end >= objectNodes.size() ? objectNodes.size() - 1 : end));
    return response;
  }


  @GetMapping("/rest/history")
  public Response getHistory(@RequestParam("start") int start, @RequestParam("end") int end) throws Exception {
    HashMap<HttpRequest, HttpResponse> data = dataStore.allRequests();
    List<HttpRequest> list = new ArrayList<>(data.keySet());
    list.sort((o1, o2) -> -Long.compare(Long.parseLong(o1.requestId), Long.parseLong(o2.requestId)));
    return getResponseFromList(start, end, list);
  }

  @GetMapping("/rest/request/{requestId}")
  public String getRequest(@PathVariable("requestId") String requestId) throws Exception {
    HttpRequest request = dataStore.get(requestId);
    try {
      request.setContent(
          mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(request.getContent(),
              JsonNode.class)));
    } catch (Exception ignored) {

    }
    return request.toString();
  }

  @GetMapping("/rest/response/{requestId}")
  public String getResponse(@PathVariable("requestId") String requestId) throws Exception {
    HttpResponse response = dataStore.getResponse(requestId);
    try {
      response.setContent(
          mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(response.getContent(),
              JsonNode.class)));
    } catch (Exception ignored) {

    }
    return response.toString();
  }

  @GetMapping("/rest/replay/{requestId}")
  public void replayRequest(@PathVariable("requestId") String requestId) throws Exception {
    log.info("Replaying request= " + requestId);
    HttpRequest request = dataStore.get(requestId);
    FullHttpRequest fullRequest =
        new DefaultFullHttpRequest(HttpVersion.valueOf(request.getVersion()),
            HttpMethod.valueOf(request.getMethod()), request.getUri(),
            Unpooled.copiedBuffer(request.getContent().getBytes(StandardCharsets.UTF_8)), request.getHeaders(),
            request.getTrailer());
    localHttpRequest(fullRequest);
  }

  @GetMapping("/rest/delete/{requestId}")
  public void deleteRequest(@PathVariable("requestId") String requestId) throws Exception {
    log.info("Deleting request= " + requestId);
    dataStore.remove(requestId);
  }

  private void localHttpRequest(FullHttpRequest fullHttpRequest) throws Exception {
    String requestId = System.currentTimeMillis() + "";
    dataStore.add(requestId, fullHttpRequest);
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap b = new Bootstrap();
    b.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress(tunnelConfig.getDestHost(),
        tunnelConfig.getDestPort())).handler(
        new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline p = socketChannel.pipeline();
            p.addLast("log", new LoggingHandler(LogLevel.DEBUG));
            p.addLast("codec", new HttpClientCodec());
            p.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
            p.addLast("handler", new DestHttpResponseHandler(null, requestId, dataStore));
          }
        });
    Channel channel = b.connect().sync().channel();
    ChannelFuture f = channel.writeAndFlush(fullHttpRequest);
    f.addListener(future -> {
      if (!future.isSuccess()) {
        future.cause().printStackTrace();
      }
    });
  }
}
