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
        new Thread(() -> new NioServer()).start();
        Thread.sleep(2000);
        NioClient client = new NioClient();
        for (int i = 0; i < 100; i++) {
            client.write(RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(10,20)));
        }
        client.close();
    }
}
