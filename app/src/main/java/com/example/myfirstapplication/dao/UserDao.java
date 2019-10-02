package com.example.myfirstapplication.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myfirstapplication.model.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("select * from User")
    List<User> getAll();

    @Query("select * from User  WHERE username = :username")
    List<User> getUserByUsername(String username);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);

}