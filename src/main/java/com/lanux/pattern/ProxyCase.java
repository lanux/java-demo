package com.lanux.pattern;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyCase {

    interface UserService {
        void addUser(String userId, String userName);

        void delUser(String userId);
    }

    class UserServiceImpl implements UserService {

        @Override
        public void addUser(String userId, String userName) {
            System.out.println("UserServiceImpl.addUser");
        }

        @Override
        public void delUser(String userId) {
            System.out.println("UserServiceImpl.delUser");
        }
    }

    class UserServiceImplProxy implements UserService {

        // 目标对象
        private UserService userService;

        // 通过构造方法传入目标对象
        public UserServiceImplProxy(UserService userService) {
            this.userService = userService;
        }

        @Override
        public void addUser(String userId, String userName) {
            System.out.println("start-->addUser()");
            userService.addUser(userId, userName);
            System.out.println("success-->addUser()");
        }

        @Override
        public void delUser(String userId) {
            userService.delUser(userId);
        }
    }

    class LogHandler implements InvocationHandler {

        // 目标对象
        private Object targetObject;

        /**
         * 根据传入的目标返回一个代理对象
         * @param targetObject
         * @return
         */
        public Object newProxyInstance(Object targetObject) {
            this.targetObject = targetObject;
            //该方法用于为指定类装载器、一组接口及调用处理器生成动态代理类实例
            //第一个参数指定产生代理对象的类加载器，需要将其指定为和目标对象同一个类加载器
            //第二个参数要实现和目标对象一样的接口，所以只需要拿到目标对象的实现接口
            //第三个参数表明这些被拦截的方法在被拦截时需要执行哪个InvocationHandler的invoke方法
            return Proxy.newProxyInstance(targetObject.getClass().getClassLoader(),
                    targetObject.getClass().getInterfaces(), this);
        }

        /**
         * InvocationHandler接口的方法，
         * @param proxy 表示代理，
         * @param method 表示原对象被调用的方法，
         * @param args 表示方法的参数
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("start-->>" + method.getName());
            Object ret = method.invoke(targetObject, args);
            System.out.println("success-->>" + method.getName());
            return ret;
        }

    }

    public static void main(String[] args) {
        ProxyCase pc = new ProxyCase();
//        UserService userService = pc.new UserServiceImplProxy(pc.new UserServiceImpl());
//        userService.addUser("001", "张三");

        LogHandler logHandler = pc.new LogHandler();
        UserService userService = (UserService) logHandler.newProxyInstance(pc.new UserServiceImpl());
        //UserManager userManager=new UserServiceImpl();
        userService.addUser("0001", "张三");
    }

}
