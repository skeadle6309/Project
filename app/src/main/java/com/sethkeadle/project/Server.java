package com.sethkeadle.project;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


//singleton class holding thee active ssh connection to the Amazon EC2 instance
public class Server extends AsyncTask<String, Void, String> implements Runnable{
    private JSch jsch;
    private Session session;
    private Context context;
    private PrintWriter toChannel;
    private final String ls = "ls";
    private  InputStream inStream;
    private Channel myChannel;
    private InputStreamReader tout;

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
        //SSHClient ssh = new SSHClient();
    }
    public void start() {
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
    public void sendCommand()
    {
        if(session != null && session.isConnected())
        {
            try {
                OutputStream outStream = myChannel.getOutputStream();
                toChannel = new PrintWriter(new OutputStreamWriter(outStream), true);
                toChannel.println("pwd");
                Log.i("MyApp","Command pwd sent");
            } catch(Exception e){
                Log.e("MyApp",e.getMessage());
            }
        }
    }
    void readerThread()
    {

        //Thread read2 = new Thread(){
           // @Override
            //public void run(){
                Log.i("MyApp","Reader Thread running");
                StringBuilder line = new StringBuilder();
                char toAppend = ' ';
                try {
                    int i = 0;
                    while(true){
                        //Log.i("MyApp","first while loop");

                        final InputStreamReader tout = new InputStreamReader(inStream);
                        try {

                            while (tout.ready()) {
                                //Log.i("MyApp", "second while loop");
                                toAppend = (char) tout.read();
                                if (toAppend == '\n') {
                                    Log.i("MyApp", "line: " + line.toString());
                                    line.setLength(0);
                                } else
                                    line.append(toAppend);
                                if (tout.ready() == false) {
                                    //Log.i("MyApp", "Shit broke");
                                }
                            }
                            if (i >= 5) {
                                return;
                            }
                            i++;

                        } catch (Exception e) {
                            Log.e("MyApp","************errorrrrrrr reading character**********");
                        }
                        Thread.sleep(500);
                    }
                }catch (Exception ex) {
                    Log.i("MyApp",ex.getMessage());
                    try{
                        tout.close();
                    }
                    catch(Exception e)
                    {
                        Log.e("MyApp","error at catch");
                    }
                }
           // }
        //};
        //read2.start();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session = jsch.getSession("guest","18.225.22.185",22);
            session.setPassword("password");
            session.setConfig(config);

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
            Log.e("MyApp",e.getMessage());
        }
        Log.i("MyApp","Execute Async");
        return null;
    }
}
