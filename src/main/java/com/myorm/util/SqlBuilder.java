package com.myorm.util;

import com.myorm.annotation.Column;
import com.myorm.exception.OrmException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * SQL构建工具类，用于生成SQL语句
 */
public class SqlBuilder {

    /**
     * 生成插入SQL语句
     * @param entity 实体对象
     * @return SQL语句和参数
     */
    public static SqlAndParams buildInsertSql(Object entity) {
        if (entity == null) {
            throw new OrmException("实体对象不能为空");
        }
        
        Class<?> clazz = entity.getClass();
        String tableName = ReflectionUtil.getTableName(clazz);
        List<Field> columnFields = ReflectionUtil.getColumnFields(clazz);
        
        // 过滤掉自增主键字段
        List<Field> fieldsToInsert = new ArrayList<>();
        for (Field field : columnFields) {
            Column column = field.getAnnotation(Column.class);
            if (!(column.primaryKey() && column.autoIncrement())) {
                fieldsToInsert.add(field);
            }
        }
        
        if (fieldsToInsert.isEmpty()) {
            throw new OrmException("没有可插入的字段");
        }
        
        StringJoiner columnJoiner = new StringJoiner(", ");
        StringJoiner placeholderJoiner = new StringJoiner(", ");
        List<Object> params = new ArrayList<>();
        
        for (Field field : fieldsToInsert) {
            try {
                String columnName = ReflectionUtil.getColumnName(field);
                Object value = field.get(entity);
                
                columnJoiner.add(columnName);
                placeholderJoiner.add("?");
                params.add(value);
            } catch (IllegalAccessException e) {
                throw new OrmException("无法访问字段" + field.getName(), e);
            }
        }
        
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", 
                tableName, columnJoiner.toString(), placeholderJoiner.toString());
        
        return new SqlAndParams(sql, params);
    }
    
    /**
     * 生成更新SQL语句
     * @param entity 实体对象
     * @return SQL语句和参数
     */
    public static SqlAndParams buildUpdateSql(Object entity) {
        if (entity == null) {
            throw new OrmException("实体对象不能为空");
        }
        
        Class<?> clazz = entity.getClass();
        String tableName = ReflectionUtil.getTableName(clazz);
        Field primaryKeyField = ReflectionUtil.getPrimaryKeyField(clazz);
        
        if (primaryKeyField == null) {
            throw new OrmException("实体类" + clazz.getName() + "没有定义主键字段");
        }
        
        List<Field> columnFields = ReflectionUtil.getColumnFields(clazz);
        StringJoiner setJoiner = new StringJoiner(", ");
        List<Object> params = new ArrayList<>();
        
        // 设置更新字段
        for (Field field : columnFields) {
            Column column = field.getAnnotation(Column.class);
            if (!column.primaryKey()) { // 排除主键字段
                try {
                    String columnName = ReflectionUtil.getColumnName(field);
                    Object value = field.get(entity);
                    
                    setJoiner.add(columnName + " = ?");
                    params.add(value);
                } catch (IllegalAccessException e) {
                    throw new OrmException("无法访问字段" + field.getName(), e);
                }
            }
        }
        
        // 设置WHERE条件（主键）
        String primaryKeyColumnName = ReflectionUtil.getColumnName(primaryKeyField);
        Object primaryKeyValue;
        try {
            primaryKeyValue = primaryKeyField.get(entity);
        } catch (IllegalAccessException e) {
            throw new OrmException("无法访问主键字段" + primaryKeyField.getName(), e);
        }
        
        if (primaryKeyValue == null) {
            throw new OrmException("主键值不能为空");
        }
        
        params.add(primaryKeyValue);
        
        String sql = String.format("UPDATE %s SET %s WHERE %s = ?", 
                tableName, setJoiner.toString(), primaryKeyColumnName);
        
        return new SqlAndParams(sql, params);
    }
    
    /**
     * 生成删除SQL语句
     * @param clazz 实体类Class对象
     * @param id 主键值
     * @return SQL语句和参数
     */
    public static SqlAndParams buildDeleteSql(Class<?> clazz, Object id) {
        if (clazz == null) {
            throw new OrmException("实体类不能为空");
        }
        
        if (id == null) {
            throw new OrmException("主键值不能为空");
        }
        
        String tableName = ReflectionUtil.getTableName(clazz);
        Field primaryKeyField = ReflectionUtil.getPrimaryKeyField(clazz);
        
        if (primaryKeyField == null) {
            throw new OrmException("实体类" + clazz.getName() + "没有定义主键字段");
        }
        
        String primaryKeyColumnName = ReflectionUtil.getColumnName(primaryKeyField);
        List<Object> params = new ArrayList<>();
        params.add(id);
        
        String sql = String.format("DELETE FROM %s WHERE %s = ?", tableName, primaryKeyColumnName);
        
        return new SqlAndParams(sql, params);
    }
    
    /**
     * 生成查询SQL语句
     * @param clazz 实体类Class对象
     * @param id 主键值
     * @return SQL语句和参数
     */
    public static SqlAndParams buildSelectByIdSql(Class<?> clazz, Object id) {
        if (clazz == null) {
            throw new OrmException("实体类不能为空");
        }
        
        if (id == null) {
            throw new OrmException("主键值不能为空");
        }
        
        String tableName = ReflectionUtil.getTableName(clazz);
        Field primaryKeyField = ReflectionUtil.getPrimaryKeyField(clazz);
        
        if (primaryKeyField == null) {
            throw new OrmException("实体类" + clazz.getName() + "没有定义主键字段");
        }
        
        String primaryKeyColumnName = ReflectionUtil.getColumnName(primaryKeyField);
        List<Object> params = new ArrayList<>();
        params.add(id);
        
        String sql = String.format("SELECT * FROM %s WHERE %s = ?", tableName, primaryKeyColumnName);
        
        return new SqlAndParams(sql, params);
    }
    
    /**
     * 生成查询所有记录的SQL语句
     * @param clazz 实体类Class对象
     * @return SQL语句和参数
     */
    public static SqlAndParams buildSelectAllSql(Class<?> clazz) {
        if (clazz == null) {
            throw new OrmException("实体类不能为空");
        }
        
        String tableName = ReflectionUtil.getTableName(clazz);
        String sql = String.format("SELECT * FROM %s", tableName);
        
        return new SqlAndParams(sql, new ArrayList<>());
    }
    
    /**
     * 生成条件查询的SQL语句
     * @param clazz 实体类Class对象
     * @param conditions 条件映射（列名 -> 值）
     * @return SQL语句和参数
     */
    public static SqlAndParams buildSelectByCriteriaSql(Class<?> clazz, Map<String, Object> conditions) {
        if (clazz == null) {
            throw new OrmException("实体类不能为空");
        }
        
        String tableName = ReflectionUtil.getTableName(clazz);
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM ").append(tableName);
        
        List<Object> params = new ArrayList<>();
        
        if (conditions != null && !conditions.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            StringJoiner conditionJoiner = new StringJoiner(" AND ");
            
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                String columnName = "\"" + entry.getKey() + "\"";
                Object value = entry.getValue();
                
                if (value == null) {
                    conditionJoiner.add(columnName + " IS NULL");
                } else {
                    conditionJoiner.add(columnName + " = ?");
                    params.add(value);
                }
            }
            
            sqlBuilder.append(conditionJoiner.toString());
        }
        
        return new SqlAndParams(sqlBuilder.toString(), params);
    }
    
    /**
     * SQL语句和参数的封装类
     */
    public static class SqlAndParams {
        private final String sql;
        private final List<Object> params;
        
        public SqlAndParams(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }
        
        public String getSql() {
            return sql;
        }
        
        public List<Object> getParams() {
            return params;
        }
    }
}
