package com.lanux.juc;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class WaitNotifySemaphore {
    private final static int QUERY_LENGTH = 5;
    private final static int THREAD_COUNT = 20;
    private final static AtomicInteger COUNTER = new AtomicInteger(0);
    private final static Object LOCK = new Object();

    public static void main(String[] args) {
        Random random = new Random();
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(String.valueOf(i)) {
                @Override
                public void run() {
                    tryLock();
                    System.out.println("start " + this.getName());
                    try {
                        int i1 = random.nextInt() & 3000;
                        System.out.println("sleep = " + i1);
                        Thread.sleep(100 + i1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("unlock  " + this.getName());
                    unLock();
                }
            }.start();
        }
    }

    private static void unLock() {
        COUNTER.getAndDecrement();
        synchronized (LOCK) {
            LOCK.notifyAll();
        }
    }

    private static void tryLock() {
        int tryTimes = 0;
        int nowValue = COUNTER.get();
        while (true) {
            if (nowValue < QUERY_LENGTH && COUNTER.compareAndSet(nowValue, nowValue + 1)) {
                break;
            }
            if (tryTimes % 3 == 0) {
                waitForNotify();
            }
            nowValue = COUNTER.get();
            tryTimes++;
        }
    }

    private static void waitForNotify() {
        synchronized (LOCK) {
            try {
                LOCK.wait(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

