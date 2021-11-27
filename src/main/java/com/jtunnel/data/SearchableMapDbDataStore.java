package com.jtunnel.data;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.jtunnel.spring.HttpRequest;
import com.jtunnel.spring.HttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SearchableMapDbDataStore extends MapDbDataStore implements SearchableDataStore {

  private final Map<String, Set<String>> invertedIndex = new HashMap<>();

  public SearchableMapDbDataStore(String directory) {
    super(directory);
  }

  @Override
  public void saveFullTrace(String requestId, FullHttpResponse response) throws Exception {
    super.saveFullTrace(requestId, response);
    index(get(requestId), getResponse(requestId));
  }

  @Override
  public Set<String> search(List<String> text) {
    return text.stream().filter(invertedIndex::containsKey).map(invertedIndex::get).flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public void index(HttpRequest request, HttpResponse response) {
    String[] parts = request.getInitialLine().split("/");
    for (int i = 0; i < parts.length; i++) {
      StringBuffer buffer = new StringBuffer("/");
      for (int j = i; j < parts.length; j++) {
        buffer.append(parts[j]).append("/");
        Set<String> documentIds = invertedIndex.getOrDefault(buffer.toString(), new HashSet<>());
        documentIds.add(request.getRequestId());
        invertedIndex.put(buffer.toString(), documentIds);
      }
    }
  }
}






