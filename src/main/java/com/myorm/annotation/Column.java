package com.myorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 列注解，用于标记实体类的属性与数据库表列的映射关系
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * 列名，默认为属性名
     */
    String name() default "";
    
    /**
     * 是否为主键
     */
    boolean primaryKey() default false;
    
    /**
     * 是否自增
     */
    boolean autoIncrement() default false;
    
    /**
     * 是否允许为空
     */
    boolean nullable() default true;
}
