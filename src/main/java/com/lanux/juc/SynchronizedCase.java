package com.lanux.juc;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * static的方法属于类方法，它属于这个Class（注意：这里的Class不是指Class的某个具体对象），
 * 那么static获取到的锁，是属于类的锁。而非static方法获取到的锁，是属于当前对象的锁。
 * 所以，他们之间不会产生互斥。
 * 网上说 "synchronized修饰静态方法作用的象是这个类的所有对象" 这句话是错误的
 */
public class SynchronizedCase {
    static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2,
            new ThreadFactory() {
                AtomicInteger counter = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r,"thread-" + counter.getAndIncrement());
                }
            });

    public static void main(String[] args) throws Exception {
        test();
        executor.shutdown();
    }

    private static void test() throws InterruptedException {
        final SynchronizedCase demo = new SynchronizedCase();
        List<Callable<Void>> list = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            list.add(() -> {
                staticFunction();
                return null;
            });
        }
        for (int i = 0; i < 5; i++) {
            list.add(() -> {
                demo.function();
                return null;
            });
        }
        executor.invokeAll(list);
    }

    public static synchronized void staticFunction() {
        for (int i = 0; i < 3; i++) {
            sleep();
            System.out.println(Thread.currentThread().getName() + ": Static function running");
        }
    }

    public synchronized void function() {
        for (int i = 0; i < 3; i++) {
            sleep();
            System.out.println(Thread.currentThread().getName() + ": not static function running");
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
