package com.test;

import com.spring.ApplicationContext;
import com.spring.Interfaces.BeanPostProcessor;
import com.test.service.CircleService;
import com.test.service.OrderService;
import com.test.service.UserService;

public class TestApp {

    public static void main(String[] args){
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);
        System.out.println("----------------------------main--------------------------");

        CircleService circleService = (CircleService)ApplicationContext.getBean("circleService");
        circleService.test();

        OrderService orderService = (OrderService)ApplicationContext.getBean("orderService");
        orderService.test();
    }
}
