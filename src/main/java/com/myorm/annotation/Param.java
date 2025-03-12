package com.myorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * u6807u8bb0u65b9u6cd5u53c2u6570u7684u540du79f0uff0cu7528u4e8eu5728SQLu4e2du5f15u7528
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    /**
     * u53c2u6570u540du79f0
     */
    String value();
}
