package com.lanux.io.bio;

import com.lanux.io.nio.NioClient;
import com.lanux.io.nio.NioServer;

/**
 * Created by lanux on 2017/9/16.
 */
public class TestNio {
    public static void main(String[] args) throws Exception {
        new Thread(() -> new NioServer()).start();
        Thread.sleep(2000);
        new NioClient();
    }
}
