package com.test.service;

import com.spring.Annotations.Autowired;
import com.spring.Annotations.Component;
import com.spring.Annotations.Scope;

@Component("userService")
@Scope()
public class UserService {

    @Autowired
    private OrderService orderService;

    public void test(){
        System.out.println("test content " + orderService);
    }
}
