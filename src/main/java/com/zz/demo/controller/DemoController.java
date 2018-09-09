package com.zz.demo.controller;



import com.zz.demo.service.DemoService;
import com.zz.mvcframework.annotation.ZZAutowired;
import com.zz.mvcframework.annotation.ZZController;
import com.zz.mvcframework.annotation.ZZRequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zz on 2018/9/1.
 */
@ZZController
@ZZRequestMapping("demo")
public class DemoController {

    @ZZAutowired
    private DemoService demoService;

    @ZZRequestMapping("/query.json")
    public void query(HttpServletRequest req, HttpServletResponse res,String name){
        demoService.get(name);
    }
}
