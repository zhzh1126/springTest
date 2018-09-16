package com.zz.demo.controller;



import com.zz.demo.service.DemoService;
import com.zz.mvcframework.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by zz on 2018/9/1.
 */
@ZZController
@ZZRequestMapping("demo")
public class DemoController {

    @ZZAutowired
    private DemoService demoService;

    @ZZRequestMapping("/query.json")
    public void query(HttpServletRequest req, HttpServletResponse res, @ZZRequestParam(name="name") String name){

        try {
            res.getWriter().print(demoService.get(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("aaa");
    }
}
