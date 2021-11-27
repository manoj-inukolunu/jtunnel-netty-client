package com.jtunnel.data;

import com.jtunnel.data.index.SearchIndex;
import com.jtunnel.spring.HttpRequest;
import com.jtunnel.spring.HttpResponse;

public interface SearchableDataStore extends DataStore, SearchIndex {

  void index(HttpRequest request, HttpResponse response);
}
