package com.example.myfirstapplication.model;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {
    SharedPreferences pref;  // 0 - for private mode
    SharedPreferences.Editor editor;


    public Session(Context context){
        pref = context.getSharedPreferences("MyPref", 0);
        editor = pref.edit();
    }

    public void setUsername(String username){
        editor.putString("username", username); // Storing string
        editor.apply();
    }

    public String getUsername(){
        return pref.getString("username", null);
    }
}
