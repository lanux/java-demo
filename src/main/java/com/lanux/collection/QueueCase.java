package com.lanux.collection;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueCase {
    static ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();

    static LinkedBlockingQueue<String> queue2 = new LinkedBlockingQueue<>();

    static volatile int ctt;

    static ExecutorService executorService1 = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3, new ThreadFactory() {
                final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "s-" + threadNumber.getAndIncrement());
                }
            });
    static ExecutorService executorService2 = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3, new ThreadFactory() {
                final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "p-" + threadNumber.getAndIncrement());
                }
            });

    public static void main(String[] args) throws Exception {
        long startTime = System.nanoTime();
        for (int i = 0; i < 5; i++) {
            executorService2.submit((Runnable) () -> {
                for (int count = 0; count < 100000; count++) {
                    String x = count + " = " + Thread.currentThread().getName();
                    queue.offer(x);
                }
            });
        }
        for (int i = 0; i < 5; i++) {
            executorService1.submit(() -> {
                for (int count = 0; count < 100000; count++) {
                    String take = queue.poll();
                    System.out.println(Thread.currentThread().getName() + " consumer : " + take);
                }
                ctt++;
            });
        }
        while (true) {
            if (ctt==5) {
                long estimatedTime = System.nanoTime() - startTime;
                System.out.println("estimatedTime = " + estimatedTime);
                executorService1.shutdown();
                executorService2.shutdown();
                break;
            }
        }
    }
}
