package com.example.myfirstapplication.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.myfirstapplication.dao.MessageDao;
import com.example.myfirstapplication.dao.PositionDao;
import com.example.myfirstapplication.dao.UserDao;
import com.example.myfirstapplication.model.Converters;
import com.example.myfirstapplication.model.Message;
import com.example.myfirstapplication.model.Position;
import com.example.myfirstapplication.model.User;

@Database(entities = {User.class, Position.class, Message.class}, version = 5)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao UserDao();
    public abstract PositionDao PositionDao();
    public abstract MessageDao MessageDao();
}