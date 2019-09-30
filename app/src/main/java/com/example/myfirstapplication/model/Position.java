package com.example.myfirstapplication.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;


@Entity(foreignKeys = @ForeignKey(entity = User.class,
        parentColumns = "user_id",
        childColumns = "user_id"))
public class Position {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "position_id")
    public int position_id;

    @ColumnInfo(name = "lat")
    public double latitude;

    @ColumnInfo(name = "lng")
    public double longitude;

    @ColumnInfo(name = "user_id")
    public int user_id;

    @ColumnInfo(name = "timestamp")
    public Date timestamp;

}
