package com.spring.components.AOP;


import java.lang.reflect.Method;

public interface MethodBeforeAdvice {
    void before(Method method, Object[] args, Object target) throws Throwable;
}
