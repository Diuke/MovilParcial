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

    @Query("select * from Position  WHERE username = :username")
    List<Position> getPositionsByUsername(String username);

    @Insert
    void insertAll(Position... position);

    @Query("DELETE FROM Position")
    void deleteAll();

    @Query("DELETE FROM Position WHERE position_id = :position_id")
    void deleteById(int position_id);
}
