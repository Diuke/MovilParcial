package com.example.myfirstapplication.gps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.widget.Toast;

import androidx.room.Room;

import com.example.myfirstapplication.database.AppDatabase;
import com.example.myfirstapplication.model.Position;
import com.example.myfirstapplication.model.Session;
import com.example.myfirstapplication.model.User;
import com.example.myfirstapplication.webservice.MapService;

import java.util.Date;
import java.util.List;


public class GPSManager implements LocationListener {
    private Activity activity;
    private GPSManagerCallerInterface caller;

    LocationManager locationManager;

    AppDatabase appDatabase;
    String username;
    Session session;

    Location actualLocation;
    Location lastTrack;

    ResponseResultReceiver response;

    public GPSManager(Activity activity,
                      GPSManagerCallerInterface caller) {
        this.activity = activity;
        this.caller = caller;
        this.actualLocation = null;
        this.lastTrack = null;
        Session session = new Session(activity.getApplicationContext());
        username = session.getUsername();
    }

    public void initializeLocationManager() {
        try {
            locationManager = (LocationManager)
                    this.activity.getSystemService(Context.LOCATION_SERVICE);
            if (activity.checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED ||
                    activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                caller.needPermissions();
                return;
            }else{
                initializeDataBase();
                startGPSRequesting();
            }

        }catch (Exception error){
            error.printStackTrace();
        }
    }

    public void initializeDataBase(){
        try{
            appDatabase= Room.
                    databaseBuilder(this.activity, AppDatabase.class,
                            "app-database").
                    fallbackToDestructiveMigration().build();
        }catch (Exception error){
            Toast.makeText(this.activity, error.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingPermission")
    public void startGPSRequesting(){
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0, 10, this, Looper.getMainLooper());
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0, 10, this, Looper.getMainLooper());
            actualLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            lastTrack = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            this.saveLocation(actualLocation);

        }catch (Exception error){
            error.printStackTrace();
            //caller.gpsErrorHasBeenThrown(error);
        }
    }

    public void saveLocation(Location location){
        final Position position = new Position();
        position.lat = ""+location.getLatitude();
        position.lon = ""+location.getLongitude();
        position.location_timestamp = new Date().toString();
        position.username = this.username;
        try {
            response = new ResponseResultReceiver(new Handler());
            Intent serviceIntent = new Intent(activity.getApplicationContext(), MapService.class);
            serviceIntent.putExtra("action", "SEND_LOCATION");
            serviceIntent.putExtra("lat", position.lat);
            serviceIntent.putExtra("lon", position.lon);
            serviceIntent.putExtra("username", username);
            serviceIntent.putExtra("receiver", response);
            activity.startService(serviceIntent);

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<User> activeUsers = appDatabase.UserDao().getAll();
                        List<Position> list = appDatabase.PositionDao().getPositionsByUsername(username);
                        appDatabase.PositionDao().insertAll(position);

                         System.out.println(list);
                    } catch (Exception error){
                        error.printStackTrace();
                    }

                }
            });

        }catch (Exception error){
            Toast.makeText(this.activity, error.getMessage(),Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        this.actualLocation = location;
        float change = this.lastTrack.distanceTo((location));
        if(change >= 50.0){
            this.lastTrack = location;
            Toast.makeText(this.activity, "Location Changed", Toast.LENGTH_LONG).show();
            this.saveLocation(this.lastTrack);
        }
        caller.locationHasBeenReceived(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    private class ResponseResultReceiver extends ResultReceiver {
        public ResponseResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode){
                case MapService.ERROR:{
                    Toast.makeText(activity, resultData.getString("response"), Toast.LENGTH_LONG).show();
                    break;
                }

                case MapService.SUCCESS_SEND_LOCATION: {
                    Toast.makeText(activity, "Location Updated", Toast.LENGTH_LONG).show();
                    break;
                }

                case MapService.SUCCESS_GET_USERS_LOCATIONS: {
                    break;
                }
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

}


