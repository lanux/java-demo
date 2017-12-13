package com.lanux.pattern;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyDemo {

    public interface BasicService {
        String getServiceName();
    }

    public interface MyService extends BasicService {
        void sayHello(String name);

        void sayGoodBye(String name);
    }

    public class MyServiceProxy implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args != null)
                System.out.println(method.getName() + " " + args[0]);
            return this.getClass().getName();
        }

        public MyService newProxyInstance() {
            return (MyService) Proxy.newProxyInstance(ProxyDemo.this.getClass().getClassLoader(),
                    new Class[] { MyService.class },
                    this);
        }
    }

    public static void main(String[] args) {
        ProxyDemo demo = new ProxyDemo();
        MyService proxy = demo.new MyServiceProxy().newProxyInstance();
        String serviceName = proxy.getServiceName();
        proxy.sayHello(serviceName);
        proxy.sayGoodBye(serviceName);
    }
}
