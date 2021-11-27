package com.jtunnel.index;


import java.util.List;

public interface SearchIndex {

  List<String> search(List<String> text);

  void add(String request, String response);
}






