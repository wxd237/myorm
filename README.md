# MyORM 框架

这是一个简单的Java ORM（对象关系映射）框架，用于简化Java应用程序与关系型数据库之间的交互。

## 功能特点

- 简单易用的API
- 支持基本的CRUD操作
- 支持事务管理
- 支持自定义SQL查询
- 使用注解映射实体类与数据库表
- 支持多种数据库（MySQL, H2, Oracle, PostgreSQL, SQL Server）

## 快速开始

### 1. 定义实体类

使用`@Entity`和`@Column`注解来定义实体类：

```java
@Entity(table = "user")
public class User {
    @Column(primaryKey = true, autoIncrement = true)
    private Integer id;
    
    @Column
    private String username;
    
    @Column
    private String email;
    
    // getter和setter方法
}
```

### 2. 创建会话工厂

```java
SessionFactory sessionFactory = SessionFactory.builder()
        .url("jdbc:mysql://localhost:3306/mydb")
        .username("root")
        .password("password")
        .build();
```

### 3. 打开会话并执行操作

```java
// 打开会话
Session session = sessionFactory.openSession();

// 创建用户
User user = new User();
user.setUsername("张三");
user.setEmail("zhangsan@example.com");
session.save(user);

// 查询用户
User foundUser = session.findById(User.class, 1);

// 更新用户
foundUser.setUsername("李四");
session.update(foundUser);

// 删除用户
session.delete(User.class, 1);

// 关闭会话
session.close();
```

### 4. 使用事务

```java
Session session = sessionFactory.openSession();
TransactionManager transactionManager = new TransactionManager(session);

transactionManager.executeInTransaction(s -> {
    // 在事务中执行的操作
    User user = new User();
    user.setUsername("王五");
    s.save(user);
    
    // 可以执行多个操作，它们要么全部成功，要么全部失败
    
    return null; // 或者返回操作结果
});

session.close();
```

## 注解说明

### @Entity

用于标记一个类为实体类，对应数据库中的一张表。

- `table`：表名，默认为类名（小写）

### @Column

用于标记一个字段为列，对应数据库表中的一列。

- `name`：列名，默认为字段名
- `primaryKey`：是否为主键，默认为false
- `autoIncrement`：是否自增，默认为false
- `nullable`：是否允许为空，默认为true

## 测试

框架使用H2内存数据库进行单元测试，无需额外配置。运行测试用例：

```bash
mvn test
```

## 注意事项

- 实体类必须有一个无参构造函数
- 实体类的字段必须有对应的getter和setter方法
- 主键字段必须使用`@Column(primaryKey = true)`标记
- 自增主键字段必须使用`@Column(primaryKey = true, autoIncrement = true)`标记
