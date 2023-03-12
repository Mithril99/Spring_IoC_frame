package com.spring.components.AOP;

import java.lang.reflect.Method;

public interface AfterReturningAdvice {
    void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}
