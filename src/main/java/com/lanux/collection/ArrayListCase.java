package com.lanux.collection;

import com.google.common.collect.Maps;

import java.util.HashMap;

public class ArrayListCase {
    private String name;

    class Inner {
        private String name;

        public void get(String[] args) {
            System.out.println("this.name = " + ArrayListCase.this.name);
        }
    }
    public static void main(String[] args) {
        HashMap map = new HashMap();
        for (int i = 0; i < 1_000_000; i++) {
            map.put(i, null);
        }
    }
}
