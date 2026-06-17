package com.example.bookmanagement.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /** 操作类型 */
    String value();

    /** 操作描述 */
    String description() default "";
}