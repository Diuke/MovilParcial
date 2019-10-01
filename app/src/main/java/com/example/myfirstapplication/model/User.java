package com.example.myfirstapplication.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey
    @ColumnInfo(name = "username")
    @NonNull public String username;

    @ColumnInfo(name = "first_name")
    public String first_name;

    @ColumnInfo(name = "last_name")
    public String last_name;

    @ColumnInfo(name = "full_name")
    public String full_name;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "lastLat")
    public String lastLat;

    @ColumnInfo(name = "lastLon")
    public String lastLon;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "lastSeen")
    public String lastSeen;

    @ColumnInfo(name = "pwd")
    public String pwd;

}