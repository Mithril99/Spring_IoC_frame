package com.test.service;

import com.spring.Annotations.Autowired;
import com.spring.Annotations.Component;

@Component("orderService")
public class OrderService {

    @Autowired
    private CircleService circleService;

    public void test(){
        System.out.println(this.getClass().getSimpleName());
        System.out.println(this.circleService.getClass().getSimpleName());
    }
}
