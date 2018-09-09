package com.zz.demo.model;

import java.lang.reflect.Method;

/**
 * Created by zz on 2018/9/2.
 */
public class Handler {
    String url;
    Method method;
    Object object;

    public Handler(String url, Method method, Object object) {
        this.url = url;
        this.method = method;
        this.object = object;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
