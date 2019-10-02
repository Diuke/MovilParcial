package com.example.myfirstapplication.model;

import org.osmdroid.views.overlay.Marker;

public class UserView {
    private String username;
    private String full_name;
    private double lat;
    private double lon;
    private String lastSeen;
    private boolean status;
    private Marker marker;

    public UserView(String username, String full_name, double lat, double lon, String lastSeen, String status){
        this.username = username;
        this.full_name = full_name;
        this.lastSeen = lastSeen;
        this.lat = lat;
        this.lon = lon;
        this.status = status.equals("online") || status.equals("ONLINE");
        this.marker = null;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
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

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
