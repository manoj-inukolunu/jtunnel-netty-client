package com.jtunnel.codec;

import com.jtunnel.proto.MessageType;
import com.jtunnel.proto.ProtoMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

public class ProtoMessageDecoder extends ByteToMessageDecoder {

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
    ProtoMessage message = new ProtoMessage();
    /*message.setMagicNumber(byteBuf.readInt());  //  Read magic number
    message.setMainVersion(byteBuf.readByte()); //  Read the main version number
    message.setSubVersion(byteBuf.readByte()); //  Read the minor version number
    message.setModifyVersion(byteBuf.readByte());//  Read the revision number*/
    CharSequence sessionId =
        byteBuf.readCharSequence(UUID.randomUUID().toString().length(), Charset.defaultCharset());//  Read sessionId
    message.setSessionId((String) sessionId);

    message.setMessageType(MessageType.get(byteBuf.readByte()));
    short attachmentSize = byteBuf.readShort();
    for (short i = 0; i < attachmentSize; i++) {
      int keyLength = byteBuf.readInt();
      CharSequence key = byteBuf.readCharSequence(keyLength, Charset.defaultCharset());
      int valueLength = byteBuf.readInt();
      CharSequence value = byteBuf.readCharSequence(valueLength, Charset.defaultCharset());
      message.addAttachment(key.toString(), value.toString());
    }

    int bodyLength = byteBuf.readInt();
    CharSequence body = byteBuf.readCharSequence(bodyLength, Charset.defaultCharset());
    message.setBody(body.toString());
    out.add(message);
  }
}






