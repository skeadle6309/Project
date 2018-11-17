package com.sethkeadle.project;

import android.content.Context;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class SSH_Server implements Runnable {
    private JSch jsch;
    private Session session;
    private PrintWriter toChannel;
    private InputStream inStream;
    private Channel myChannel;
    private String lastLine, secondToLast, fileName;


    private static SSH_Server instance = null;

    public static SSH_Server getInsance(Context context) {
        if (instance == null) {
            instance = new SSH_Server(context);
        }
        return instance;
    }


    private SSH_Server(Context context) {
        Log.i("MyApp", "into SSH_Server");
        jsch = new JSch();
        lastLine = "";
    }
    public void start(String fileName) {
        this.fileName = fileName;
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
            myChannel = session.openChannel("shell");
            myChannel.connect();
            Log.i("MyApp","Connected");


            //send a command for testing
            sendCommand();
            //Log.i("MyApp","Command Sent");

            //start reader thread
            inStream = myChannel.getInputStream();

            readerThread();
//            Log.i("MyApp","Reader started");
        }
        catch (Exception e) {
            Log.e("MyApp", e.getMessage());
        }
    }
    private void sendCommand()
    {
        if(session != null && session.isConnected())
        {
            try {
                OutputStream outStream = myChannel.getOutputStream();
                toChannel = new PrintWriter(new OutputStreamWriter(outStream), true);
                toChannel.println("python database/db.py 2 /home/guest/images/" + fileName);
                //toChannel.println("python sethtest_main.py samples/" + fileName);
                Log.i("MyApp","Command pwd sent");
            } catch(Exception e){
                Log.e("MyApp",e.getMessage());
            }
        }
    }
    public String getLastLine() {
        return secondToLast;
    }
    private void readerThread()
    {
        Log.i("MyApp","Reader Thread running");
        StringBuilder line = new StringBuilder();
        char toAppend;
        final InputStreamReader tout = new InputStreamReader(inStream);
        try {
            int i = 0;
            while(true){
                try {

                    while (tout.ready()) {
                        toAppend = (char) tout.read();
                        if (toAppend == '\n') {
                            Log.i("MyApp", "line: " + line.toString());
                            secondToLast = lastLine;
                            lastLine = line.toString();
                            line.setLength(0);
                        } else
                            line.append(toAppend);
                    }
                    if (i >= 10) {
                        return;
                    }
                    i++;

                } catch (Exception e) {
                    Log.e("MyApp","************errorrrrrrr reading character**********");
                }
                Thread.sleep(1000);
            }
        }
        catch (Exception ex) {
            Log.i("MyApp",ex.getMessage());
            try{
                tout.close();
            }
            catch(Exception e)
            {
                Log.e("MyApp","error at catch");
            }
        }
    }
}
