package com.example.myfirstapplication.webservice;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myfirstapplication.MainActivity;
import com.example.myfirstapplication.R;
import com.example.myfirstapplication.model.Routes;
import com.example.myfirstapplication.model.UserView;

import org.json.JSONObject;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class LoginService extends IntentService {

    public static final int LOGIN = 1;
    public static final int REGISTER = 2;

    RequestQueue requestQueue;
    Routes routes;

    public LoginService() {
        super("LoginService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        requestQueue = Volley.newRequestQueue(this);
        routes = new Routes();

        if (intent != null) {
            final String action = intent.getStringExtra("action");
            final ResultReceiver receiver = intent.getParcelableExtra("receiver");
            if ("LOGIN".equals(action)) {
                final String username = intent.getStringExtra("username");
                final String password = intent.getStringExtra("password");
                login(username, password, receiver);
            } else if ("REGISTER".equals(action)) {
                final String username = intent.getStringExtra("username");
                final String password = intent.getStringExtra("password");
                final String confirmPassword = intent.getStringExtra("confirmPassword");
                register(username, password, confirmPassword, receiver);
            }
        }
    }

    public void register(final String username, final String password, final String confirmPassword, final ResultReceiver receiver){
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            JSONObject body = new JSONObject();
            final JSONObject user = new JSONObject();
            user.put("username", username);
            user.put("first_name", "Default");
            user.put("last_name", "Default");
            user.put("full_name", "Default Default");
            user.put("email", username + "@uninorte.edu.co");
            user.put("lastLat",0);
            user.put("lastLon",0);
            user.put("status", "ONLINE");
            user.put("lastSeen", formatter.format(new Date()));
            body.put("newUser", user);
            body.put("pwd", password);
            body.put("pwdConfirmation", confirmPassword);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    routes.routes.get("USERS"), body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(("" + response.get("success")).equals("true")) {
                            Toast.makeText(getApplicationContext(), R.string.successful_user_creation,
                                    Toast.LENGTH_LONG).show();
                            Bundle bundle = new Bundle();
                            bundle.putString("username", username);
                            bundle.putString("password", password);
                            bundle.putString("confirmPassword", confirmPassword);
                            bundle.putString("response", "SUCCESS");
                            receiver.send(REGISTER, bundle);

                        }else {
                            Toast.makeText(getApplicationContext(), R.string.user_exist_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error.getMessage());
                }
            });
            requestQueue.add(request);
        } catch (Exception error){
            Toast.makeText(getApplicationContext(), R.string.register_error, Toast.LENGTH_LONG).show();
            error.printStackTrace();
        }

    }

    public void login(final String username, String password, final ResultReceiver receiver){
        try {
            JSONObject body = new JSONObject();
            body.put("data", password);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    routes.routes.get("USER_BY_USERNAME") + username, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        System.out.println(response);
                        Bundle bundle = new Bundle();
                        if (("" + response.get("success")).equals("true")) {
                            Toast.makeText(getApplicationContext(), R.string.successful_login,
                                    Toast.LENGTH_LONG).show();
                            bundle.putString("response", "SUCCESS");
                            bundle.putString("username", username);
                            receiver.send(LOGIN, bundle);

                        } else {
                            bundle.putString("response", "ERROR");
                            Toast.makeText(getApplicationContext(), R.string.login_password_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(), R.string.something_wrong, Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error.getMessage());
                }
            });
            requestQueue.add(request);
        } catch (Exception error){
            error.printStackTrace();
        }
    }



}
