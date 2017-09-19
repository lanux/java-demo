package com.lanux.io.bio;

import com.lanux.io.nio.NioClient;
import com.lanux.io.nio.NioServer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

/**
 * Created by lanux on 2017/9/16.
 */
public class TestNio {
    public static void main(String[] args) throws Exception {
        new Thread(() -> new NioServer(),"nio server").start();
        Thread.sleep(2000);
        NioClient client = new NioClient();
        new Thread(() -> client.listen(),"nio client").start();
        while (!client.channel.isConnected()){
            System.out.println("wait");
            Thread.sleep(100);
        }
        for (int i = 0; i < 100; i++) {
            client.write(i + "=" + RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(1000, 20000)));
        }
        client.close();
    }
}
