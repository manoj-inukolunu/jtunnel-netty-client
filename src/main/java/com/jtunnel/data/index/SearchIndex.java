package com.jtunnel.data.index;


import com.jtunnel.spring.HttpRequest;
import com.jtunnel.spring.HttpResponse;
import java.util.List;
import java.util.Set;

public interface SearchIndex {

  Set<String> search(List<String> text);

  void index(HttpRequest request, HttpResponse response);

  void indexJsonContent(String content);
}







