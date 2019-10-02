package com.example.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myfirstapplication.model.Routes;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    Routes routes;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        routes = new Routes();
        ((Button) findViewById(R.id.register_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = ((EditText)findViewById(R.id.register_user_name)).getText() + "";
                System.out.println(userName);
                String password = ((EditText)findViewById(R.id.register_password)).getText() + "";
                System.out.println(password);
                String confirmPassword = ((EditText)findViewById(R.id.register_confirm_password)).getText() + "";
                System.out.println(confirmPassword);

                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                if(isConnected) {
                    registerNewUser(userName, password, confirmPassword);
                    //Toast.makeText(getApplicationContext(), "Online", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(getApplicationContext(), R.string.register_network_error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void registerNewUser(String username, String password, String confirmPassword) {
        try {
            if(username.equals("")) {
                Toast.makeText(getApplicationContext(), R.string.register_username_error, Toast.LENGTH_LONG).show();
                return;
            }
            if(password.equals("")) {
                Toast.makeText(getApplicationContext(), R.string.register_password_error, Toast.LENGTH_LONG).show();
                return;
            }
            if(confirmPassword.equals("")) {
                Toast.makeText(getApplicationContext(), R.string.register_confirm_password_error, Toast.LENGTH_LONG).show();
                return;
            }
            if(password.equals(confirmPassword)) {
                //Create User On Web Server
                try {
                    requestQueue = Volley.newRequestQueue(this);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    JSONObject body = new JSONObject();
                    JSONObject user = new JSONObject();
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
                                    Intent intentToBeCalled = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intentToBeCalled);
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
                }catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(getApplicationContext(), R.string.register_error, Toast.LENGTH_LONG).show();
                    return;
                }
            }else{
                Toast.makeText(getApplicationContext(), R.string.register_mismatch_error, Toast.LENGTH_LONG).show();
                return;
            }
        }catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

}
