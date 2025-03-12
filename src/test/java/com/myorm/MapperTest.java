package com.myorm;

import com.myorm.core.Session;
import com.myorm.core.SessionFactory;
import com.myorm.entity.User;
import com.myorm.mapper.UserMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Mapperu529fu80fdu6d4bu8bd5
 */
public class MapperTest {
    
    private Connection connection;
    private Session session;
    private UserMapper userMapper;
    
    @Before
    public void setUp() throws Exception {
        // u521du59cbu5316H2u6570u636eu5e93u8fdeue5a5
        connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
        
        // u521bu5efau8868
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS \"USER\" (\"ID\" INT AUTO_INCREMENT PRIMARY KEY, \"USERNAME\" VARCHAR(50) NOT NULL, \"EMAIL\" VARCHAR(100), \"AGE\" INT)");
            
            // u6e05u7a7au8868
            stmt.execute("DELETE FROM \"USER\"");
            
            // u63d2u5165u6d4bu8bd5u6570u636e
            stmt.execute("INSERT INTO \"USER\" (\"USERNAME\", \"EMAIL\", \"AGE\") VALUES ('张三', 'zhangsan@example.com', 25)");
            stmt.execute("INSERT INTO \"USER\" (\"USERNAME\", \"EMAIL\", \"AGE\") VALUES ('李四', 'lisi@example.com', 30)");
            stmt.execute("INSERT INTO \"USER\" (\"USERNAME\", \"EMAIL\", \"AGE\") VALUES ('王五', 'wangwu@example.com', 35)");
        }
        
        // u521bu5efaSessionu5e76u83b7u53d6UserMapper
        session = new Session(connection);
        userMapper = session.getMapper(UserMapper.class);
    }
    
    @After
    public void tearDown() throws Exception {
        // u5173u95edu8fdeue5a5
        if (session != null) {
            session.close();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    @Test
    public void testFindByUsername() {
        // u6d4bu8bd5u6839u636eu7528u6237u540du67e5u8be2
        User user = userMapper.findByUsername("张三");
        assertNotNull("用户不应该为空", user);
        assertEquals("张三", user.getUsername());
        assertEquals("zhangsan@example.com", user.getEmail());
        assertEquals(25, user.getAge());
    }
    
    @Test
    public void testFindAll() {
        // u6d4bu8bd5u67e5u8be2u6240u6709u7528u6237
        List<User> users = userMapper.findAll();
        assertNotNull("用户列表不应该为空", users);
        assertEquals("应该有3个用户", 3, users.size());
    }
    
    @Test
    public void testFindByAgeGreaterThan() {
        // u6d4bu8bd5u6839u636eu5e74u9f84u67e5u8be2
        List<User> users = userMapper.findByAgeGreaterThan(25);
        assertNotNull("用户列表不应该为空", users);
        assertEquals("应该有2个用户年龄大于25", 2, users.size());
        
        // u68c0u67e5u7b2cu4e00u4e2au7528u6237
        User user1 = users.get(0);
        assertEquals("李四", user1.getUsername());
        assertEquals(30, user1.getAge());
    }
    
    @Test
    public void testAddUser() {
        // u6d4bu8bd5u6dfbu52a0u7528u6237
        int result = userMapper.addUser("赵六", "zhaoliu@example.com", 40);
        assertEquals("应该成功插入1行", 1, result);
        
        // u9a8cu8bc1u63d2u5165u6210u529f
        User user = userMapper.findByUsername("赵六");
        assertNotNull("新用户不应该为空", user);
        assertEquals("赵六", user.getUsername());
        assertEquals("zhaoliu@example.com", user.getEmail());
        assertEquals(40, user.getAge());
    }
    
    @Test
    public void testUpdateEmail() {
        // u6d4bu8bd5u66f4u65b0u90aeu7bb1
        boolean result = userMapper.updateEmail("zhangsan_new@example.com", "张三");
        assertTrue("更新应该成功", result);
        
        // u9a8cu8bc1u66f4u65b0u6210u529f
        User user = userMapper.findByUsername("张三");
        assertEquals("zhangsan_new@example.com", user.getEmail());
    }
    
    @Test
    public void testDeleteByUsername() {
        // u6d4bu8bd5u5220u9664u7528u6237
        int result = userMapper.deleteByUsername("王五");
        assertEquals("应该成功删除1行", 1, result);
        
        // u9a8cu8bc1u5220u9664u6210u529f
        User user = userMapper.findByUsername("王五");
        assertNull("用户应该已被删除", user);
        
        // u9a8cu8bc1u603bu6570
        int count = userMapper.count();
        assertEquals("应该剩下2个用户", 2, count);
    }
    
    @Test
    public void testCount() {
        // u6d4bu8bd5u8ba1u7b97u7528u6237u6570u91cf
        int count = userMapper.count();
        assertEquals("应该有3个用户", 3, count);
    }
}
