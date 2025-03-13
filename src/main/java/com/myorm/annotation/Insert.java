package com.myorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个插入方法，自动生成SQL语句
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Insert {
    /**
     * SQL语句
     */
    String value() default "";

    /**
     * 实体类
     */
    Class<?> entityClass() default Object.class;
    
    /**
     * 数据库表名
     */
    String tableName() default "";
}
