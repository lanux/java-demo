package com.lanux.io.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;
import java.util.Map;

/**
 * https://github.com/linweiliang451791119/NIO/blob/master/nio/src/chapter13/NettyMarshallingEncoder.java
 * Created by lanux on 2017/9/16.
 */
public class NettyMessageEncoder extends MessageToMessageEncoder<NettyMessage> {


    public NettyMessageEncoder() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, NettyMessage msg,
            List<Object> out) throws Exception {
        if (msg == null) {
            throw new Exception("The encode message is null");
        }

        ByteBuf sendBuf = Unpooled.buffer();
        sendBuf.writeByte(0x02);
        sendBuf.writeInt(0);
        sendBuf.writeBytes(JsonUtil.toJson(msg).getBytes());
        // 在第4个字节出写入Buffer的长度
        int readableBytes = sendBuf.readableBytes();
        sendBuf.setInt(1, readableBytes-5);

        // 把Message添加到List传递到下一个Handler
        out.add(sendBuf);
    }

}