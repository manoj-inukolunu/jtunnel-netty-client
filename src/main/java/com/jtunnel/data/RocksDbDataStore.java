/*
package com.jtunnel.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtunnel.spring.HttpRequest;
import com.jtunnel.spring.HttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import lombok.extern.java.Log;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteOptions;

@Log
public class RocksDbDataStore implements DataStore {


  static {
    RocksDB.loadLibrary();
  }

  private RocksDB rocksDB;
  private static final ObjectMapper mapper = new ObjectMapper();
  private ColumnFamilyHandle requestColumnFamilyHandle;
  private ColumnFamilyHandle responseColumnFamilyHandle;

  public RocksDbDataStore() throws RocksDBException {
    final ColumnFamilyOptions cfOpts = new ColumnFamilyOptions().optimizeUniversalStyleCompaction();
    final List<ColumnFamilyDescriptor> cfDescriptors = Arrays.asList(
        new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts),
        new ColumnFamilyDescriptor("request".getBytes(), cfOpts),
        new ColumnFamilyDescriptor("response".getBytes(), cfOpts)
    );
    List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
    DBOptions dbOptions = new DBOptions().setCreateIfMissing(true).setCreateMissingColumnFamilies(true);
    rocksDB = RocksDB.open(dbOptions, "jtunnel", cfDescriptors, columnFamilyHandles);
    requestColumnFamilyHandle = columnFamilyHandles.get(1);
    responseColumnFamilyHandle = columnFamilyHandles.get(2);
  }


  @Override
  public void add(String requestId, FullHttpRequest request) throws JsonProcessingException, RocksDBException {
    log.info(requestId);
    rocksDB.put(requestColumnFamilyHandle, new WriteOptions(), requestId.getBytes(StandardCharsets.UTF_8),
        mapper.writeValueAsBytes(buildHttpRequest(request, requestId)));
  }

  @Override
  public void saveFullTrace(String requestId, FullHttpResponse response) throws Exception {
    rocksDB.put(responseColumnFamilyHandle, new WriteOptions(), requestId.getBytes(StandardCharsets.UTF_8),
        mapper.writeValueAsBytes(buildHttpResponse(response, requestId)));
  }

  @Override
  public HashMap<HttpRequest, HttpResponse> allRequests() throws Exception {
    HashMap<HttpRequest, HttpResponse> map = new HashMap<>();
    RocksIterator iterator = rocksDB.newIterator(requestColumnFamilyHandle, new ReadOptions());
    for (iterator.seekToLast(); iterator.isValid(); iterator.prev()) {
      HttpRequest req = mapper.readValue(iterator.value(), HttpRequest.class);
      byte[] resp = rocksDB.get(responseColumnFamilyHandle, new ReadOptions(), req.requestId.getBytes(
          StandardCharsets.UTF_8));
      if (resp != null) {
        HttpResponse res = mapper.readValue(resp, HttpResponse.class);
        map.put(req, res);
      }

    }
    return map;
  }

  @Override
  public HttpRequest get(String requestId) throws Exception {
    return mapper.readValue(
        rocksDB.get(requestColumnFamilyHandle, new ReadOptions(), requestId.getBytes(StandardCharsets.UTF_8)),
        HttpRequest.class);
  }

  @Override
  public HttpResponse getResponse(String requestId) throws Exception {
    return mapper.readValue(
        rocksDB.get(responseColumnFamilyHandle, new ReadOptions(), requestId.getBytes(StandardCharsets.UTF_8)),
        HttpResponse.class);
  }
}
*/
