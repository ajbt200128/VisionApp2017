package com.team1058.vision;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Austin on 4/24/2017.
 */

public class SocketSender{
    private static SocketSender mInstance = null;

    private static String hostName = "10.0.0.12";
    private static int port = 1058;
    private static int timeout = 200;
    private static Socket visionSocket;
    private boolean connected;
    public static SocketSender getInstance(){
        if(mInstance == null)
        {
            mInstance = new SocketSender(hostName,port);
        }
        return mInstance;
    }

    private SocketSender(String newHostName,int newPort){
        hostName = newHostName;
        port = newPort;
        try{
            Socket visionSocket = new Socket();
            visionSocket.connect(new InetSocketAddress(hostName, port), timeout);
            Log.d("Connection: ","True");
            connected = true;
        }catch (IOException e){
            e.printStackTrace();
            connected = false;
        }
    }
    //TODO: make connection faster and more reliable also kill me sockets suck
    protected Boolean send(String... strings){
        try{
            if (!connected || (visionSocket == null) || !testConnection()) {
                visionSocket = new Socket();
                visionSocket.connect(new InetSocketAddress(hostName, port), 200);
                Log.d("Connection: ","Reconnect");
            }
            Log.d("Connection: ","True");
            PrintStream out = new PrintStream(visionSocket.getOutputStream());
            for (String s: strings) {
                out.println(s);
            }
            connected = true;
            return true;
        }catch (IOException e){
            e.printStackTrace();
        }
        Log.d("Connection: ","False");
        connected = false;
        return false;

    }
    public boolean testConnection(){
        try{
            PrintStream out = new PrintStream(visionSocket.getOutputStream());
            out.println("Ping");
            visionSocket.setSoTimeout(timeout);
            BufferedReader in = new BufferedReader(new InputStreamReader(visionSocket.getInputStream()));
            if ((in.readLine()).equals("Pong")){
                in.close();
                return true;
            }


        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
}
