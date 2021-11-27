package com.jtunnel.index;

import com.jtunnel.spring.HttpRequest;
import com.jtunnel.spring.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InMemoryIndex implements SearchIndex {

  Map<String, List<String>> invertedIndex = new HashMap<>();


  @Override
  public List<String> search(List<String> text) {
    return text.stream().filter(term -> invertedIndex.containsKey(term)).map(term -> invertedIndex.get(term))
        .flatMap(List::stream).collect(
            Collectors.toList());
  }

  @Override
  public void add(String request, String response) {

  }
}






