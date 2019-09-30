package com.example.myfirstapplication.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myfirstapplication.model.Position;

import java.util.List;

@Dao
public interface PositionDao {
    @Query("select * from Position")
    List<Position> getAll();

    @Query("select * from Position  WHERE position_id = :positionId")
    List<Position> getPositionById(int positionId);

    @Query("select * from Position  WHERE user_id = :user_id")
    List<Position> getPositionsByUserId(int user_id);

    @Insert
    void insertAll(Position... position);

    @Delete
    void delete(Position position);
}
