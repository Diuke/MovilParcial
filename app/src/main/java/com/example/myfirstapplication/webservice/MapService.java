package com.example.myfirstapplication.webservice;

import android.content.Context;
import android.os.AsyncTask;

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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MapService {
    RequestQueue rq;
    RequestQueue realtimeRq;
    Routes routes;
    MapView map;
    Context context;
    HashMap<String, UserView> users;
    HashSet<String> usernames;

    public MapService(Context context, MapView map){
        this.rq = Volley.newRequestQueue(context);
        this.realtimeRq = Volley.newRequestQueue(context);
        this.routes = new Routes();
        this.map = map;
        this.context = context;
        users = new HashMap<>();
        usernames = new HashSet<>();
    }

    public void updateMarker(UserView user){
        Marker marker = user.getMarker();
        marker.setPosition(new GeoPoint(user.getLat(), user.getLon()));
        if(user.isStatus()){
            marker.setIcon(context.getResources().getDrawable(R.drawable.ic_menu_mylocation));
        } else {
            marker.setIcon(context.getResources().getDrawable(R.drawable.ic_menu_offline));
        }

    }

    public void getUsersLocations(){
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                routes.routes.get("LOCATION"), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response);
                        try {
                            String jsonString = response.getString("data").replaceAll("\\n", "");
                            JSONArray list = new JSONArray(jsonString);
                            for(int i = 0; i < list.length(); i++){

                                JSONObject user = list.getJSONObject(i);
                                UserView currentUser;
                                String username = user.getString("username");
                                if(!usernames.contains(username)){
                                    usernames.add(username);
                                    users.put(username,
                                            new UserView(username, user.getString("full_name"),
                                                    user.getDouble("lat"), user.getDouble("lon"), user.getString("lastSeen"),
                                                    user.getString("status")));
                                    Marker marker = new Marker(map);
                                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                    marker.setIcon(context.getResources().getDrawable(R.drawable.ic_menu_mylocation));
                                    marker.setTitle(username);
                                    map.getOverlays().add(marker);
                                    users.get(username).setMarker(marker);
                                }
                                currentUser = users.get(username);
                                updateMarker(currentUser);
                            }

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
        realtimeRq.add(req);
    }

    public void sendLocation(double lat, double lon, String username){
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
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error.getMessage());
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
