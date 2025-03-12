package com.myorm.core;

import com.myorm.exception.OrmException;
import com.myorm.util.ReflectionUtil;
import com.myorm.util.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ORM会话类，用于执行数据库操作
 */
public class Session {
    private static final Logger logger = LoggerFactory.getLogger(Session.class);
    private final Connection connection;
    
    public Session(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * 保存实体对象
     * @param entity 实体对象
     * @param <T> 实体类型
     * @return 受影响的行数
     */
    public <T> int save(T entity) {
        if (entity == null) {
            throw new OrmException("实体对象不能为空");
        }
        
        SqlBuilder.SqlAndParams sqlAndParams = SqlBuilder.buildInsertSql(entity);
        String sql = sqlAndParams.getSql();
        List<Object> params = sqlAndParams.getParams();
        
        logger.debug("执行SQL: {}", sql);
        logger.debug("参数: {}", params);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OrmException("保存实体对象失败", e);
        }
    }
    
    /**
     * 更新实体对象
     * @param entity 实体对象
     * @param <T> 实体类型
     * @return 受影响的行数
     */
    public <T> int update(T entity) {
        if (entity == null) {
            throw new OrmException("实体对象不能为空");
        }
        
        SqlBuilder.SqlAndParams sqlAndParams = SqlBuilder.buildUpdateSql(entity);
        String sql = sqlAndParams.getSql();
        List<Object> params = sqlAndParams.getParams();
        
        logger.debug("执行SQL: {}", sql);
        logger.debug("参数: {}", params);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OrmException("更新实体对象失败", e);
        }
    }
    
    /**
     * 删除实体对象
     * @param clazz 实体类Class对象
     * @param id 主键值
     * @param <T> 实体类型
     * @return 受影响的行数
     */
    public <T> int delete(Class<T> clazz, Object id) {
        if (clazz == null) {
            throw new OrmException("实体类不能为空");
        }
        
        if (id == null) {
            throw new OrmException("主键值不能为空");
        }
        
        SqlBuilder.SqlAndParams sqlAndParams = SqlBuilder.buildDeleteSql(clazz, id);
        String sql = sqlAndParams.getSql();
        List<Object> params = sqlAndParams.getParams();
        
        logger.debug("执行SQL: {}", sql);
        logger.debug("参数: {}", params);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OrmException("删除实体对象失败", e);
        }
    }
    
    /**
     * 根据主键查询实体对象
     * @param clazz 实体类Class对象
     * @param id 主键值
     * @param <T> 实体类型
     * @return 实体对象，如果不存在则返回null
     */
    public <T> T findById(Class<T> clazz, Object id) {
        if (clazz == null) {
            throw new OrmException("实体类不能为空");
        }
        
        if (id == null) {
            throw new OrmException("主键值不能为空");
        }
        
        SqlBuilder.SqlAndParams sqlAndParams = SqlBuilder.buildSelectByIdSql(clazz, id);
        String sql = sqlAndParams.getSql();
        List<Object> params = sqlAndParams.getParams();
        
        logger.debug("执行SQL: {}", sql);
        logger.debug("参数: {}", params);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, params);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEntity(rs, clazz);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new OrmException("查询实体对象失败", e);
        }
    }
    
    /**
     * 查询所有实体对象
     * @param clazz 实体类Class对象
     * @param <T> 实体类型
     * @return 实体对象列表
     */
    public <T> List<T> findAll(Class<T> clazz) {
        if (clazz == null) {
            throw new OrmException("实体类不能为空");
        }
        
        SqlBuilder.SqlAndParams sqlAndParams = SqlBuilder.buildSelectAllSql(clazz);
        String sql = sqlAndParams.getSql();
        List<Object> params = sqlAndParams.getParams();
        
        logger.debug("执行SQL: {}", sql);
        logger.debug("参数: {}", params);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, params);
            ResultSet rs = stmt.executeQuery();
            
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                T entity = mapResultSetToEntity(rs, clazz);
                results.add(entity);
            }
            
            return results;
        } catch (SQLException e) {
            throw new OrmException("查询实体对象失败", e);
        }
    }
    
    /**
     * 根据条件查询实体对象
     * @param clazz 实体类Class对象
     * @param conditions 条件映射（列名 -> 值）
     * @param <T> 实体类型
     * @return 实体对象列表
     */
    public <T> List<T> findByCriteria(Class<T> clazz, Map<String, Object> conditions) {
        if (clazz == null) {
            throw new OrmException("实体类不能为空");
        }
        
        SqlBuilder.SqlAndParams sqlAndParams = SqlBuilder.buildSelectByCriteriaSql(clazz, conditions);
        String sql = sqlAndParams.getSql();
        List<Object> params = sqlAndParams.getParams();
        
        logger.debug("执行SQL: {}", sql);
        logger.debug("参数: {}", params);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, params);
            ResultSet rs = stmt.executeQuery();
            
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                T entity = mapResultSetToEntity(rs, clazz);
                results.add(entity);
            }
            
            return results;
        } catch (SQLException e) {
            throw new OrmException("查询实体对象失败", e);
        }
    }
    
    /**
     * 执行自定义SQL查询
     * @param sql SQL语句
     * @param params 参数列表
     * @param clazz 实体类Class对象
     * @param <T> 实体类型
     * @return 实体对象列表
     */
    public <T> List<T> query(String sql, List<Object> params, Class<T> clazz) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new OrmException("SQL语句不能为空");
        }
        
        if (clazz == null) {
            throw new OrmException("实体类不能为空");
        }
        
        logger.debug("执行SQL: {}", sql);
        logger.debug("参数: {}", params);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (params != null) {
                setParameters(stmt, params);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                T entity = mapResultSetToEntity(rs, clazz);
                results.add(entity);
            }
            
            return results;
        } catch (SQLException e) {
            throw new OrmException("执行查询失败", e);
        }
    }
    
    /**
     * 执行自定义SQL更新
     * @param sql SQL语句
     * @param params 参数列表
     * @return 受影响的行数
     */
    public int execute(String sql, List<Object> params) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new OrmException("SQL语句不能为空");
        }
        
        logger.debug("执行SQL: {}", sql);
        logger.debug("参数: {}", params);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (params != null) {
                setParameters(stmt, params);
            }
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OrmException("执行更新失败", e);
        }
    }
    
    /**
     * 开始事务
     */
    public void beginTransaction() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new OrmException("开始事务失败", e);
        }
    }
    
    /**
     * 提交事务
     */
    public void commit() {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new OrmException("提交事务失败", e);
        }
    }
    
    /**
     * 回滚事务
     */
    public void rollback() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new OrmException("回滚事务失败", e);
        }
    }
    
    /**
     * 关闭会话
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new OrmException("关闭会话失败", e);
        }
    }
    
    /**
     * 设置PreparedStatement的参数
     * @param stmt PreparedStatement对象
     * @param params 参数列表
     * @throws SQLException SQL异常
     */
    private void setParameters(PreparedStatement stmt, List<Object> params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
        }
    }
    
    /**
     * 将ResultSet映射为实体对象
     * @param rs ResultSet对象
     * @param clazz 实体类Class对象
     * @param <T> 实体类型
     * @return 实体对象
     * @throws SQLException SQL异常
     */
    /**
     * 执行自定义SQL查询，返回Map列表
     * @param sql SQL语句
     * @param params 参数列表
     * @return Map列表（列名 -> 值）
     */
    public List<Map<String, Object>> queryForMap(String sql, List<Object> params) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParameters(stmt, params);
            
            logger.debug("执行SQL: {}", sql);
            logger.debug("参数: {}", params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            throw new OrmException("执行SQL查询失败: " + sql, e);
        }
        
        return results;
    }
    
    /**
     * 获取Mapper接口的实现
     * @param mapperInterface Mapper接口的Class对象
     * @param <T> Mapper接口类型
     * @return Mapper接口的实现
     */
    public <T> T getMapper(Class<T> mapperInterface) {
        // 延迟导入MapperProxyFactory，避免循环依赖
        return new com.myorm.core.MapperProxyFactory(this).createMapper(mapperInterface);
    }
    
    private <T> T mapResultSetToEntity(ResultSet rs, Class<T> clazz) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        Map<String, Object> columnValues = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            Object value = rs.getObject(i);
            columnValues.put(columnName, value);
        }
        
        return ReflectionUtil.mapToEntity(clazz, columnValues);
    }
}
