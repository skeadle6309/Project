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
import java.util.regex.Pattern;

public class SSH_Server_Tensorflow implements Runnable {
    private JSch jsch;
    private Session session;
    private PrintWriter toChannel;
    private InputStream inStream;
    private Channel myChannel;
    private String sshPairReturn,fileName;
    private OutputStream outStream;
    private OutputStreamWriter outputStreamWriter;
    private Stack<String> fileNames;
    private Context context;
    private SSH_Server_DataBase database;
    private Boolean readyForImage = false;
    private Boolean readyForResults = false;
    final private String REGEX_PAIR = "^\\[\\[.*\\]\\]$";
    final private String REGEX_BEGIN_INPUT = "^0000.*";


    private static SSH_Server_Tensorflow instance = null;

    public static SSH_Server_Tensorflow getInsance(Context context) {
        if (instance == null) {
            instance = new SSH_Server_Tensorflow(context);
        }
        return instance;
    }


    private SSH_Server_Tensorflow(Context context) {
        Log.i("MyApp", "into SSH_Server_Tensorflow");
        jsch = new JSch();
        fileNames = new Stack<String>();
        this.context = context;
    }

    public void start() {
        //start the database
        database = SSH_Server_DataBase.getInsance(context);
        //start a new thread for this instance
        Thread thread = new Thread(instance);
        thread.start();
        try {
            //thread.join();
            Log.i("MyApp", "Thread Joined");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            //-------START SSH--------------------------
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session = jsch.getSession("guest", "18.225.22.185", 22);
            session.setPassword("password");
            session.setConfig(config);
            session.setTimeout(10000);
            Log.i("MyApp", "try To connect");
            session.connect();
            myChannel = session.openChannel("shell");
            myChannel.connect();
            Log.i("MyApp", "Connected");


            //Log.i("MyApp","Command Sent");
            //set up send command function
            outStream = myChannel.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outStream);
            toChannel = new PrintWriter(outputStreamWriter, true);
            //-------SSH STARTED--------------------------
            //send command
            sendCommand();

            //start reader thread
            inStream = myChannel.getInputStream();
            readerThread();

            while (true) {
                if (!fileNames.isEmpty()) {
                    fileName = fileNames.pop();
                    seeFood(fileName);
                    Thread.sleep(2000);
                    Pair<String,String> push = new Pair<>(fileName,sshPairReturn);
                    //database.addCommand(push);
                    //call a new thread that writes the file to the db
                    //toChannel.println("python /home/database/db.py 2 /home/guest/images/" + fileName + " " + sshPairReturn);
                }
            }

        }
        catch (Exception e) {
            Log.e("MyApp", e.getMessage());
        }
    }

    private void sendCommand() {
        if (session != null && session.isConnected()) {
            toChannel.println("cd ../ubuntu/seefood");
            toChannel.println("python seefood_initalize.py");
        }
    }

    public String getSshPairReturn() {
        return sshPairReturn;
    }
    private void trimSSHReturn() {
        sshPairReturn = sshPairReturn.replace("[","");
        sshPairReturn = sshPairReturn.replace("]","");
        sshPairReturn.trim();
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
                                    if (regExVerify(line.toString().trim(),REGEX_BEGIN_INPUT)){
                                        readyForImage = true;
                                    }
                                    if (regExVerify(line.toString().trim(), REGEX_PAIR)) {
                                        Log.i("MyApp", "Found Pair" + line.toString().trim());
                                        sshPairReturn = line.toString().trim();
                                        trimSSHReturn();
                                        readyForResults = true;
                                        //database = SSH_Server_DataBase.getInsance(context);

                                        //send the database the last line results(string comparison) and the filename
                                        database.addImageCommand(new Pair<String, String> (fileName,sshPairReturn));
                                    } else {
                                        Log.i("MyApp", line.toString().trim());
                                    }
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

    private void seeFood(String imgName) {
        toChannel.println(imgName);
//        Log.i("MyApp", "seefood");


    }

    public String addFile(String fileName) {
        while (!readyForImage)
        {
            Log.i("MyApp","Waiting on TensorFlow");
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        fileNames.push(fileName);
        Log.i("MyAppServer", "Waiting on results");
        while (!readyForResults){}
        Log.i("MyAppServer", "Results found");
        readyForResults = false;
        Log.i("MyAppServer", sshPairReturn);

        return sshPairReturn;
    }

    private boolean regExVerify(String line, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(line).matches();
    }
}
