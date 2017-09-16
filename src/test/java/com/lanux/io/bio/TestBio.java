package com.lanux.io.bio;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.io.ObjectInputStream;

/**
 * Created by lanux on 2017/9/16.
 */
public class TestBio {

    public static void main(String[] args) throws Exception {
        new Thread(() -> new BioServer()).start();
        Thread.sleep(2000);
        try {
            BioClient client = new BioClient().init();
            for (int j = 0; j < 5; j++) {
                client.write(RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(2000, 10000)));
            }
            for (int j = 0; j < 5; j++) {
                client.read();
            }

            System.out.println();
            Thread.sleep(2000);

            for (int j = 0; j < 5; j++) {
                client.write(RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(2000, 10000)));
                client.read();
                System.out.println();
            }
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
