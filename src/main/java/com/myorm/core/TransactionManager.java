package com.myorm.core;

import com.myorm.exception.OrmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事务管理器，用于管理数据库事务
 */
public class TransactionManager {
    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
    private final Session session;
    
    /**
     * 创建事务管理器
     * @param session 会话对象
     */
    public TransactionManager(Session session) {
        this.session = session;
    }
    
    /**
     * 在事务中执行操作
     * @param callback 事务回调接口
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T executeInTransaction(TransactionCallback<T> callback) {
        try {
            session.beginTransaction();
            logger.debug("事务已开始");
            
            T result = callback.execute(session);
            
            session.commit();
            logger.debug("事务已提交");
            
            return result;
        } catch (Exception e) {
            session.rollback();
            logger.debug("事务已回滚", e);
            throw new OrmException("事务执行失败", e);
        }
    }
    
    /**
     * 事务回调接口
     * @param <T> 返回值类型
     */
    public interface TransactionCallback<T> {
        /**
         * 在事务中执行的操作
         * @param session 会话对象
         * @return 操作结果
         */
        T execute(Session session);
    }
}
