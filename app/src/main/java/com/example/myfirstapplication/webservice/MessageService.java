package com.example.myfirstapplication.webservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myfirstapplication.model.Routes;
import com.example.myfirstapplication.model.UserView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class MessageService extends IntentService {

    public static final int SEND_MESSAGE = 1;
    public static final int SUCCESS_GET_MESSAGES = 2;

    RequestQueue requestQueue;
    Routes routes;

    HashSet<String> messages;
    HashSet<String> usernames;

    public MessageService() {
        super("MessageService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        requestQueue = Volley.newRequestQueue(this);
        routes = new Routes();

        if (intent != null) {
            final String action = intent.getStringExtra("action");
            final ResultReceiver receiver = intent.getParcelableExtra("receiver");
            if ("SEND_MESSAGE".equals(action)) {
                final String message = intent.getStringExtra("message");
                final String username = intent.getStringExtra("username");
                registerMessage(message, username, receiver);
            }else{
                if ("GET_MESSAGES".equals(action)){
                    getMessages(receiver);
                }
            }
        }
    }

    public void registerMessage(final String message, final String username, final ResultReceiver receiver) {
        final String globalMessage = message;
        final String globalUsername = username;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            JSONObject loadMessage = new JSONObject();
            loadMessage.put("body", globalMessage);
            loadMessage.put("message_timestamp", formatter.format(new Date()));
            loadMessage.put("sender", globalUsername);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    routes.routes.get("MESSAGES"), loadMessage, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(("" + response.get("success")).equals("true")) {
                            System.out.println("Escribio mensaje en base de datos");
                            Bundle bundle = new Bundle();
                            bundle.putString("message", globalMessage);
                            bundle.putString("username", globalUsername);
                            bundle.putString("response", "SUCCESS");
                            receiver.send(SEND_MESSAGE, bundle);
                        }else {
                            System.out.println("Something Wrong");
                        }
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            requestQueue.add(request);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getMessages(final ResultReceiver receiver){
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                routes.routes.get("MESSAGES"), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response);
                        try {
                            String jsonString = response.getString("data").replaceAll("\\n", "");
                            JSONArray list = new JSONArray(jsonString);
                            Bundle bundle = new Bundle();
                            usernames = new HashSet<>();
                            messages = new HashSet<>();
                            for(int i = 0; i < list.length(); i++){

                                JSONObject message = list.getJSONObject(i);
                                UserView currentUser;
                                String username = message.getString("sender");
                                String msg = message.getString("body");
                                usernames.add(username);
                                messages.add(msg);

                                //updateMarker(currentUser);
                            }
                            bundle.putSerializable("messages", messages);
                            bundle.putSerializable("usernames", usernames);
                            receiver.send(SUCCESS_GET_MESSAGES, bundle);


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
        requestQueue.add(req);
    }

}
