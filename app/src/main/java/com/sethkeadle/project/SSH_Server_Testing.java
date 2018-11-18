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
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSH_Server_Testing implements Runnable {
    private JSch jsch;
    private Session session;
    private PrintWriter toChannel;
    private InputStream inStream;
    private Channel myChannel;
    private String sshPairReturn;
    private OutputStream outStream;
    private OutputStreamWriter outputStreamWriter;
    private Stack<String> fileNames;
    final private String REGEX_PAIR = "^\\[\\[.*\\]\\]$";
    final private String REGEX_WAIT = "^[E]nter .* exi[t]$";


    private static SSH_Server_Testing instance = null;

    public static SSH_Server_Testing getInsance(Context context) {
        if (instance == null) {
            instance = new SSH_Server_Testing(context);
        }
        return instance;
    }


    private SSH_Server_Testing(Context context) {
        Log.i("MyApp", "into SSH_Server");
        jsch = new JSch();
        fileNames = new Stack<String>();
    }

    public void start() {
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

            //send command
            sendCommand();

            //start reader thread
            inStream = myChannel.getInputStream();
            readerThread();

            while (true) {
                if (!fileNames.isEmpty()) {
                    seeFood(fileNames.pop());
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

    private void readerThread() {
        Thread thread = new Thread() {
            public void run() {
                Log.i("MyApp", "Reader Thread running");
                StringBuilder line = new StringBuilder();
                char toAppend;
                final InputStreamReader tout = new InputStreamReader(inStream);
                try {
                    int i = 0;
                    while (true) {
                        try {

                            while (tout.ready()) {
                                toAppend = (char) tout.read();
                                if (toAppend == '\n') {

                                    if (regExVerify(line.toString().trim(), REGEX_PAIR)) {
                                        Log.i("MyApp", "Found Pair" + line.toString().trim());
                                        sshPairReturn = line.toString().trim();
                                    } else {
                                        Log.i("MyApp", line.toString().trim());
                                    }
                                    line.setLength(0);
                                } else
                                    line.append(toAppend);
                            }
//                    if (i >= 10) {
//                        return;
//                    }
                            i++;
                            if (i % 2 == 0) {
                                //Log.i("MyApp","Still in while");
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

    private void seeFood(String imgPath) {
        toChannel.println(imgPath);
        Log.i("MyApp", "seefood");
    }

    public void addFile(String fileName) {
        fileNames.push(fileName);
    }

    private boolean regExVerify(String line, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(line).matches();
    }
}
