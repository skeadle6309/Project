package com.sethkeadle.project;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Stack;

public class SSH_Server_DataBase implements Runnable {
    private JSch jsch;
    private Session session;
    private PrintWriter toChannel;
    private InputStream inStream;
    private Channel myChannel;
    private OutputStream outStream;
    private OutputStreamWriter outputStreamWriter;
    private String fileName,seeFoodResult;
    private Stack<Pair<String,String>> commandList;


    private static SSH_Server_DataBase instance = null;

    public static SSH_Server_DataBase getInsance(Context context) {
        if (instance == null) {
            instance = new SSH_Server_DataBase(context);
        }
        return instance;
    }


    private SSH_Server_DataBase(Context context) {
        Log.i("MyApp", "into SSH_Server");
        jsch = new JSch();
        commandList = new Stack<Pair<String,String>>();
    }
    public void start() {
        Thread thread = new Thread(instance);
        thread.start();
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
            //set up send command function
            outStream = myChannel.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outStream);
            toChannel = new PrintWriter(outputStreamWriter, true);

            //send a command for testing
            sendCommand();
            //Log.i("MyApp","Command Sent");

            //start reader thread
            inStream = myChannel.getInputStream();
            readerThread();
        }
        catch (Exception e) {
            Log.e("MyApp", e.getMessage());
        }
    }
    private void readerThread() {
        Thread thread = new Thread() {
            public void run() {
                Log.i("MyApp", "Reader Thread running");
                StringBuilder line = new StringBuilder();
                char toAppend;
                final InputStreamReader tout = new InputStreamReader(inStream);
                try {
                    while (true) {
                        try {
                            while (tout.ready()) {
                                toAppend = (char) tout.read();
                                if (toAppend == '\n') {
                                        Log.i("MyApp", line.toString().trim());
                                    line.setLength(0);
                                } else
                                    line.append(toAppend);
                            }
                        }
                        catch (Exception e) {
                            Log.e("MyApp", "************errorrrrrrr reading character**********");
                        }
                    }
                }
                catch (Exception ex) {
                    Log.i("MyApp", ex.getMessage());
                    try {
                        tout.close();
                    }
                    catch (Exception e) {
                        Log.e("MyApp", "error at catch");
                    }
                }
            }
        };
        thread.start();
    }
    private void sendCommand()
    {
        while (true) {
            if (!commandList.isEmpty()) {
                Pair<String,String> command = commandList.pop();
                fileName = command.first;
                seeFoodResult = command.second;
                toChannel.println("python database/db.py 2 /home/guest/images/" + fileName + " " + seeFoodResult);
                Log.i("MyApp", "Database Command sent: /home/guest/images/" + fileName + " " + seeFoodResult);
            }
        }
    }

    public void addCommand(Pair<String,String> cmd) {
        commandList.push(cmd);
    }

}
