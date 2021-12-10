package com.jtunnel.data;

import com.jtunnel.data.index.SearchIndex;
import com.jtunnel.spring.HttpRequest;
import com.jtunnel.spring.HttpResponse;
import java.util.Set;

public interface SearchableDataStore extends DataStore, SearchIndex {

  void index(HttpRequest request, HttpResponse response);

  void addSearchTerm(String term);

  Set<String> getSearchTerms();
}
