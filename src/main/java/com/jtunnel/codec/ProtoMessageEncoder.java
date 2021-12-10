package com.jtunnel.codec;


import com.google.common.base.Strings;
import com.jtunnel.proto.MessageType;
import com.jtunnel.proto.ProtoMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.nio.charset.Charset;
import java.util.UUID;

public class ProtoMessageEncoder extends MessageToByteEncoder<ProtoMessage> {

  @Override
  protected void encode(ChannelHandlerContext ctx, ProtoMessage message, ByteBuf out) {
    //  This will determine whether the message type is EMPTY type , If it is EMPTY type , Indicates that the
    //  current message does not need to be written to the pipeline
    if (message.getMessageType() != MessageType.EMPTY) {
    /*  out.writeInt(1);//  Write the current magic number
      out.writeByte(1);//  Write the current major version number
      out.writeByte(1);//  Write the current minor version number
      out.writeByte(1);//  Write the current revision number*/
      if (Strings.isNullOrEmpty(message.getSessionId())) {
        //  Generate a sessionId, And write it to the byte sequence
        String sessionId = UUID.randomUUID().toString();
        message.setSessionId(sessionId);
        out.writeCharSequence(sessionId, Charset.defaultCharset());
      } else {
        out.writeCharSequence(message.getSessionId(), Charset.defaultCharset());
      }

      out.writeByte(message.getMessageType().getType());//  Write the type of the current message
      out.writeShort(message.getAttachments().size());//  Number of additional parameters written to the
      // current message
      message.getAttachments().forEach((key, value) -> {
        Charset charset = Charset.defaultCharset();
        out.writeInt(key.length());//  The length of the write key
        out.writeCharSequence(key, charset);//  Write key data
        out.writeInt(value.length());//  The length of the hill value
        out.writeCharSequence(value, charset);//  Write value data
      });

      if (null == message.getBody()) {
        out.writeInt(0);//  If the message body is empty , Then write 0, Indicates that the message body
        // length is 0
      } else {
        out.writeInt(message.getBody().length());
        out.writeCharSequence(message.getBody(), Charset.defaultCharset());
      }
    }
  }
}





