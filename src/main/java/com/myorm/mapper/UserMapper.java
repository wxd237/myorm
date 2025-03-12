package com.myorm.mapper;

import com.myorm.annotation.*;
import com.myorm.entity.User;

import java.util.List;

/**
 * Useru5b9eu4f53u7684Mapperu63a5u53e3
 */
@Mapper(entity = User.class)
public interface UserMapper {
    
    /**
     * u6839u636eu7528u6237u540du67e5u8be2u7528u6237
     * @param username u7528u6237u540d
     * @return u7528u6237u5bf9u8c61
     */
    @Select("SELECT * FROM \"USER\" WHERE \"USERNAME\" = ?")
    User findByUsername(@Param("username") String username);
    
    /**
     * u67e5u8be2u6240u6709u7528u6237
     * @return u7528u6237u5217u8868
     */
    @Select("SELECT * FROM \"USER\"")
    List<User> findAll();
    
    /**
     * u6839u636eu5e74u9f84u67e5u8be2u7528u6237
     * @param age u5e74u9f84
     * @return u7528u6237u5217u8868
     */
    @Select("SELECT * FROM \"USER\" WHERE \"AGE\" > ?")
    List<User> findByAgeGreaterThan(@Param("age") int age);
    
    /**
     * u6dfbu52a0u7528u6237
     * @param username u7528u6237u540d
     * @param email u90aeu7bb1
     * @param age u5e74u9f84
     * @return u53d7u5f71u54cdu7684u884cu6570
     */
    @Insert("INSERT INTO \"USER\" (\"USERNAME\", \"EMAIL\", \"AGE\") VALUES (?, ?, ?)")
    int addUser(@Param("username") String username, @Param("email") String email, @Param("age") int age);
    
    /**
     * u66f4u65b0u7528u6237u90aeu7bb1
     * @param username u7528u6237u540d
     * @param email u65b0u90aeu7bb1
     * @return u662fu5426u66f4u65b0u6210u529f
     */
    @Update("UPDATE \"USER\" SET \"EMAIL\" = ? WHERE \"USERNAME\" = ?")
    boolean updateEmail(@Param("email") String email, @Param("username") String username);
    
    /**
     * u5220u9664u7528u6237
     * @param username u7528u6237u540d
     * @return u53d7u5f71u54cdu7684u884cu6570
     */
    @Delete("DELETE FROM \"USER\" WHERE \"USERNAME\" = ?")
    int deleteByUsername(@Param("username") String username);
    
    /**
     * u8ba1u7b97u7528u6237u6570u91cf
     * @return u7528u6237u6570u91cf
     */
    @Select("SELECT COUNT(*) FROM \"USER\"")
    int count();
}
