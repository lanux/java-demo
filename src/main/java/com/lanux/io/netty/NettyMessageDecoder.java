package com.lanux.io.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * https://github.com/linweiliang451791119/NIO/blob/master/nio/src/chapter13/NettyMarshallingEncoder.java
 * Created by lanux on 2017/9/16.
 */
public class NettyMessageDecoder extends ByteToMessageDecoder {

    public NettyMessageDecoder() {
        super();
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out)
            throws Exception {
        Object decoded = this.decode(channelHandlerContext, byteBuf);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in.readableBytes() < 5) {
            return null;
        }
        int begin = in.readerIndex();
        byte crcCode = in.readByte();
        if (0x02 != crcCode) {
            in.readerIndex(begin);
            return null;
        }
        int length = in.readInt();
        if (in.readableBytes() < length) { // STX - ETX
            in.readerIndex(begin);
            return null;
        }
        byte[] array = new byte[length];
        in.readBytes(array);
        NettyMessage nettyMessage = JsonUtil.fromJson(new String(array), NettyMessage.class);
        return nettyMessage;
    }
}