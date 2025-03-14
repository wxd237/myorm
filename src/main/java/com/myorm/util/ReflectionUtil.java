package com.myorm.util;

import com.myorm.annotation.Column;
import com.myorm.annotation.Entity;
import com.myorm.exception.OrmException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反射工具类，用于处理实体类的反射操作
 */
public class ReflectionUtil {

    /**
     * 获取实体类对应的表名
     * @param clazz 实体类Class对象
     * @return 表名
     */
    public static String getTableName(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new OrmException("类" + clazz.getName() + "不是一个实体类，缺少@Entity注解");
        }
        
        Entity entity = clazz.getAnnotation(Entity.class);
        String tableName = entity.table();
        if (tableName == null || tableName.trim().isEmpty()) {
            // 如果没有指定表名，则使用类名作为表名（转换为大写）
            tableName = clazz.getSimpleName().toUpperCase();
        }
        
        // 在H2数据库中，表名需要用双引号括起来，尤其是当表名是保留关键字时
        return "\"" + tableName + "\"";
    }
    
    /**
     * 获取实体类的所有带有@Column注解的字段
     * @param clazz 实体类Class对象
     * @return 字段列表
     */
    public static List<Field> getColumnFields(Class<?> clazz) {
        List<Field> columnFields = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                field.setAccessible(true);
                columnFields.add(field);
            }
        }
        
        return columnFields;
    }
    
    /**
     * 获取实体类的主键字段
     * @param clazz 实体类Class对象
     * @return 主键字段，如果没有则返回null
     */
    public static Field getPrimaryKeyField(Class<?> clazz) {
        List<Field> columnFields = getColumnFields(clazz);
        
        for (Field field : columnFields) {
            Column column = field.getAnnotation(Column.class);
            if (column.primaryKey()) {
                return field;
            }
        }
        
        return null;
    }
    
    /**
     * 获取字段对应的列名
     * @param field 字段
     * @return 列名
     */
    public static String getColumnName(Field field) {
        if (!field.isAnnotationPresent(Column.class)) {
            throw new OrmException("字段" + field.getName() + "不是一个列，缺少@Column注解");
        }
        
        Column column = field.getAnnotation(Column.class);
        String columnName = column.name();
        if (columnName == null || columnName.trim().isEmpty()) {
            // 如果没有指定列名，则使用字段名作为列名（转换为大写）
            columnName = field.getName().toUpperCase();
        }
        
        // 在H2数据库中，列名也需要用双引号括起来，尤其是当列名是保留关键字时
        return "\"" + columnName + "\"";
    }
    
    /**
     * 将实体对象转换为字段名和值的映射
     * @param entity 实体对象
     * @return 字段名和值的映射
     */
    public static Map<String, Object> entityToMap(Object entity) {
        if (entity == null) {
            return null;
        }
        
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = entity.getClass();
        List<Field> columnFields = getColumnFields(clazz);
        
        for (Field field : columnFields) {
            try {
                String columnName = getColumnName(field);
                Object value = field.get(entity);
                map.put(columnName, value);
            } catch (IllegalAccessException e) {
                throw new OrmException("无法访问字段" + field.getName(), e);
            }
        }
        
        return map;
    }
    
    /**
     * 将结果集映射到实体对象
     * @param clazz 实体类Class对象
     * @param columnValues 列名和值的映射
     * @param <T> 实体类型
     * @return 实体对象
     */
    public static <T> T mapToEntity(Class<T> clazz, Map<String, Object> columnValues) {
        if (columnValues == null || columnValues.isEmpty()) {
            return null;
        }
        
        try {
            T entity = clazz.getDeclaredConstructor().newInstance();
            List<Field> columnFields = getColumnFields(clazz);
            
            for (Field field : columnFields) {
                String columnName = getColumnName(field);
                String rawColumnName = columnName.replace("\"", ""); // 移除双引号
                
                // 尝试使用带双引号的列名和不带双引号的列名
                Object value = null;
                if (columnValues.containsKey(columnName)) {
                    value = columnValues.get(columnName);
                } else if (columnValues.containsKey(rawColumnName)) {
                    value = columnValues.get(rawColumnName);
                }
                
                if (value != null) {
                    // 处理类型转换
                    value = convertValueType(value, field.getType());
                    field.set(entity, value);
                }
                
            }
            
            return entity;
        } catch (Exception e) {
            throw new OrmException("无法创建实体对象", e);
        }
    }
    
    /**
     * 转换值的类型
     * @param value 原始值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    private static Object convertValueType(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        // 如果类型已经匹配，则直接返回
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        
        // 处理基本类型的转换
        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else {
                return Integer.parseInt(value.toString());
            }
        } else if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            } else {
                return Long.parseLong(value.toString());
            }
        } else if (targetType == Double.class || targetType == double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else {
                return Double.parseDouble(value.toString());
            }
        } else if (targetType == Float.class || targetType == float.class) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            } else {
                return Float.parseFloat(value.toString());
            }
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            } else {
                return Boolean.parseBoolean(value.toString());
            }
        }
        
        // 默认返回原值的字符串表示
        return value.toString();
    }
}
