package com.example.myfirstapplication.webservice;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class WebServiceManager {

    public static void callWebServiceOperation(final WebServiceManagerCallerInterface caller,
                                               final String url, final String userState,
                                               final Context context, final int method) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestQueue queue = Volley.newRequestQueue(context);
                    if(userState.equals("ALL") || userState.equals("INTERVAL") ) {
                        JsonArrayRequest jar = new JsonArrayRequest(method, url, null, new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                caller.webServiceArrayReceived(userState,response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (userState.equals("ALL")) {
                                    caller.webServiceMessageReceived(userState,error.getMessage());
                                }else{
                                    caller.webServiceMessageReceived(userState,error.getMessage());
                                }
                            }
                        });
                        queue.add(jar);
                    }else {
                        JsonObjectRequest js = new JsonObjectRequest(method, url, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                JSONObject rs = response;
                                switch (userState) {
                                    case "LOGIN":
                                        caller.webServiceMessageReceived(userState,
                                                rs.optString("password"));
                                        break;
                                    case "SING IN":
                                        caller.webServiceMessageReceived(userState,
                                                "User registered");
                                        break;
                                    case "SET":
                                        caller.webServiceMessageReceived(userState,
                                                rs.optString("idUsuario") + "_" +
                                                        rs.optString("latitud") + "_" +
                                                        rs.optString("longitud"));
                                        break;
                                }
                            }
                        },new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                switch (userState){
                                    case "LOGIN":
                                        caller.webServiceMessageReceived("ERROR " + userState,
                                                "User not found. Please verify your password or your connection");
                                        break;

                                    case "SING IN":
                                        caller.webServiceMessageReceived("ERROR " + userState,
                                                "Error while registering user, verify your connection");
                                        break;
                                    case "SET":
                                        caller.webServiceMessageReceived("ERROR " + userState,
                                                error.getMessage());
                                        break;
                                    case "UPDATE":
                                        caller.webServiceMessageReceived("ERROR " + userState,
                                                error.getMessage());
                                        break;
                                }
                            }
                        });
                        queue.add(js);
                    }
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

}
