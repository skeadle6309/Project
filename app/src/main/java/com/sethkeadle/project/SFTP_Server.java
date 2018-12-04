package com.sethkeadle.project;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Stack;

public class SFTP_Server implements Runnable {
    private JSch jsch;
    private Session session;
    private Context context;
    private PrintWriter toChannel;
    private InputStream inStream;
    private ChannelSftp myChannel;
    private String fileName;
    private OutputStream outStream;
    private OutputStreamWriter outputStreamWriter;
    private Boolean SFTP_Running =false;
    private final String fileDestination = "/home/guest/images";
    private final String localFileDestination = "/home/guest/images";
    private Stack<Pair<Integer,String>> commandList;
    final private Integer two = 2;
    final private Integer one = 1;


    private static SFTP_Server instance = null;

    //Create SFTP_Server class for use with threads
    public static SFTP_Server getInsance(Context context) {
        if (instance == null) {
            instance = new SFTP_Server(context);
        }
        return instance;
    }

    //Load the initial SFTP server connection and load
    //the jsch library for communication with AWS
    private SFTP_Server(Context context) {
        Log.i("MyApp", "into SFTP");
        jsch = new JSch();
        commandList = new Stack<>();
        this.context = context;
    }

    //Push commands via SSH into AWS
    public void pushCommand(Pair<Integer,String>cmd) {
        SFTP_Running = true;
        commandList.push(cmd);
        Log.i("MyAppSftp","Cmd pushed " + cmd.first + " and " + cmd.second);
    }

    //Starts thread for passing commands into AWS, the image file will be passed
    //and be stored locally on AWS server
    public void start() {
//        this.fileName = fileName;
        Thread thread = new Thread(instance);
        thread.start();
        try {
//            thread.join();
            Log.i("MyApp","Thread Joined");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Main command line for running SFTP_Server.java class
    //Contains all logic and username/password credentials for logging
    //into AWS via the "guest" account.
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

            Log.i("MyApp","SFTP Connected");
//            send the put command
            Log.i("MyApp", fileName + ": " + fileDestination);
            while (true) {
                if (!commandList.empty()) {
                    if (commandList.peek().first == two) {
                        Pair<Integer,String> tmp = commandList.pop();
                        Log.i("MyAppSftp","start FOS");
                        try (FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/"+tmp.second,true)) {
                            Log.i("MyAppSftp","start IS filename is: " + Environment.getExternalStorageDirectory().getPath() + "/" + tmp.second);
                            try (InputStream in = myChannel.get(tmp.second)) {
                                // read from in, write to out
                                Log.i("MyAppSftp","Inside both trys");
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = in.read(buffer)) != -1) {
                                    out.write(buffer, 0, len);
                                }
                                out.close();
                                Log.i("MyAppSftp","Out stream finished");
                                SFTP_Running = false;
                            }
                        }
                    }
                    else {
                        myChannel.put(commandList.pop().second,fileDestination);
                    }
                }
            }
        }
        catch (Exception e) {
            Log.e("MyApp", e.getMessage());
        }
    }

    public Boolean getSFTP_Running(){
        return SFTP_Running;
    }

}
