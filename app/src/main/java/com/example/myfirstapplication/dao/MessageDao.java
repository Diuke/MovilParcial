package com.example.myfirstapplication.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myfirstapplication.model.Message;

import java.util.List;

@Dao
public interface MessageDao {
    @Query("select * from Message")
    List<Message> getAll();

    @Insert
    void insertAll(Message... message);

    @Delete
    void delete(Message message);
}
