package com.example.myfirstapplication.webservice;

import org.json.JSONArray;

public interface WebServiceManagerCallerInterface {

    void webServiceMessageReceived(String userState, String message);
    void webServiceArrayReceived(String userState, JSONArray message);

}
