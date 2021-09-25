package com.jtunnel.netty;

import io.netty.handler.codec.http.FullHttpRequest;
import java.util.LinkedList;
import java.util.Queue;

public class QueueConsumer implements Runnable {

  private Queue<FullHttpRequest> queue;

  public QueueConsumer(LinkedList<FullHttpRequest> queue) {
    this.queue = queue;
  }

  @Override
  public void run() {
    while (true) {
      if (!queue.isEmpty()) {
        FullHttpRequest request = queue.poll();
      }
    }
  }
}
