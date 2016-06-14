package com.shpp.sv.wifichat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.primitives.Bytes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "svcom";
    public static final String BROADCAST_ACTION = "com.shpp.sv.wifichat";
    public static final String MESSAGE = "message";
    public static final String EXTRA_IP = "ip";
    public static final String EXTRA_TIME = "time";
    public static final String EXTRA_USER_NAME = "name";
    public static final String EXTRA_MESSAGE_COLOR = "color";
    public static final int PORT = 49152;
    private BroadcastReceiver receiver;

    private String name = "SV";
    private int messageColor;
    public static final int DEFAULT_COLOR = 0xABCDEF;
    public static final byte ID = 26;
    public static final byte TYPE_EMPTY = 42;
    public static final byte TYPE_FULL = 43;

    private byte[] empty_message;

    private ArrayList<Message> messageList;
    private ListView lstvMessages;
    private MessagesAdapter messagesAdapter;
    private EditText edtMessage;
    private Toast toast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtMessage = (EditText)findViewById(R.id.edtMessage);
        messageList = new ArrayList<>();
        lstvMessages = (ListView)findViewById(R.id.lstvMessages);
        messagesAdapter = new MessagesAdapter(this, messageList);
        lstvMessages.setAdapter(messagesAdapter);

        createReceiver();

        Intent intent = getIntent();
        name = intent.getStringExtra(EXTRA_USER_NAME);
        messageColor = intent.getIntExtra(EXTRA_MESSAGE_COLOR, DEFAULT_COLOR);

        createEmptyMessage();
        launchService(empty_message);
    }

    private byte[] sendChatMessage(String mess) {
        byte[] header = new byte[6];
        header[0] = (byte) ID;
        header[1] = (byte) TYPE_FULL;
        header[2] = (byte)Color.red(messageColor);
        header[3] = (byte)Color.green(messageColor);
        header[4] = (byte)Color.blue(messageColor);
        header[5] = (byte) name.length();
        byte[] name = this.name.getBytes();

        byte[]fullMessage = Bytes.concat(header, name, mess.getBytes());

        Intent intent = new Intent(MainActivity.this, MessageManagerService.class);
        intent.putExtra(MESSAGE, fullMessage);
        startService(intent);
        return fullMessage;
    }

    private void createEmptyMessage() {

        byte[] header = new byte[5];
        header[0] = (byte) ID;
        header[1] = (byte) TYPE_EMPTY;
        header[2] = (byte)Color.red(messageColor);
        header[3] = (byte)Color.green(messageColor);
        header[4] = (byte)Color.blue(messageColor);
        empty_message = Bytes.concat(header, name.getBytes());
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    private void createReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                byte[] data = intent.getByteArrayExtra(MESSAGE);
                String ip = intent.getStringExtra(EXTRA_IP);
                long time = intent.getLongExtra(EXTRA_TIME, 0);
                if (data[1] == TYPE_EMPTY){
                    updateUsersList(data, time, ip);
                } else if(data[1] == TYPE_FULL){
                    showMessage(data, time, false);
                }
            }
        };
    }


    private void updateUsersList(byte[] data, long time, String ip){

    }


    private void showMessage(byte[] data, long time, boolean own){
        int color = Color.rgb(data[2] & 0xFF, data[3] & 0xFF, data[4] & 0xFF);
        int nameLength = (int)data[5];
        String name = new String(Arrays.copyOfRange(data, 6, 6 + nameLength));
        String msgText = new String(Arrays.copyOfRange(data, 6 + nameLength, data.length));

        User user = new User(name, color);
        Message message = new Message(user, msgText, time, own);

        messageList.add(message);
        messagesAdapter.notifyDataSetChanged();
        lstvMessages.setSelection(messagesAdapter.getCount() - 1);
    }



    private void launchService(byte[] empty_message) {
        Intent intent = new Intent(MainActivity.this, MessageManagerService.class);
        intent.putExtra(MESSAGE, empty_message);
        startService(intent);
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(MainActivity.this, MessageManagerService.class);
        stopService(intent);
        unregisterReceiver(receiver);
    }

    public void onClickBtnSend(View view) {
        String text = edtMessage.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            long time = Calendar.getInstance().getTimeInMillis();
            byte[] message = sendChatMessage(text);
            showMessage(message, time, true);
            edtMessage.setText("");
        }
    }

    @Override
    public void onBackPressed() {

        if (toast != null && toast.getView().getWindowToken() != null){
            finishAffinity();
        } else {
            toast = Toast.makeText(this, getResources().getString(R.string.msgPressBack),
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
