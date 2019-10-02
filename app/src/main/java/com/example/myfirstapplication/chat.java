package com.example.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myfirstapplication.broadcast.BroadcastManager;
import com.example.myfirstapplication.broadcast.BroadcastManagerCallerInterface;
import com.example.myfirstapplication.model.Session;
import com.example.myfirstapplication.network.SocketManagementService;

import java.util.ArrayList;

public class chat extends AppCompatActivity implements BroadcastManagerCallerInterface {

    boolean serviceStarted = true;
    private ArrayList<String> listOfMessages=new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private BroadcastManager broadcastManagerForSocketIO;

    Session session;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        session = new Session(getApplicationContext());
        username = session.getUsername();

//        try {
//            Intent intent = new Intent(
//                    getApplicationContext(), SocketManagementService.class);
//            intent.putExtra("SERVER_HOST", "172.0.0.1");
//            intent.putExtra("SERVER_PORT", 8080);
//            intent.setAction(SocketManagementService.ACTION_CONNECT);
//            startService(intent);
//            serviceStarted = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(getApplicationContext(),"Error connecting to chat",Toast.LENGTH_SHORT).show();
//        }
        initializeBroadcastManagerForSocketIO();
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, listOfMessages);
        ((ListView)findViewById(R.id.messages_list_view)).setAdapter(adapter);
        ((Button)findViewById(R.id.buttonSend)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(((EditText)findViewById(R.id.chat_message)).getText().toString());
            }
        });
    }

    private void sendMessage(String msg){
        if (serviceStarted){
            if (broadcastManagerForSocketIO != null){
                listOfMessages.add(username + ": " + msg);
                adapter.notifyDataSetChanged();
                broadcastManagerForSocketIO.sendBroadcast(SocketManagementService.CLIENT_TO_SERVER_MESSAGE, username + ": " + msg);
            }
        }
    }

    private void initializeBroadcastManagerForSocketIO(){
        broadcastManagerForSocketIO=new BroadcastManager(this,
                SocketManagementService.
                        SOCKET_SERVICE_CHANNEL,this);
    }

    @Override
    public void MessageReceivedThroughBroadcastManager(final String channel, final String type, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println(message);
                if(!message.contains("update@locations") && !message.contains(username + ": ")){
                    listOfMessages.add(message);
                    adapter.notifyDataSetChanged();
                }

            }
        });
    }

    @Override
    public void ErrorAtBroadcastManager(Exception error) {

    }
}