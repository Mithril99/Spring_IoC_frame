package com.spring.components.AOP;

import com.spring.Annotations.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
public class MethodBeforeAdviceList implements Iterable<MethodBeforeAdvice>{

    private final List<MethodBeforeAdvice> advices;

    public MethodBeforeAdviceList() {
        this.advices = new ArrayList<>();
    }

    public void add(MethodBeforeAdvice methodBeforeAdvice){
        advices.add(methodBeforeAdvice);
    }

    public MethodBeforeAdvice get(int index){
        return advices.get(index);
    }

    public int size(){
        return advices.size();
    }

    public boolean isEmpty(){
        return advices.isEmpty();
    }

    @Override
    public Iterator iterator() {
        return advices.iterator();
    }

    @Override
    public void forEach(Consumer action) {
        advices.forEach(action);
    }

    @Override
    public Spliterator spliterator() {
        return advices.spliterator();
    }
}
