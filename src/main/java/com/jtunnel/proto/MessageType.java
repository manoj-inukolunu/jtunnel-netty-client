package com.jtunnel.proto;

public enum MessageType {
  HTTP_REQUEST((byte) 1), HTTP_RESPONSE((byte) 2), PING((byte) 3), PONG((byte) 4), EMPTY((byte) 5), REGISTER((byte) 6),
  FIN((byte) 7), ACK((byte) 8);

  private byte type;

  MessageType(byte type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public static MessageType get(byte type) {
    for (MessageType value : values()) {
      if (value.type == type) {
        return value;
      }
    }

    throw new RuntimeException("unsupported type: " + type);
  }
}






