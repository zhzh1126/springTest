package com.zz.mvcframework.servlet;


import com.zz.demo.model.Handler;
import com.zz.mvcframework.annotation.ZZAutowired;
import com.zz.mvcframework.annotation.ZZController;
import com.zz.mvcframework.annotation.ZZRequestMapping;
import com.zz.mvcframework.annotation.ZZService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by zz on 2018/9/1.
 */
public class DispatcherServlet extends HttpServlet {
    Properties contextConfig  = new Properties();
    //存放扫描并且通过反射实例化的对象
    HashMap<String,Object> iocObjectMap = new HashMap<>();
    ArrayList<String> classNameList = new ArrayList<>();
    ArrayList<Handler> handlerList = new ArrayList<>();
    @Override
    public void init(ServletConfig config) throws ServletException {
        //读取配置
        doLoadConfig(config);
        //通过解析配置文件中的内容，扫描出所有的类
        doScan(contextConfig.getProperty("scanPackage"));
        //实例化
        doInstance();
        //注入属性对象
        doAutowried();
    }

    private void doAutowried() {
    }

    private void doInstance() {
        if(classNameList.isEmpty()){
            return;
        }
        for (String className:classNameList ) {
            try {
                Class<?> clazz = Class.forName(className);
                //只初始化有ZZController 和 ZZService 注解的类
                if(clazz.isAnnotationPresent(ZZController.class)){

                }else if (clazz.isAnnotationPresent(ZZService.class)){

                }else{
                    continue;
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void doLoadConfig(ServletConfig config) {
        InputStream is = this.getClass().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void doScan(String packagePath) {
        packagePath = packagePath.replace(".",File.separator);
        URL url =  this.getClass().getClassLoader().getResource(packagePath);
        //test dubbger测试一下这里url.getFile()的返回值最后有没有“/”
        File classDir = new File(url.getFile());
        for(File file:classDir.listFiles()){
            if(file.isDirectory()){
               doScan(packagePath+"."+file.getName());
            }else{
                classNameList.add(packagePath+"."+file.getName().replace(".class",""));
            }
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    public static void main(String[] args) {

     //   test1();
        test2();

    }

    private static void test2() {
        String a = "a.b.c.d";
        String b = a.replace(".","*");
        System.out.println(b);
    }

    private static void test1() {
        URL url  = DispatcherServlet.class.getClassLoader().getResource("com/zz/demo/controller");
        System.out.println(url.getPath());
        try {

      //      Class<?> clazz = Class.forName(path+"com"+File.separator+"zz"+File.separator+"demo"+File.separator+"controller"+File.separator+"DemoController");
            Class<?> clazz = Class.forName("com.zz.demo.controller.DemoController");
            Field[] fields = clazz.getDeclaredFields();
            System.out.println(fields.length);

            for (int i = 0; i < fields.length; i++) {
               // fields[i].get;
                System.out.println(fields[i].getType().getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
