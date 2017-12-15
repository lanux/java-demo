package com.lanux.rxjava;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> observableEmitter) throws Exception {
                String s = "hello";
                System.out.println("Observable " + Thread.currentThread().getName());
                Thread.sleep(100);
                observableEmitter.onNext(s);
                observableEmitter.onComplete();
            }
        })
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.newThread())
                .subscribe(s -> {
            System.out.println("subscribe " + Thread.currentThread().getName());
        });
        Thread.sleep(200);
        System.out.println("888 " + Thread.currentThread().getName());
    }
}
