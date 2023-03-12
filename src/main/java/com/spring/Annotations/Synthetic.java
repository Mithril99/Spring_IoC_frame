package com.spring.Annotations;

/**
 * 注解是否进行AOP代理处理
 *
 */
public @interface Synthetic {
    boolean SYNTHETIC = true;
    boolean NOT_SYNTHETIC = false;
    boolean isSynthetic();
}
