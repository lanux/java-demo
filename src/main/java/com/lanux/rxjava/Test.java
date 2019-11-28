package com.lanux.rxjava;

import com.google.common.collect.Lists;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;

public class Test {
    static class User{
        private String name;
    }

    public static void main(String[] args) throws InterruptedException {
        Observable.just(new ArrayList<User>(),new ArrayList<User>())
                .flatMap(userList->Observable.fromIterable(userList))
                .map(user->user.name)
                .subscribe(name-> System.out.println(name));
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> observableEmitter) throws Exception {
                String s = "hello";
                System.out.println("发送线程 " + Thread.currentThread().getName());
                Thread.sleep(100);
                observableEmitter.onNext(s);
                observableEmitter.onComplete();
            }
        })
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
            System.out.println("消费线程 " + Thread.currentThread().getName());
        });
        Thread.sleep(200);
        System.out.println("888 " + Thread.currentThread().getName());
    }
}
