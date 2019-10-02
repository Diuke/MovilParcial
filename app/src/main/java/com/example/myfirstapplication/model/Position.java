package com.example.myfirstapplication.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;


@Entity(foreignKeys = @ForeignKey(entity = User.class,
        parentColumns = "username",
        childColumns = "username",
        onDelete = ForeignKey.CASCADE))
public class Position {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "position_id")
    public int position_id;

    @ColumnInfo(name = "lat")
    public String lat;

    @ColumnInfo(name = "lon")
    public String lon;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "location_timestamp")
    public String location_timestamp;


}
