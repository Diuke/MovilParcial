package com.example.myfirstapplication.model;

import java.util.HashMap;

public class Routes {
    public HashMap<String, String> routes = new HashMap<>();
    public String url = "http://192.168.1.8/MovilAPI/api";

    public Routes(){
        routes.put("LOCATION", url+"/locations");
        routes.put("LOCATION_BY_USERNAME", url+"/locations/");
        routes.put("USERS", url+"/users");
        routes.put("USER_BY_USERNAME", url+"/users/");
        routes.put("MESSAGES", url+"/messages");
    }
}
