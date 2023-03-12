package com.test.advices;

import com.spring.Annotations.Component;
import com.spring.components.AOP.AfterReturningAdvice;

import java.lang.reflect.Method;

@Component("helloAdvice")
public class helloAfterReturningAdvice implements AfterReturningAdvice {
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        System.out.println("Hello, this is after advice, the Method before me is " + method.getName() + ", the Object run it is " + target.getClass().getSimpleName());
    }
}
