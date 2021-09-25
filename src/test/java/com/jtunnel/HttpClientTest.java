/*
package com.jtunnel;

import org.junit.Test;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;


public class HttpClientTest {


  @Test
  public void runClient() throws Exception {
    String tunnelName = "manoj.jtunnel.net";
    int localServerPort = 8080;
    SocketChannel clientSocketChannel = SocketChannel.open(new InetSocketAddress(tunnelName, 1234));
    clientSocketChannel.configureBlocking(false);
    Selector selector = Selector.open();
    clientSocketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    while (true) {
      int selected = selector.select();
      if (selected == 0) {
        continue;
      }
      Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
      while (keys.hasNext()) {
        SelectionKey key = keys.next();
        if (key.isValid() && key.isReadable()) {
          Object attachment = key.attachment();
          if (attachment != null) {
            System.out.println("Received Data From Server");
            SocketChannel destChannel = SocketChannel.open(new InetSocketAddress(localServerPort));
            destChannel.configureBlocking(false);
            destChannel.register(selector, SelectionKey.OP_READ);
            destChannel.finishConnect();
            SocketChannel channel = (SocketChannel) key.channel();
            transfer(channel, destChannel);
          } else {
            System.out.println("Sending Data back to the server");
            SocketChannel channel = (SocketChannel) key.channel();
            transfer(channel, clientSocketChannel);
          }
        } else if (key.isValid() && key.isWritable() && key.attachment() == null) {
          SocketChannel channel = (SocketChannel) key.channel();
          System.out.println("Registering domain with server");
          channel.write(ByteBuffer.wrap(("register " + tunnelName).getBytes(StandardCharsets.UTF_8)));
          key.attach("Registered");
        }
        keys.remove();
      }
    }
  }


  @Test
  public void runClient1() throws Exception {
    SocketChannel clientSocketChannel = SocketChannel.open(new InetSocketAddress("test.jtunnel.net", 1234));
    clientSocketChannel.configureBlocking(false);
    Selector selector = Selector.open();
    clientSocketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    while (true) {
      int selected = selector.select();
      if (selected == 0) {
        continue;
      }
      Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
      while (keys.hasNext()) {
        SelectionKey key = keys.next();
        if (key.isValid() && key.isReadable()) {
          Object attachment = key.attachment();
          if (attachment != null) {
            System.out.println("Received Data From Server");
            SocketChannel destChannel = SocketChannel.open(new InetSocketAddress(8070));
            destChannel.configureBlocking(false);
            destChannel.register(selector, SelectionKey.OP_READ);
            destChannel.finishConnect();
            SocketChannel channel = (SocketChannel) key.channel();
            transfer(channel, destChannel);
          } else {
            System.out.println("Sending Data back to the server");
            SocketChannel channel = (SocketChannel) key.channel();
            transfer(channel, clientSocketChannel);
          }
        } else if (key.isValid() && key.isWritable() && key.attachment() == null) {
          SocketChannel channel = (SocketChannel) key.channel();
          System.out.println("Registering domain with server");
          channel.write(ByteBuffer.wrap("register test.jtunnel.net".getBytes(StandardCharsets.UTF_8)));
          key.attach("Registered");
        }
        keys.remove();
      }
    }
  }

  private void transfer(SocketChannel source, SocketChannel sink) throws Exception {
    ByteBuffer buffer = ByteBuffer.allocateDirect(10000);
    int count;
    while ((count = source.read(buffer)) > 0) {
      buffer.flip();
      while (buffer.hasRemaining()) {
        sink.write(buffer);
      }
      buffer.clear();
    }

    if (count < 0) {
      source.close();
    }
  }
}
*/
