package com.myorm.entity;

import com.myorm.annotation.Column;
import com.myorm.annotation.Entity;
import com.myorm.annotation.Id;

/**
 * 用户实体类
 */
@Entity(table = "USER")
public class User {
    
    @Id
    @Column(primaryKey = true, autoIncrement = true)
    private Integer id;
    
    @Column(name = "USERNAME")
    private String username;
    
    @Column(name = "EMAIL")
    private String email;
    
    @Column(name = "AGE")
    private Integer age;
    
    public User() {
    }
    
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
