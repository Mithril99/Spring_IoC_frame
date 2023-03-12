package com.spring.components.AOP;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;


import java.lang.reflect.Method;

/**
 * AOP代理核心类
 * - target: 被代理的类对象，我们的代理对象通过该对象来进行方法的调用
 * - beforeAdviceList: 前置处理器列表
 * - afterAdviceList: 后置处理器列表
 * createProxy() 创建代理对象
 * intercept() 激活方法
 */
public class AOPProxy implements MethodInterceptor {
    private Object target;

    private MethodBeforeAdviceList beforeAdviceList;

    private AfterReturningAdviceList afterAdviceList;

    public Object createProxy(Object target, MethodBeforeAdviceList methodBeforeAdviceList,
                              AfterReturningAdviceList afterReturningAdviceList){
        this.target = target;
        this.beforeAdviceList = methodBeforeAdviceList;
        this.afterAdviceList = afterReturningAdviceList;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(this.target.getClass());
        System.out.println("Generating Proxy Object for " + target.getClass().getSimpleName());
        enhancer.setCallback(this);
        enhancer.setClassLoader(this.target.getClass().getClassLoader());

        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("this is an AOP proxy object for " + target.toString());

        // AOP方法代理前置处理器
        for(MethodBeforeAdvice advice:beforeAdviceList){
            advice.before(method, objects, target);
        }
        Object result = method.invoke(target, objects);

        // AOP方法代理后置处理器
        for(AfterReturningAdvice advice:afterAdviceList){
            advice.afterReturning(result, method, objects, target);
        }

        return result;
    }
}
