package com.myorm;

import com.myorm.annotation.Column;
import com.myorm.annotation.Entity;
import com.myorm.core.Session;
import com.myorm.core.SessionFactory;
import com.myorm.core.TransactionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {
    
    private SessionFactory sessionFactory;
    private Session session;
    
    @Before
    public void setUp() {
        // 使用H2内存数据库进行测试
        sessionFactory = SessionFactory.builder()
                .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .build();
        
        session = sessionFactory.openSession();
        
        // 创建用户表
        session.execute(
                "CREATE TABLE IF NOT EXISTS \"USER\" (" +
                "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "USERNAME VARCHAR(50) NOT NULL, " +
                "EMAIL VARCHAR(100), " +
                "AGE INT);", 
                null);
    }
    
    @After
    public void tearDown() {
        // 删除用户表
        session.execute("DROP TABLE IF EXISTS \"USER\";", null);
        session.close();
    }
    
    @Test
    public void testCRUD() {
        // 创建用户
        User user = new User();
        user.setUsername("张三");
        user.setEmail("zhangsan@example.com");
        user.setAge(25);
        
        // 保存用户
        int result = session.save(user);
        assertEquals(1, (int)result);
        
        // 查询所有用户
        List<User> allUsers = session.findAll(User.class);
        assertEquals(1, (int)allUsers.size());
        
        // 根据条件查询用户
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("USERNAME", "张三");
        List<User> users = session.findByCriteria(User.class, conditions);
        assertEquals(1, (int)users.size());
        assertEquals("zhangsan@example.com", (String)users.get(0).getEmail());
        
        // 更新用户
        User userToUpdate = users.get(0);
        userToUpdate.setAge(30);
        result = session.update(userToUpdate);
        assertEquals(1, (int)result);
        
        // 验证更新结果
        User updatedUser = session.findById(User.class, userToUpdate.getId());
        assertEquals(30, (int)updatedUser.getAge());
        
        // 删除用户
        result = session.delete(User.class, updatedUser.getId());
        assertEquals(1, (int)result);
        
        // 验证删除结果
        List<User> remainingUsers = session.findAll(User.class);
        assertEquals(0, (int)remainingUsers.size());
    }
    
    @Test
    public void testTransaction() {
        TransactionManager transactionManager = new TransactionManager(session);
        
        // 在事务中执行操作
        transactionManager.executeInTransaction(s -> {
            // 创建用户1
            User user1 = new User();
            user1.setUsername("李四");
            user1.setEmail("lisi@example.com");
            user1.setAge(28);
            s.save(user1);
            
            // 创建用户2
            User user2 = new User();
            user2.setUsername("王五");
            user2.setEmail("wangwu@example.com");
            user2.setAge(32);
            s.save(user2);
            
            return null;
        });
        
        // 验证事务提交结果
        List<User> users = session.findAll(User.class);
        assertEquals(2, (int)users.size());
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
