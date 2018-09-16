package com.zz.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * Created by zz on 2018/9/1.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZZService {
    String value() default "";
}
