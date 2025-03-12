package com.myorm.core;

import com.myorm.exception.OrmException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 会话工厂类，用于创建和管理数据库会话
 */
public class SessionFactory {
    private static final Logger logger = LoggerFactory.getLogger(SessionFactory.class);
    private final String url;
    private final String username;
    private final String password;
    
    /**
     * 创建会话工厂
     * @param url 数据库URL
     * @param username 用户名
     * @param password 密码
     */
    public SessionFactory(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        
        try {
            // 尝试加载数据库驱动
            if (url.contains("mysql")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if (url.contains("h2")) {
                Class.forName("org.h2.Driver");
            } else if (url.contains("oracle")) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
            } else if (url.contains("postgresql")) {
                Class.forName("org.postgresql.Driver");
            } else if (url.contains("sqlserver")) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            }
        } catch (ClassNotFoundException e) {
            logger.warn("无法加载数据库驱动: {}", e.getMessage());
        }
    }
    
    /**
     * 打开一个新的会话
     * @return 会话对象
     */
    public Session openSession() {
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            return new Session(connection);
        } catch (SQLException e) {
            throw new OrmException("无法创建数据库连接", e);
        }
    }
    
    /**
     * 创建一个会话工厂构建器
     * @return 会话工厂构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 会话工厂构建器，用于构建会话工厂
     */
    public static class Builder {
        private String url;
        private String username;
        private String password;
        
        /**
         * 设置数据库URL
         * @param url 数据库URL
         * @return 构建器对象
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }
        
        /**
         * 设置用户名
         * @param username 用户名
         * @return 构建器对象
         */
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        /**
         * 设置密码
         * @param password 密码
         * @return 构建器对象
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        
        /**
         * 构建会话工厂
         * @return 会话工厂对象
         */
        public SessionFactory build() {
            if (url == null || url.trim().isEmpty()) {
                throw new OrmException("数据库URL不能为空");
            }
            
            return new SessionFactory(url, username, password);
        }
    }
}
