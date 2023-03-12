package com.test.service;

import com.spring.Annotations.Component;
import com.spring.Interfaces.BeanPostProcessor;


import java.lang.reflect.Method;

/**
 * 错误的代理方式，这样我们创建出来的新对象不具备那些依赖注入，初始化后的属性，只是单独创建了一个代理类
 */
@Deprecated
@Component("proxyPostProcessor")
public class ProxyPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
