package com.myorm;

import com.myorm.annotation.Column;
import com.myorm.annotation.Entity;
import com.myorm.core.Session;
import com.myorm.core.SessionFactory;
import com.myorm.core.TransactionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ORM框架测试主类
 */
public class Main {

    public static void main(String[] args) {
        // 创建会话工厂
        SessionFactory sessionFactory = SessionFactory.builder()
                .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .build();
        
        // 打开会话
        Session session = sessionFactory.openSession();
        
        try {
            // 创建用户表
            session.execute(
                    "CREATE TABLE IF NOT EXISTS USER (" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "USERNAME VARCHAR(50) NOT NULL, " +
                    "EMAIL VARCHAR(100), " +
                    "AGE INT);", 
                    null);
            
            System.out.println("创建用户表成功");
            
            // 创建用户
            User user = new User();
            user.setUsername("张三");
            user.setEmail("zhangsan@example.com");
            user.setAge(25);
            
            // 保存用户
            int result = session.save(user);
            System.out.println("保存用户结果: " + result);
            
            // 查询所有用户
            List<User> allUsers = session.findAll(User.class);
            System.out.println("查询所有用户结果: " + allUsers.size());
            for (User u : allUsers) {
                System.out.println(u);
            }
            
            // 根据条件查询用户
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("username", "张三");
            List<User> users = session.findByCriteria(User.class, conditions);
            System.out.println("根据条件查询用户结果: " + users.size());
            
            if (!users.isEmpty()) {
                User foundUser = users.get(0);
                System.out.println("找到用户: " + foundUser);
                
                // 更新用户
                foundUser.setAge(30);
                result = session.update(foundUser);
                System.out.println("更新用户结果: " + result);
                
                // 验证更新结果
                User updatedUser = session.findById(User.class, foundUser.getId());
                System.out.println("更新后的用户: " + updatedUser);
                
                // 删除用户
                result = session.delete(User.class, updatedUser.getId());
                System.out.println("删除用户结果: " + result);
                
                // 验证删除结果
                List<User> remainingUsers = session.findAll(User.class);
                System.out.println("删除后剩余用户数: " + remainingUsers.size());
            }
            
            // 测试事务
            System.out.println("\n开始测试事务...");
            TransactionManager transactionManager = new TransactionManager(session);
            
            // 在事务中执行操作
            transactionManager.executeInTransaction(s -> {
                // 创建用户1
                User user1 = new User();
                user1.setUsername("李四");
                user1.setEmail("lisi@example.com");
                user1.setAge(28);
                s.save(user1);
                System.out.println("在事务中创建用户1: " + user1.getUsername());
                
                // 创建用户2
                User user2 = new User();
                user2.setUsername("王五");
                user2.setEmail("wangwu@example.com");
                user2.setAge(32);
                s.save(user2);
                System.out.println("在事务中创建用户2: " + user2.getUsername());
                
                return null;
            });
            
            // 验证事务提交结果
            List<User> usersAfterTransaction = session.findAll(User.class);
            System.out.println("事务提交后的用户数: " + usersAfterTransaction.size());
            for (User u : usersAfterTransaction) {
                System.out.println(u);
            }
            
            // 删除用户表
            session.execute("DROP TABLE IF EXISTS USER;", null);
            System.out.println("删除用户表成功");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭会话
            session.close();
            System.out.println("会话已关闭");
        }
    }
    
    @Entity(table = "USER")
    public static class User {
        @Column(primaryKey = true, autoIncrement = true)
        private Integer id;
        
        @Column
        private String username;
        
        @Column
        private String email;
        
        @Column
        private Integer age;
        
        public Integer getId() {
            return id;
        }
        
        public void setId(Integer id) {
            this.id = id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public Integer getAge() {
            return age;
        }
        
        public void setAge(Integer age) {
            this.age = age;
        }
        
        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
