package com.sethkeadle.project;
import android.content.Context;
import android.util.Log;
import android.util.Printer;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.PrivateKey;


//singleton class holding thee active ssh connection to the Amazon EC2 instance
public class Server implements Runnable {
    private JSch jsch;
    private Session session;
    private Context context;
    private final String ls = "ls";

    private static Server instance = null;

    public static Server getInsance(Context context) {
        if (instance == null) {
            instance = new Server(context);
        }
        return instance;
    }


    private Server(Context context) {
        Log.i("MyApp", "into Server");
        this.context = context;
        jsch = new JSch();
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        try {
            session = jsch.getSession("guest","18.225.22.185",22);
            session.setPassword("password");
            session.setConfig(config);
        }
        catch (Exception e)
        {
            Log.e("MyApp",e.getMessage());
        }

    }

    @Override
    public void run() {
        try {
            Log.i("MyApp","try To connect");
            session.connect();
            Log.i("MyApp","Connected");
            try {
                Thread.sleep(1000);
                exec();
            }
            catch (Exception e) {
                Log.i("MyApp",e.getMessage());
            }
        }
        catch (Exception e) {
            Log.i("MyApp",e.getMessage());
        }
    }

    public void exec() {
        try {
            //SSH channel
            Channel channelssh = (Channel) session.openChannel("exec");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channelssh.setOutputStream(baos);
            channelssh.setInputStream(null);

            BufferedReader bReader = new BufferedReader(new InputStreamReader(channelssh.getInputStream()));

            //Send a command
            PrintStream out = new PrintStream(channelssh.getOutputStream());
            out.println("#!/bin/bash/pwd");
            out.println("exit");
            out.flush();

            //read string
            readChannelOutput(channelssh);
            channelssh.disconnect();
        }
        catch (Exception e) {
            Log.e("MyApp",e.getMessage());
        }

    }
    private static void readChannelOutput(Channel channel){

        byte[] buffer = new byte[1024];

        try{
            InputStream in = channel.getInputStream();
            String line = "";
            while (true){
                while (in.available() > 0) {
                    int i = in.read(buffer, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    line = new String(buffer, 0, i);
                    //System.out.println(line);
                    Log.i("myApp","line: " + line);
                }

                if(line.contains("logout")){
                    break;
                }

                if (channel.isClosed()){
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee){}
            }
        }catch(Exception e){
            System.out.println("Error while reading channel output: "+ e);
        }

    }

}
