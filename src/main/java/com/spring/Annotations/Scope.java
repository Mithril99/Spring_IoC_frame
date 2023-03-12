package com.spring.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于注解各个类究竟要被加载为什么类型的Bean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {
    int SCOPE_PROTOTYPE = 1;
    int SCOPE_SINGLETON = 0;

    int value() default SCOPE_PROTOTYPE;
}
