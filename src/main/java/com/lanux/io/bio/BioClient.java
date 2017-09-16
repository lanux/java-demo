package com.lanux.io.bio;

import com.lanux.io.NetConfig;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by lanux on 2017/8/6.
 */
public class BioClient extends IoStream implements Closeable {

    private Socket socket;

    public BioClient init() throws Exception {
        socket = new Socket(NetConfig.server_ip, NetConfig.server_port);
        socket.setKeepAlive(true);
        socket.setSoTimeout(NetConfig.so_timeout);
        return this;
    }

    @Override
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public void write(String value) throws IOException {
        byte[] bytes = value.getBytes();
        System.out.println("client write " + bytes.length + " : " + maxString(value,50));
        super.writeStream(socket.getOutputStream(), bytes);
    }

    public String read() throws IOException {
        byte[] bytes = super.readStream(socket.getInputStream());
        String s = new String(bytes);
        System.out.println("client read " + bytes.length + " : " + maxString(s,50));
        return s;
    }
}
