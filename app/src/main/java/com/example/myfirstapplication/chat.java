package com.example.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myfirstapplication.broadcast.BroadcastManager;
import com.example.myfirstapplication.broadcast.BroadcastManagerCallerInterface;
import com.example.myfirstapplication.gps.GPSManager;
import com.example.myfirstapplication.model.Session;
import com.example.myfirstapplication.network.SocketManagementService;
import com.example.myfirstapplication.webservice.LoginService;
import com.example.myfirstapplication.webservice.MessageService;

import java.util.ArrayList;
import java.util.HashSet;

public class chat extends AppCompatActivity implements BroadcastManagerCallerInterface {

    boolean serviceStarted = true;
    private ArrayList<String> listOfMessages=new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private BroadcastManager broadcastManagerForSocketIO;
    Activity thisActivity;

    Session session;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        thisActivity = this;
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
        updateMessages();
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, listOfMessages);
        ((ListView)findViewById(R.id.messages_list_view)).setAdapter(adapter);
        ((Button)findViewById(R.id.buttonSend)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(((EditText)findViewById(R.id.chat_message)).getText().toString());
            }
        });
    }

    public void getMessages(String message){
        listOfMessages.add(message);
    }

    public void updateMessages(){

        try {
            ResponseResultReceiver response = new ResponseResultReceiver(new Handler());
            Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
            serviceIntent.putExtra("action", "GET_MESSAGES");
            serviceIntent.putExtra("receiver", response);
            thisActivity.startService(serviceIntent);

        }catch (Exception error){
            Toast.makeText(thisActivity, error.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    private void sendMessage(String msg){
        if (serviceStarted){
            if (broadcastManagerForSocketIO != null){
                listOfMessages.add(username + ": " + msg);
                adapter.notifyDataSetChanged();
                broadcastManagerForSocketIO.sendBroadcast(SocketManagementService.CLIENT_TO_SERVER_MESSAGE, username + ": " + msg);
                sendMessageRequest(msg, username);
            }
        }
    }

    private void initializeBroadcastManagerForSocketIO(){
        broadcastManagerForSocketIO=new BroadcastManager(this,
                SocketManagementService.
                        SOCKET_SERVICE_CHANNEL,this);
    }

    public void sendMessageRequest(String message, String username) {
        ResponseResultReceiver response = new ResponseResultReceiver(new Handler());
        Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);
        serviceIntent.putExtra("action", "SEND_MESSAGE");
        serviceIntent.putExtra("message", message);
        serviceIntent.putExtra("username", username);
        serviceIntent.putExtra("receiver", response);
        startService(serviceIntent);
    }

    @Override
    public void MessageReceivedThroughBroadcastManager(final String channel, final String type, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println(message);
                if(!message.contains("update@locations") && !message.contains(username + ": ") && !message.contains("update@messages")){
                    listOfMessages.add(message);
                    adapter.notifyDataSetChanged();
                }

            }
        });
    }

    @Override
    public void ErrorAtBroadcastManager(Exception error) {

    }

    private class ResponseResultReceiver extends ResultReceiver {

        public ResponseResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            try {
                switch (resultCode) {
                    case MessageService.SUCCESS_GET_MESSAGES: {
                        HashSet<String> tempUsernames = (HashSet)resultData.getSerializable("usernames");
                        HashSet<String> tempMessages = (HashSet)resultData.getSerializable("messages");
                        for(String message : tempMessages){

                            getMessages(message);
                        }
                        adapter.notifyDataSetChanged();
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
