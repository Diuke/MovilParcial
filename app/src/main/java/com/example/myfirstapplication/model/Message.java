package com.example.myfirstapplication.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = User.class,
        parentColumns = "username",
        childColumns = "sender"))
public class Message {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "message_id")
    public int message_id;

    @ColumnInfo(name = "body")
    public String body;

    @ColumnInfo(name = "message_timestamp")
    public String message_timestamp;

    @ColumnInfo(name = "sender")
    public String sender;

}
