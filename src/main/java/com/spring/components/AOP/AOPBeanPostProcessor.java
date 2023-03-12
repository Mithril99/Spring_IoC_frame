package com.spring.components.AOP;

import com.spring.Annotations.Autowired;
import com.spring.Annotations.Component;
import com.spring.Interfaces.BeanPostProcessor;
import com.spring.constant.Context;
import org.springframework.cglib.proxy.Enhancer;

/**
 * cglib在JDK17会无法使用，所以这里借用了Spring修正后的cglib
 * AOPBeanPostProcessor 为一个BeanPostProcessor，主要负责处理AOP相关问题
 * methodBeforeAdviceList AOP前置处理器列表，从容器中唯一获取
 * afterReturningAdviceList AOP后置处理器列表，从容器中唯一获取
 */
@Component(Context.AOP_PROCESSOR)
public class AOPBeanPostProcessor implements BeanPostProcessor {
    @Autowired
    private MethodBeforeAdviceList methodBeforeAdviceList;
    @Autowired
    private AfterReturningAdviceList afterReturningAdviceList;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        AOPProxy aopProxy = new AOPProxy();
        Object proxy = aopProxy.createProxy(bean, methodBeforeAdviceList, afterReturningAdviceList);

        return proxy;
    }
}
