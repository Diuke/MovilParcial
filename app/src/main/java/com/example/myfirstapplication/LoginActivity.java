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

public class LoginActivity extends Activity {

    String IP = "172.17.9.205";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);

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

                if(login(userName, password, isConnected)) {
                    Intent intentToBeCalled = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intentToBeCalled);
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

    public boolean login(String username, String password, boolean isConnected) {
        try {
            if(username.equals("")) {
                Toast.makeText(getApplicationContext(), R.string.register_username_error, Toast.LENGTH_LONG).show();
                return false;
            }
            if(password.equals("")) {
                Toast.makeText(getApplicationContext(), R.string.register_password_error, Toast.LENGTH_LONG).show();
                return false;
            }
            if(isConnected) {
                //Remote Request
            }else {
                //Local Request
            }
            return true;
        }catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
