package com.lanux.juc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadInterruptCase {

    class ThreadCase extends Thread implements Runnable{

        @Override
        public void run() {
            super.run();
        }
    }

    static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            new ThreadFactory() {
                AtomicInteger counter = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "thread-" + counter.getAndIncrement());
                    return thread;
                }
            });

    public static void main(String[] args) {
        for (int i = 0; i < 100000; i++) {
            executor.submit(new Runnable() {
                private int count;

                @Override
                public void run() {
                    System.out.println(count + " " + Thread.currentThread().getName());
                    Thread.currentThread().stop();
                    long time = System.currentTimeMillis();
                    /*
                     * 使用while循环模拟 sleep 方法，这里不要使用sleep，否则在阻塞时会 抛
                     * InterruptedException异常
                     */
                    while ((System.currentTimeMillis() - time < 100)) {}
                }

                public Runnable setCount(int count) {
                    this.count = count;
                    return this;
                }
            }.setCount(i));
        }
    }
}
