package com.zz.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * Created by zz on 2018/9/1.
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZZRequestMapping {
    String value() default "";
}
