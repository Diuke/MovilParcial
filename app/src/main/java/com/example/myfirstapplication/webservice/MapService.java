package com.example.myfirstapplication.webservice;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myfirstapplication.R;
import com.example.myfirstapplication.model.Routes;
import com.example.myfirstapplication.model.User;
import com.example.myfirstapplication.model.UserView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MapService extends IntentService {
    public final static int ERROR = 1;
    public final static int SUCCESS_SEND_LOCATION = 2;
    public final static int SUCCESS_GET_USERS_LOCATIONS = 3;

    RequestQueue rq;
    Routes routes;
    MapView map;
    Context context;

    HashMap<String, UserView> users;
    HashSet<String> usernames;


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        routes = new Routes();
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String action = intent.getStringExtra("action");
        this.rq = Volley.newRequestQueue(getApplicationContext());
        switch (action){
            case "GET_USERS": {
                getUsersLocations(receiver);
                break;
            }

            case "SEND_LOCATION": {
                double lat = Double.parseDouble(intent.getStringExtra("lat"));
                double lon = Double.parseDouble(intent.getStringExtra("lon"));
                String username = intent.getStringExtra("username");
                sendLocation(lat, lon, username, receiver);
                break;
            }
        }

    }

    public MapService(){
        super("MapService");
    }

    public void getUsersLocations(final ResultReceiver receiver){
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                routes.routes.get("LOCATION"), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response);
                        try {
                            String jsonString = response.getString("data").replaceAll("\\n", "");
                            JSONArray list = new JSONArray(jsonString);
                            Bundle bundle = new Bundle();
                            usernames = new HashSet<>();
                            users = new HashMap<>();
                            for(int i = 0; i < list.length(); i++){

                                JSONObject user = list.getJSONObject(i);
                                UserView currentUser;
                                String username = user.getString("username");
                                usernames.add(username);
                                users.put(username,
                                        new UserView(username, user.getString("full_name"),
                                                user.getDouble("lat"), user.getDouble("lon"), user.getString("lastSeen"),
                                                user.getString("status")));

                                //updateMarker(currentUser);
                            }
                            bundle.putSerializable("usernames", usernames);
                            bundle.putSerializable("user_info", users);
                            receiver.send(SUCCESS_GET_USERS_LOCATIONS, bundle);


                        } catch (Exception error){
                            error.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        rq.add(req);
    }

    public void sendLocation(double lat, double lon, String username, final ResultReceiver receiver){
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            JSONObject body = new JSONObject();
            body.put("lat", lat);
            body.put("lon", lon);
            body.put("location_timestamp", formatter.format(new Date()));
            body.put("username", username);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, routes.routes.get("LOCATION"), body,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println(response);
                            Bundle bundle = new Bundle();
                            bundle.putString("response", "SUCCESS");
                            receiver.send(SUCCESS_SEND_LOCATION, bundle);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error.getMessage());
                            Bundle bundle = new Bundle();
                            bundle.putString("response", error.getMessage());
                            receiver.send(ERROR, bundle);
                        }
                    }) {
                /**
                 * Passing some request headers*
                 */
                @Override
                public Map getHeaders() {
                    HashMap headers = new HashMap();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            rq.add(request);
        } catch (Exception error){
            error.printStackTrace();
        }
    }

    public void cancelAllRequests(String tag) {
        this.rq.cancelAll(tag);
    }
}
