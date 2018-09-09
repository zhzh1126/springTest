package com.zz.demo.service.impl;

import com.zz.demo.service.DemoService;

/**
 * Created by zz on 2018/9/1.
 */
public class DemoServiceImpl implements DemoService{

    @Override
    public String get(String name) {
        return "Hello "+name;
    }
}
