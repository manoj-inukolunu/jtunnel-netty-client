package com.jtunnel.data;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtunnel.spring.HttpRequest;
import com.jtunnel.spring.HttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SearchableMapDbDataStore extends MapDbDataStore implements SearchableDataStore {

  private final Map<String, Set<String>> initialLineIndex = new HashMap<>();
  private static final ObjectMapper mapper = new ObjectMapper();
  private final TrieNode trieNode = new TrieNode();


  public SearchableMapDbDataStore(String directory) {
    super(directory);
  }

  @Override
  public void saveFullTrace(String requestId, FullHttpResponse response) throws Exception {
    super.saveFullTrace(requestId, response);
  }

  @Override
  public Set<String> search(List<String> text) {
    try {
      return text.stream().filter(initialLineIndex::containsKey).map(initialLineIndex::get).flatMap(Set::stream)
          .collect(Collectors.toSet());
    } catch (RuntimeException e) {
      log.warn("Failed to search", e);
      return new HashSet<>();
    }
  }

  @Override
  public void index(HttpRequest request, HttpResponse response) {
    String[] parts = request.getInitialLine().split("/");
    for (int i = 0; i < parts.length; i++) {
      StringBuilder builder = new StringBuilder("/");
      for (int j = i; j < parts.length; j++) {
        builder.append(parts[j]).append("/");
        Set<String> documentIds = initialLineIndex.getOrDefault(builder.toString(), new HashSet<>());
        documentIds.add(request.getRequestId());
        initialLineIndex.put(builder.toString(), documentIds);
      }
    }
  }

  @Override
  public void indexJsonContent(String content) {
    try {
      JsonNode node = mapper.readValue(content, JsonNode.class);
      trieNode.insert(node);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}






