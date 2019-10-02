package com.example.myfirstapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myfirstapplication.model.Routes;

import org.json.JSONObject;

public class LoginActivity extends Activity {

    Routes routes;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);

        routes = new Routes();

        if(this.checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                this.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE},
                    1003);
        }

        ((Button)findViewById(R.id.login_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = ((EditText) findViewById(R.id.login_user_name)).getText() + "";
                String password = ((EditText) findViewById(R.id.login_password)).getText() + "";

                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                if(fieldVerify(userName, password)) {
                    if(isConnected) {
                        loginRequest(userName, password);
                    }else {
                        //Login Usando Room Database
                        Toast.makeText(getApplicationContext(), "Offline", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        ((Button)findViewById(R.id.login_register_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToBeCalled = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intentToBeCalled);
            }
        });
    }

    public boolean fieldVerify(String username, String password) {
        try {
            if(username.equals("")) {
                Toast.makeText(getApplicationContext(), R.string.register_username_error, Toast.LENGTH_LONG).show();
                return false;
            }
            if(password.equals("")) {
                Toast.makeText(getApplicationContext(), R.string.register_password_error, Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void loginRequest(String username, String password) {
        try {
            requestQueue = Volley.newRequestQueue(this);
            JSONObject body = new JSONObject();
            body.put("data", password);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    routes.routes.get("USER_BY_USERNAME") + username, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        System.out.println(response);
                        if (("" + response.get("success")).equals("true")) {
                            Toast.makeText(getApplicationContext(), R.string.successful_login,
                                    Toast.LENGTH_LONG).show();
                            Intent intentToBeCalled = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intentToBeCalled);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.login_password_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }catch (Exception ex) {
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
        }catch (Exception ex) {
            Toast.makeText(getApplicationContext(), R.string.something_wrong, Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

}
