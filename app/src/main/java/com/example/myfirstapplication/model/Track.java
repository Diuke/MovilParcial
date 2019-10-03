package com.example.myfirstapplication.model;

import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class Track {
    double lat;
    double lon;
    String username;
    String location_timestamp;

    ArrayList<Marker> trackMarkers;

    public Track(double lat, double lon, String username, String location_timestamp) {
        this.lat = lat;
        this.lon = lon;
        this.username = username;
        this.location_timestamp = location_timestamp;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLocation_timestamp() {
        return location_timestamp;
    }

    public void setLocation_timestamp(String location_timestamp) {
        this.location_timestamp = location_timestamp;
    }
}
