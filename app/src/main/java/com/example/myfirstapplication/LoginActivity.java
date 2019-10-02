package com.example.myfirstapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.room.Room;

import com.example.myfirstapplication.database.AppDatabase;
import com.example.myfirstapplication.model.Position;
import com.example.myfirstapplication.model.Routes;
import com.example.myfirstapplication.model.Session;
import com.example.myfirstapplication.model.User;
import com.example.myfirstapplication.webservice.LoginService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LoginActivity extends Activity {

    Routes routes;
    AppDatabase appDatabase;
    Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);
        thisActivity = this;
        routes = new Routes();
        initializeDataBase();
        if(this.checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                this.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE},
                    1003);
        }

        ((Button)findViewById(R.id.login_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userName = ((EditText) findViewById(R.id.login_user_name)).getText() + "";
                final String password = ((EditText) findViewById(R.id.login_password)).getText() + "";

                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                if(fieldVerify(userName, password)) {
                    if(isConnected) {
                        loginRequest(userName, password);
                    }else {
                        //Login Usando Room Database
                        try {

                        } catch (Exception error){

                        }
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    List<User> usersByUsername = appDatabase.UserDao().getUserByUsername(userName);
                                    if(usersByUsername.size() > 0){
                                        String pass = usersByUsername.get(0).pwd;
                                        if(pass.equals(password)){
                                            Intent intentToBeCalled = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intentToBeCalled);
                                            //Toast.makeText(thisActivity, "Login successful", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(thisActivity, "Wrong Password", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(thisActivity, "User doesn't exist", Toast.LENGTH_LONG).show();
                                    }

                                } catch (Exception error){
                                    error.printStackTrace();
                                }

                            }
                        });
                        Toast.makeText(getApplicationContext(), "You're offline, attempting local login", Toast.LENGTH_SHORT).show();
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

    public void initializeDataBase(){
        try{
            appDatabase= Room.
                    databaseBuilder(this,AppDatabase.class,
                            "app-database").
                    fallbackToDestructiveMigration().build();
            /*AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    appDatabase.UserDao().deleteAll();
                    List<User> listUser = appDatabase.UserDao().getAll();
                    appDatabase.PositionDao().deleteAll();
                    List<Position> listPosition = appDatabase.PositionDao().getAll();
                }
            });*/ //Para dropear la base de datos, NO DESCOMENTAR

        }catch (Exception error){
            Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show();
        }
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
        ResponseResultReceiver response = new ResponseResultReceiver(new Handler());
        Intent serviceIntent = new Intent(getApplicationContext(), LoginService.class);
        serviceIntent.putExtra("action", "LOGIN");
        serviceIntent.putExtra("username", username);
        serviceIntent.putExtra("password", password);
        serviceIntent.putExtra("receiver", response);
        startService(serviceIntent);
    }

    private class ResponseResultReceiver extends ResultReceiver {
        public ResponseResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            try {
                switch (resultCode){
                    case LoginService.LOGIN: {
                        if(resultData.getString("response").equals("SUCCESS")){
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                        String username = resultData.getString("username");
                                        String password = resultData.getString("password");
                                        final User user = new User();
                                        user.username = username;
                                        user.first_name = "Default";
                                        user.last_name = "Default";
                                        user.full_name = "Default";
                                        user.email = username + "@uninorte.edu.co";
                                        user.lastLat = "0";
                                        user.lastLon = "0";
                                        user.status = "ONLINE";
                                        user.lastSeen = formatter.format(new Date());
                                        user.pwd = password;
                                        appDatabase.UserDao().insertAll(user);
                                    } catch (Exception error){
                                        System.out.println("Error al agregar en ROOM");
                                    }
                                }
                            });
                            String username = resultData.getString("username");
                            Session session = new Session(getApplicationContext());
                            session.setUsername(username);
                            Intent intentToBeCalled = new Intent(getApplicationContext(), MainActivity.class);
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
