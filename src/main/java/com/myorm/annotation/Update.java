package com.myorm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * u6807u8bb0u4e00u4e2au66f4u65b0u65b9u6cd5
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Update {
    /**
     * SQLu66f4u65b0u8bedu53e5
     */
    String value();
}
