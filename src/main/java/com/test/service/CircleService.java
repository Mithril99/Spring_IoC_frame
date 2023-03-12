package com.test.service;

import com.spring.Annotations.Autowired;
import com.spring.Annotations.Component;

@Component("circleService")
public class CircleService {

    @Autowired
    private OrderService orderService;

    public void test(){
        System.out.println(this.getClass().getSimpleName());
        System.out.println(this.orderService.getClass().getSimpleName());
    }
}
