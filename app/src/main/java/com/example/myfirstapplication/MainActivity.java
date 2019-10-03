package com.example.myfirstapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.myfirstapplication.broadcast.BroadcastManager;
import com.example.myfirstapplication.broadcast.BroadcastManagerCallerInterface;
import com.example.myfirstapplication.database.AppDatabase;
import com.example.myfirstapplication.gps.GPSManager;
import com.example.myfirstapplication.gps.GPSManagerCallerInterface;
import com.example.myfirstapplication.model.Routes;
import com.example.myfirstapplication.model.Session;
import com.example.myfirstapplication.model.Track;
import com.example.myfirstapplication.model.UserView;
import com.example.myfirstapplication.network.SocketManagementService;
import com.example.myfirstapplication.webservice.MapService;

import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GPSManagerCallerInterface , BroadcastManagerCallerInterface {

    Activity thisActivity = this;
    GPSManager gpsManager;
    private MapView map;
    private MyLocationNewOverlay mLocationOverlay;
    BroadcastManager broadcastManagerForSocketIO;
    boolean serviceStarted=false;
    AppDatabase appDatabase;
    String sessionUsername;

    ArrayAdapter<String> arrayAdapter;
    HashMap<String, UserView> users;
    ArrayList<String> usernamesForListView;
    HashSet<String> usernames;

    ArrayList<Marker> TrackMarkers;

    String startDate;
    String endDate;

    ListView lv;


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

    public void initializeGPSManager(){
        gpsManager=new GPSManager(this,this);
        gpsManager.initializeLocationManager();
    }

    public void initializeBroadcastManagerForSocketIO(){
        broadcastManagerForSocketIO=new BroadcastManager(this,
                SocketManagementService.
                        SOCKET_SERVICE_CHANNEL,this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startDate = "";
        endDate = "";

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //String user=getIntent().getExtras().getString("user_name");
        //Toast.makeText(this,"Welcome "+user,Toast.LENGTH_SHORT).show();

        ((Button)findViewById(R.id.start_service_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserLocations();
            }
        });

        final View dialogView1 = View.inflate(this, R.layout.date_time_picker, null);
        final View dialogView2 = View.inflate(this, R.layout.date_time_picker2, null);
        final AlertDialog alertDialog1 = new AlertDialog.Builder(this).create();
        final AlertDialog alertDialog2 = new AlertDialog.Builder(this).create();

        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        TextView date1 = findViewById(R.id.start_date);
        date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog1.setView(dialogView1);
                alertDialog1.show();
            }
        });
        TextView date2 = findViewById(R.id.end_date);
        date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog2.setView(dialogView2);
                alertDialog2.show();
            }
        });

        dialogView1.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePicker datePicker = (DatePicker) dialogView1.findViewById(R.id.date_picker);
                TimePicker timePicker = (TimePicker) dialogView1.findViewById(R.id.time_picker);

                Date calendar = new Date(datePicker.getYear()-1900,
                        datePicker.getMonth(),
                        datePicker.getDayOfMonth(),
                        timePicker.getHour(),
                        timePicker.getMinute());
                startDate = formatter.format(calendar);
                TextView date1 = findViewById(R.id.start_date);
                date1.setText(startDate);

                alertDialog1.dismiss();
            }
        });

        dialogView2.findViewById(R.id.date_time_set2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePicker datePicker = (DatePicker) dialogView2.findViewById(R.id.date_picker2);
                TimePicker timePicker = (TimePicker) dialogView2.findViewById(R.id.time_picker2);

                Date calendar = new Date(datePicker.getYear()-1900,
                        datePicker.getMonth(),
                        datePicker.getDayOfMonth(),
                        timePicker.getHour(),
                        timePicker.getMinute());
                endDate = formatter.format(calendar);
                TextView date2 = findViewById(R.id.end_date);
                date2.setText(endDate);

                alertDialog2.dismiss();
            }
        });

        Session session = new Session(getApplicationContext());
        sessionUsername = session.getUsername();
        usernames = new HashSet<>();
        users = new HashMap<>();
        usernamesForListView = new ArrayList<>();
        lv = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, usernamesForListView);
        lv.setAdapter(arrayAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if(startDate.equals("") || endDate.equals("")){
                    Toast.makeText(thisActivity.getApplicationContext(),
                            "No dates selected",Toast.LENGTH_SHORT).show();
                } else {
                    ResponseResultReceiver response = new ResponseResultReceiver(new Handler());
                    String asdf = (String)lv.getItemAtPosition(position);
                    Intent serviceIntent = new Intent(getApplicationContext(), MapService.class);
                    serviceIntent.putExtra("action", "LOCATION_TRACK");
                    serviceIntent.putExtra("username",asdf);
                    serviceIntent.putExtra("startDate", startDate);
                    serviceIntent.putExtra("endDate", endDate);
                    serviceIntent.putExtra("receiver", response);
                    thisActivity.startService(serviceIntent);
                }
            }
        });
        initializeDataBase();
        initializeOSM();
        initializeGPSManager();
        updateUserLocations();
        Routes routes = new Routes();
        Intent intent=new Intent(getApplicationContext(),SocketManagementService.class);
        intent.putExtra("SERVER_HOST", routes.ip);
        intent.putExtra("SERVER_PORT",9090);
        intent.setAction(SocketManagementService.ACTION_CONNECT);
        startService(intent);
        serviceStarted=true;
        initializeBroadcastManagerForSocketIO();
//        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listOfMessages);

        (findViewById(R.id.chat_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentChat = new Intent(getApplicationContext(), chat.class);
                startActivity(intentChat);
            }
        });
    }


    public void updateMarker(UserView user){
        if(user.getUsername().equals(sessionUsername)) return;
        Marker marker = user.getMarker();
        marker.setPosition(new GeoPoint(user.getLat(), user.getLon()));
        if(user.isStatus()){
            marker.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.ic_user_green));
        } else {
            marker.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.ic_user_red));
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            broadcastManagerForSocketIO.sendBroadcast(SocketManagementService.CLIENT_TO_SERVER_MESSAGE,"test");

        } else if (id == R.id.nav_gallery) {


        } else if (id == R.id.nav_slideshow) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    int amount=appDatabase.UserDao().getAll().size();
                }
            });


        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void needPermissions() {
        this.requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                },
                1001);
    }

    @Override
    public void locationHasBeenReceived(final Location location) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.latitude_text_view)).setText(location.getLatitude()+"");
                ((TextView)findViewById(R.id.longitude_text_view)).setText(location.getLongitude()+"");
                if(map!=null)
                setMapCenter(location);
            }
        });

        /*if(serviceStarted)
            if(broadcastManagerForSocketIO!=null){
                broadcastManagerForSocketIO.sendBroadcast(
                        SocketManagementService.CLIENT_TO_SERVER_MESSAGE,
                        location.getLatitude()+" / "+location.getLongitude());
            }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1001){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,
                        "Thanks!!",Toast.LENGTH_SHORT).show();
                gpsManager.startGPSRequesting();
            }

        }
        if(requestCode==1002){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                initializeOSM();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


    }

    public void initializeOSM(){
        try{
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    !=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[]{
                        Manifest.permission.
                                WRITE_EXTERNAL_STORAGE},1002);

                return;
            }
            Context ctx = getApplicationContext();
            Configuration.getInstance().load(ctx,
                    PreferenceManager.
                            getDefaultSharedPreferences(ctx));
            map = (MapView) findViewById(R.id.map);
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setTilesScaledToDpi(true);
            this.mLocationOverlay =
                    new MyLocationNewOverlay(
                            new GpsMyLocationProvider(
                                    this),map);
            this.mLocationOverlay.enableMyLocation();
            this.map.setMultiTouchControls(true);

            map.getOverlays().add(this.mLocationOverlay);
            map.getController().setZoom(15.0);
        }catch (Exception error){
            Toast.makeText(this,error.getMessage(),Toast.LENGTH_SHORT).show();

        }
    }

    public void updateUserLocations(){

        try {
            ResponseResultReceiver response = new ResponseResultReceiver(new Handler());
            Intent serviceIntent = new Intent(getApplicationContext(), MapService.class);
            serviceIntent.putExtra("action", "GET_USERS");
            serviceIntent.putExtra("receiver", response);
            thisActivity.startService(serviceIntent);

        }catch (Exception error){
            Toast.makeText(thisActivity, error.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    public void setMapCenter(Location location){
        IMapController mapController =
                map.getController();
        //mapController.setZoom(5.0);
        GeoPoint startPoint = new GeoPoint(
                location.getLatitude(), location.getLongitude());
        mapController.setCenter(startPoint);
    }

    @Override
    public void MessageReceivedThroughBroadcastManager(final String channel,final String type, final String message) {

        try {
            if (type.equals(SocketManagementService.SERVER_TO_CLIENT_MESSAGE)) {
                String[] data = message.split(" ");
                if (data[1].equals("update@locations")) {
                    System.out.println("Request Actualize");
                    updateUserLocations();
                }else {
                    System.out.println("Server Msg: " + message);
                }
            }
        }catch (Exception ex) {
            Toast.makeText(getApplicationContext(), R.string.something_wrong, Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }

    }

    @Override
    public void ErrorAtBroadcastManager(Exception error) {

    }

    @Override
    protected void onDestroy() {
        if(broadcastManagerForSocketIO!=null){
            broadcastManagerForSocketIO.unRegister();
        }
        super.onDestroy();
    }

    private class ResponseResultReceiver extends ResultReceiver {
        public ResponseResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            try {
                switch (resultCode){
                    case MapService.ERROR:{
                        if(!resultData.getBoolean("connection")){
                            TextView tv = findViewById(R.id.status);
                            tv.setText("STATUS: OFFLINE");
                        }
                        Toast.makeText(thisActivity, resultData.getString("response"), Toast.LENGTH_LONG).show();
                        break;
                    }

                    case MapService.SUCCESS_GET_USERS_LOCATIONS: {
                        HashSet<String> tempUsernames = (HashSet)resultData.getSerializable("usernames");
                        HashMap<String, UserView> tempUsers = (HashMap)resultData.getSerializable("user_info");
                        TextView tv = findViewById(R.id.status);
                        tv.setText("STATUS: ONLINE");
                        for(String username : tempUsernames){
                            UserView user;
                            UserView tempUser = tempUsers.get(username);
                            if(!usernames.contains(username)) {
                                user = tempUsers.get(username);
                                usernames.add(username);
                                users.put(username, user);
                                Marker marker = new Marker(map);
                                marker.setTitle(username + "\n" + user.getLastSeen());
                                map.getOverlays().add(marker);
                                users.get(username).setMarker(marker);
                                usernamesForListView.add(username);

                            } else {
                                user = users.get(username);
                                user.setLat(tempUser.getLat());
                                user.setLon(tempUser.getLon());
                                user.setLastSeen(tempUser.getLastSeen());
                                user.setStatus(tempUser.isStatus());
                            }
                            updateMarker(user);
                        }
                        arrayAdapter.notifyDataSetChanged();
                        break;
                    }

                    case MapService.SUCCESS_GET_TRACK:{
                        if(TrackMarkers != null){
                            for(Marker m : TrackMarkers){
                                map.getOverlays().remove(m);
                            }
                        }

                        TrackMarkers = new ArrayList<>();
                        ArrayList<Track> track = (ArrayList)resultData.getSerializable("track");
                        String speed = resultData.getString("speed");
                        String distance = resultData.getString("distance");
                        TextView spd = findViewById(R.id.velocidad);
                        TextView dst = findViewById(R.id.distancia);
                        spd.setText("Average speed:\n"+speed+"m/s");
                        dst.setText("Total distance:\n"+distance+"m");

                        for(Track t :track){
                            Marker m = new Marker(map);
                            m.setTitle(t.getUsername() + "\n" + t.getLocation_timestamp());
                            m.setPosition(new GeoPoint(t.getLat(), t.getLon()));
                            m.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.ic_track));
                            map.getOverlays().add(m);
                            TrackMarkers.add(m);
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

