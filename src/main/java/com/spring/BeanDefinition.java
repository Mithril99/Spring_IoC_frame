package com.spring;

import com.spring.Annotations.Scope;

public class BeanDefinition {

    private Class clazz;
    private int scope;

    private boolean Synthetic;

    public BeanDefinition(){};

    public BeanDefinition(Class clazz, int scope, boolean synthetic){
        this.clazz = clazz;
        this.scope = scope;
        this.Synthetic = synthetic;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public boolean isSynthetic() {
        return Synthetic;
    }

    public void setSynthetic(boolean synthetic) {
        Synthetic = synthetic;
    }

    public boolean isSingleton(){
        return this.scope == Scope.SCOPE_SINGLETON;
    }
}
