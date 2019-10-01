package com.example.myfirstapplication.gps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.room.Room;

import com.example.myfirstapplication.database.AppDatabase;
import com.example.myfirstapplication.model.Position;
import com.example.myfirstapplication.webservice.MapService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class GPSManager implements LocationListener {
    private Activity activity;
    private GPSManagerCallerInterface caller;

    LocationManager locationManager;

    AppDatabase appDatabase;
    String username = "demarchenac";

    Location actualLocation;
    Location lastTrack;

    MapService mapService;

    public GPSManager(Activity activity,
                      GPSManagerCallerInterface caller, MapService mapService) {
        this.activity = activity;
        this.caller = caller;
        this.actualLocation = null;
        this.lastTrack = null;
        this.mapService = mapService;
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
            caller.gpsErrorHasBeenThrown(error);
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
            caller.gpsErrorHasBeenThrown(error);
        }
    }

    public void saveLocation(Location location){
        final Position position = new Position();
        position.lat = ""+location.getLatitude();
        position.lon = ""+location.getLongitude();
        position.location_timestamp = new Date().toString();
        position.username = this.username;
        try {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        appDatabase.PositionDao().insertAll(position);
                        mapService.sendLocation(Double.parseDouble(position.lat), Double.parseDouble(position.lon), position.username);
                        List<Position> list = appDatabase.PositionDao().getAll();
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
}
