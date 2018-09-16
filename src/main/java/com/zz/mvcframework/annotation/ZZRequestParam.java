package com.zz.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * Created by zz on 2018/9/1.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZZRequestParam {
    String name() default  "";
    String value() default "";
}
