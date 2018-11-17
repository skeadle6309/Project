package com.sethkeadle.project;

import android.content.Context;
import android.util.Log;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.OutputStream;

public class SFTP_Server implements Runnable {
    private JSch jsch;
    private Session session;
    private ChannelSftp myChannel;
    private String fileName,fileDestination = "images/";


    private static SFTP_Server instance = null;

    public static SFTP_Server getInsance(Context context) {
        if (instance == null) {
            instance = new SFTP_Server(context);
        }
        return instance;
    }

    private SFTP_Server(Context context) {
        Log.i("MyApp", "into SSH_Server");
        jsch = new JSch();
    }

    public void start(String img) {
        this.fileName = img;
        Thread thread = new Thread(instance);
        thread.start();
        try {
            thread.join();
            Log.i("MyApp","Thread Joined");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session = jsch.getSession("guest","18.225.22.185",22);
            session.setPassword("password");
            session.setConfig(config);
            session.setTimeout(10000);
            Log.i("MyApp","try To connect");
            session.connect();
            myChannel = (ChannelSftp) session.openChannel("sftp");
            myChannel.connect();

            Log.i("MyApp","Connected");

            //send the put command
            Log.i("MyApp", fileName + ": " + fileDestination);
            myChannel.put(fileName,fileDestination);

        }
        catch (Exception e) {
            Log.e("MyApp", e.getMessage());
        }
    }

    private void sendCommand(String img)
    {
        if(session != null && session.isConnected())
        {
            try {
                OutputStream outStream = myChannel.getOutputStream();
                Log.i("MyApp","Command: put /documents/image:277");
            } catch(Exception e){
                Log.e("MyApp",e.getMessage());
            }
        }
    }
}
