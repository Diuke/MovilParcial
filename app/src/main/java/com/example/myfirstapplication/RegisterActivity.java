package com.example.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myfirstapplication.database.AppDatabase;
import com.example.myfirstapplication.model.Routes;
import com.example.myfirstapplication.model.User;
import com.example.myfirstapplication.webservice.LoginService;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    Routes routes;
    AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        routes = new Routes();
        initializeDataBase();
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

    public void initializeDataBase(){
        try{
            appDatabase= Room.
                    databaseBuilder(this,AppDatabase.class,
                            "app-database").
                    fallbackToDestructiveMigration().build();
        }catch (Exception error){
            Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show();
        }
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
                ResponseResultReceiver response = new ResponseResultReceiver(new Handler());
                Intent serviceIntent = new Intent(getApplicationContext(), LoginService.class);
                serviceIntent.putExtra("action", "REGISTER");
                serviceIntent.putExtra("username", username);
                serviceIntent.putExtra("password", password);
                serviceIntent.putExtra("confirmPassword", confirmPassword);
                serviceIntent.putExtra("receiver", response);
                startService(serviceIntent);

            }else{
                Toast.makeText(getApplicationContext(), R.string.register_mismatch_error, Toast.LENGTH_LONG).show();
                return;
            }
        }catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    private class ResponseResultReceiver extends ResultReceiver {
        public ResponseResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            try {
                switch (resultCode){
                    case LoginService.REGISTER: {
                        if(resultData.getString("response").equals("SUCCESS")){
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                        String username = resultData.getString("username");
                                        String password = resultData.getString("password");
                                        String confirmPassword = resultData.getString("confirmPassword");
                                        final User user = new User();
                                        user.username = username;
                                        user.first_name = "Default";
                                        user.last_name = "Default";
                                        user.full_name = "Default";
                                        user.email = username + "@uninorte.edu.co";
                                        user.lastLat = "0";
                                        user.lastLon = "0";
                                        user.status = "OFFLINE";
                                        user.lastSeen = formatter.format(new Date());
                                        user.pwd = password;
                                        appDatabase.UserDao().insertAll(user);
                                    } catch (Exception error){
                                        System.out.println("Error al agregar en ROOM");
                                    }
                                }
                            });
                            Intent intentToBeCalled = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intentToBeCalled);
                        }
                        break;
                    }
                }
                super.onReceiveResult(resultCode, resultData);
            } catch (Exception error){
                error.printStackTrace();
            }

        }
    }

}
