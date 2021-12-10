package com.jtunnel.data;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TrieNode {

  private HashMap<String, TrieNode> map = new HashMap<>();
  boolean isTerminalNode;
  Set<String> documentIds = new HashSet<>();
  private String nodeKey;

  void insert(JsonNode jsonNode) {
    if (jsonNode.isValueNode()) {
      this.isTerminalNode = true;
      this.nodeKey = jsonNode.asText();
      return;
    }
    for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
      String fieldName = it.next();
      this.documentIds.add(fieldName);
      TrieNode trieNode = this.map.get(fieldName);
      if (trieNode == null) {
        this.map.put(fieldName, new TrieNode());
      }
      this.map.get(fieldName).insert(jsonNode.get(fieldName));
    }
  }
}






