package com.lanux.io.bio;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

/**
 * Created by lanux on 2017/9/16.
 */
public class TestBioClient {

    public static void main(String[] args) throws Exception {
        BioClient client = new BioClient().init();
        int count = 0;
//        for (int j = 0; j < 50; j++) {
//            client.write(count++ + "=" + RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(2000, 10000)));
//        }
//        for (int j = 0; j < 50; j++) {
//            client.read();
//        }
        for (int j = 0; j < 100; j++) {
            client.write(count++ + "=" + RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(8000, 10000)));
            client.read();
            System.out.println();
        }
//            client.close();
    }
}
