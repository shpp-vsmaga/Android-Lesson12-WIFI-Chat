package com.shpp.sv.wifichat;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import android.os.Handler;

public class MessageManagerService extends Service {
    private static BroadcastSenderThread broadcastSenderThread;
    private volatile String broadcastIP;
    private  volatile String myIP;
    private Handler handler;
    private static final long PING_MESSAGES_INTERVAL = 2000;

    public MessageManagerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                myIP = getOwnIP();
                broadcastIP = getBroadcastIP();
            }
        }).start();



    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        byte[] message = new byte[20];
        if (intent != null) {
            message = intent.getByteArrayExtra(MainActivity.MESSAGE);
        }

        if (message[1] == MainActivity.TYPE_EMPTY){
            startBroadcastSenderListener(message);
        } else if (message[1] == MainActivity.TYPE_FULL){
            sendChatMessage(message);
        }
        return START_STICKY;
    }

    private void sendChatMessage(final byte[] message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendUdpMessage(broadcastIP, message);
            }
        }).start();
    }

    private void startBroadcastListener(){


        BroadcastListenerThread broadcastListenerThread = new BroadcastListenerThread();
        broadcastListenerThread.start();
    }

    private class BroadcastListenerThread extends Thread{
        @Override
        public void run() {
            while (true) {
                listenBroadcastMessages();
            }
        }
    }

    private void listenBroadcastMessages(){
        try {
            InetAddress address = InetAddress.getByName(broadcastIP);

            DatagramSocket socket = new DatagramSocket(MainActivity.PORT, address);
            socket.setBroadcast(true);

            byte[] message = new byte[250];
            DatagramPacket packet = new DatagramPacket(message, message.length);


            socket.receive(packet);
            socket.close();


            byte[] clearData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
            long time = Calendar.getInstance().getTimeInMillis();
            String hostIP = packet.getAddress().getHostAddress();

            if (!myIP.equals(hostIP)) {
                sendMessageToMainActivity(clearData, hostIP, time);
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendMessageToMainActivity(byte[] data, String senderIp, long time){
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        intent.putExtra(MainActivity.MESSAGE, data);
        intent.putExtra(MainActivity.EXTRA_IP, senderIp);
        intent.putExtra(MainActivity.EXTRA_TIME, time);
        sendBroadcast(intent);
    }

    private void startBroadcastSenderListener(final byte[] message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                myIP = getOwnIP();
                broadcastIP = getBroadcastIP();
                handler.post(new SenderListenerRunnable(message));

            }
        }).start();

        broadcastSenderThread = new BroadcastSenderThread(message);
        broadcastSenderThread.start();
    }

    private class SenderListenerRunnable implements Runnable{
        private byte[] message;

        public SenderListenerRunnable(byte[] message){
            this.message = message;
        }
        @Override
        public void run() {
            broadcastSenderThread = new BroadcastSenderThread(message);
            broadcastSenderThread.start();

            startBroadcastListener();
        }
    }

    private  class BroadcastSenderThread extends Thread{
        private byte[] message;
        BroadcastSenderThread(byte[] message){
            this.message = message;
        }
        @Override
        public void run() {

                sendUdpMessage(broadcastIP, message);
                try {
                    Thread.sleep(PING_MESSAGES_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

        }
    }


    private void sendUdpMessage(String broadcastIP, byte[] message){
        try {
            DatagramSocket  socket = new DatagramSocket();
            socket.setBroadcast(true);
            InetAddress brAddress = InetAddress.getByName(broadcastIP);

            DatagramPacket packet = new DatagramPacket(message, message.length,
                    brAddress, MainActivity.PORT);

            socket.send(packet);
            socket.close();

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (broadcastSenderThread != null) {
            broadcastSenderThread.interrupt();
        }
    }


    public String getOwnIP() {
        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();

        return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
    }

    private String getBroadcastIP(){
        String broadcastIP = "";
        try {
            InetAddress address = InetAddress.getByName(myIP);
            NetworkInterface iface = NetworkInterface.getByInetAddress(address);
            InterfaceAddress ifaceAddr = iface.getInterfaceAddresses().get(1);
            broadcastIP = ifaceAddr.getBroadcast().getCanonicalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return broadcastIP;
    }
}
