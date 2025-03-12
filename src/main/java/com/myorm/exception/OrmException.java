package com.myorm.exception;

/**
 * ORM框架自定义异常类
 */
public class OrmException extends RuntimeException {
    
    public OrmException(String message) {
        super(message);
    }
    
    public OrmException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public OrmException(Throwable cause) {
        super(cause);
    }
}
