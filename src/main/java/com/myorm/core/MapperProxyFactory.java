package com.myorm.core;

import com.myorm.annotation.*;
import com.myorm.exception.OrmException;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper代理工厂，用于创建Mapper接口的代理实现
 */
public class MapperProxyFactory {
    
    private final Session session;
    
    public MapperProxyFactory(Session session) {
        this.session = session;
    }
    
    /**
     * 创建Mapper接口的代理实现
     * @param mapperInterface Mapper接口的Class对象
     * @param <T> Mapper接口类型
     * @return Mapper接口的代理实现
     */
    @SuppressWarnings("unchecked")
    public <T> T createMapper(Class<T> mapperInterface) {
        if (mapperInterface == null) {
            throw new OrmException("Mapper接口不能为空");
        }
        
        if (!mapperInterface.isInterface()) {
            throw new OrmException("Mapper必须是接口");
        }
        
        if (!mapperInterface.isAnnotationPresent(Mapper.class)) {
            throw new OrmException("接口" + mapperInterface.getName() + "没有@Mapper注解");
        }
        
        return (T) Proxy.newProxyInstance(
                mapperInterface.getClassLoader(),
                new Class<?>[]{mapperInterface},
                new MapperProxy(session, mapperInterface));
    }
    
    /**
     * Mapper接口的代理处理器
     */
    private static class MapperProxy implements InvocationHandler {
        
        private final Session session;
        
        public MapperProxy(Session session, Class<?> mapperInterface) {
            this.session = session;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 如果是Object类的方法，直接调用
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            
            // 处理接口方法
            return executeMapperMethod(method, args);
        }
        
        private Object executeMapperMethod(Method method, Object[] args) {
            // 处理Select注解
            if (method.isAnnotationPresent(Select.class)) {
                return executeSelect(method, args);
            }
            
            // 处理Insert注解
            if (method.isAnnotationPresent(Insert.class)) {
                return executeInsert(method, args);
            }
            
            // 处理Update注解
            if (method.isAnnotationPresent(Update.class)) {
                return executeUpdate(method, args);
            }
            
            // 处理Delete注解
            if (method.isAnnotationPresent(Delete.class)) {
                return executeDelete(method, args);
            }
            
            throw new OrmException("方法" + method.getName() + "没有SQL操作注解");
        }
        
        private Object executeSelect(Method method, Object[] args) {
            String sql = method.getAnnotation(Select.class).value();
            List<Object> params = extractParams(method, args);
            
            // 处理返回类型
            Class<?> returnType = method.getReturnType();
            
            // 如果返回类型是List
            if (List.class.isAssignableFrom(returnType)) {
                // 获取泛型类型
                Type genericReturnType = method.getGenericReturnType();
                if (genericReturnType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) genericReturnType;
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                        Class<?> entityClass = (Class<?>) typeArgs[0];
                        return session.query(sql, params, entityClass);
                    }
                }
                throw new OrmException("无法确定List的泛型类型");
            }
            
            // 如果返回单个对象
            if (!returnType.isPrimitive() && !returnType.equals(String.class) && 
                    !Number.class.isAssignableFrom(returnType)) {
                List<?> results = session.query(sql, params, returnType);
                return results.isEmpty() ? null : results.get(0);
            }
            
            // 如果返回基本类型或字符串
            List<Map<String, Object>> results = session.queryForMap(sql, params);
            if (results.isEmpty()) {
                if (returnType.isPrimitive()) {
                    if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
                        return false;
                    } else {
                        return 0;
                    }
                }
                return null;
            }
            
            Map<String, Object> row = results.get(0);
            if (row.isEmpty()) {
                return null;
            }
            
            Object value = row.values().iterator().next();
            if (value == null) {
                return null;
            }
            
            // 类型转换
            if (returnType.equals(Integer.class) || returnType.equals(int.class)) {
                return Integer.valueOf(value.toString());
            } else if (returnType.equals(Long.class) || returnType.equals(long.class)) {
                return Long.valueOf(value.toString());
            } else if (returnType.equals(Double.class) || returnType.equals(double.class)) {
                return Double.valueOf(value.toString());
            } else if (returnType.equals(Float.class) || returnType.equals(float.class)) {
                return Float.valueOf(value.toString());
            } else if (returnType.equals(Boolean.class) || returnType.equals(boolean.class)) {
                return Boolean.valueOf(value.toString());
            } else if (returnType.equals(String.class)) {
                return value.toString();
            }
            
            return value;
        }
        
        private Object executeInsert(Method method, Object[] args) {
            String sql = method.getAnnotation(Insert.class).value();
            List<Object> params = extractParams(method, args);
            
            int result = session.execute(sql, params);
            
            // 如果返回类型是int或Integer，返回受影响的行数
            Class<?> returnType = method.getReturnType();
            if (returnType.equals(int.class) || returnType.equals(Integer.class)) {
                return result;
            }
            
            // 如果返回类型是boolean或Boolean，返回是否成功
            if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
                return result > 0;
            }
            
            return null;
        }
        
        private Object executeUpdate(Method method, Object[] args) {
            String sql = method.getAnnotation(Update.class).value();
            List<Object> params = extractParams(method, args);
            
            int result = session.execute(sql, params);
            
            // 如果返回类型是int或Integer，返回受影响的行数
            Class<?> returnType = method.getReturnType();
            if (returnType.equals(int.class) || returnType.equals(Integer.class)) {
                return result;
            }
            
            // 如果返回类型是boolean或Boolean，返回是否成功
            if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
                return result > 0;
            }
            
            return null;
        }
        
        private Object executeDelete(Method method, Object[] args) {
            String sql = method.getAnnotation(Delete.class).value();
            List<Object> params = extractParams(method, args);
            
            int result = session.execute(sql, params);
            
            // 如果返回类型是int或Integer，返回受影响的行数
            Class<?> returnType = method.getReturnType();
            if (returnType.equals(int.class) || returnType.equals(Integer.class)) {
                return result;
            }
            
            // 如果返回类型是boolean或Boolean，返回是否成功
            if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
                return result > 0;
            }
            
            return null;
        }
        
        private List<Object> extractParams(Method method, Object[] args) {
            if (args == null || args.length == 0) {
                return new ArrayList<>();
            }
            
            // 获取参数注解
            Parameter[] parameters = method.getParameters();
            Map<String, Integer> paramIndexMap = new HashMap<>();
            
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (parameter.isAnnotationPresent(Param.class)) {
                    Param param = parameter.getAnnotation(Param.class);
                    paramIndexMap.put(param.value(), i);
                }
            }
            
            // 如果没有@Param注解，直接返回参数数组
            if (paramIndexMap.isEmpty()) {
                List<Object> params = new ArrayList<>();
                for (Object arg : args) {
                    params.add(arg);
                }
                return params;
            }
            
            // 处理命名参数
            // 这里简化处理，实际上需要解析SQL中的参数占位符
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                params.add(args[i]);
            }
            
            return params;
        }
    }
}
