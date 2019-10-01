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

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ((Button) findViewById(R.id.register_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = ((EditText)findViewById(R.id.register_user_name)).getText() + "";
                String password = ((EditText)findViewById(R.id.register_password)).getText() + "";
                String confirmPassword = ((EditText)findViewById(R.id.register_confirm_password)).getText() + "";

                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                if(isConnected) {
                    if (registerNewUser(userName, password, confirmPassword)) {
                        Intent intentToBeCalled = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intentToBeCalled);
                        //Toast.makeText(getApplicationContext(), "Online", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), R.string.register_network_error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public boolean registerNewUser(String username, String password, String confirmPassword) {
        try {
            if(username.equals("")) {
                Toast.makeText(getApplicationContext(), R.string.register_username_error, Toast.LENGTH_LONG).show();
                return false;
            }
            if(password.equals("")) {
                Toast.makeText(getApplicationContext(), R.string.register_password_error, Toast.LENGTH_LONG).show();
                return false;
            }
            if(confirmPassword.equals("")) {
                Toast.makeText(getApplicationContext(), R.string.register_confirm_password_error, Toast.LENGTH_LONG).show();
                return false;
            }
            if(password.equals(confirmPassword)) {
                //Create User On Web Server
                return true;
            }else{
                Toast.makeText(getApplicationContext(), R.string.register_mismatch_error, Toast.LENGTH_LONG).show();
                return false;
            }
        }catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
