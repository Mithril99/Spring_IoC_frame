package com.test.service;

import com.spring.Annotations.Component;
import com.spring.Interfaces.BeanPostProcessor;

@Component("myPostProcessor")
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("this is post processor! run before the Bean Initialization " + bean.getClass().getSimpleName());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("this is post processor! run after the Bean Initialization " + bean.getClass().getSimpleName());
        return bean;
    }
}
