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

public class SSH_Server_DataBase implements Runnable {
    private JSch jsch;
    private Session session;
    private PrintWriter toChannel;
    private InputStream inStream;
    private Channel myChannel;
    private OutputStream outStream;
    private OutputStreamWriter outputStreamWriter;
    private String fileName,seeFoodResult,dbReturn,dbSize;
    private Stack<String> commandList;
    private Boolean waitOnResults;
    final private String REGEX_DB_RETURN = "^1111.*";
    final private String REGEX_DB_SIZE_RETURN = "^1112.*";
    final private String ADD_IMAGE_STRING = "python database/db.py 2 /home/guest/images/";
    final private String GET_IMAGE_STRING = "python database/db.py 4";
    final private String GET_DATABASE_SIZE = "python database/db.py 3";


    private static SSH_Server_DataBase instance = null;

    public static SSH_Server_DataBase getInsance(Context context) {
        if (instance == null) {
            instance = new SSH_Server_DataBase(context);
        }
        return instance;
    }


    private SSH_Server_DataBase(Context context) {
        Log.i("MyAppDb", "into Database");
        jsch = new JSch();
        commandList = new Stack<String>();
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
            Log.i("MyAppDb","Database try To connect");
            session.connect();
            myChannel = session.openChannel("shell");
            myChannel.connect();
            Log.i("MyAppDb","Database Connected");
            //set up send command function
            outStream = myChannel.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outStream);
            toChannel = new PrintWriter(outputStreamWriter, true);


            //start reader thread
            inStream = myChannel.getInputStream();
            readerThread();


            //send a command for testing
            sendCommand();
            //Log.i("MyApp","Command Sent");



        }
        catch (Exception e) {
            Log.e("MyAppDb", e.getMessage());
        }
    }
    private void readerThread() {
        Thread thread = new Thread() {
            public void run() {
                Log.i("MyAppDb", "Database Reader Thread running");
                StringBuilder line = new StringBuilder();
                char toAppend;
                final InputStreamReader tout = new InputStreamReader(inStream);
                try {
                    while (true) {
                        try {
                            while (tout.ready()) {
                                toAppend = (char) tout.read();
                                if (toAppend == '\n') {
                                    if (regExVerify(line.toString().trim(),REGEX_DB_RETURN)) {
                                        dbReturn = line.toString().trim();
                                        dbReturn = dbReturn.replace("1111","");
                                        waitOnResults = true;
                                    }
                                    else if (regExVerify(line.toString().trim(),REGEX_DB_SIZE_RETURN)) {
                                        dbSize = line.toString().trim();
                                        dbSize = dbSize.replace("1112 ","");
                                        waitOnResults = true;
                                    }
                                    if (!line.toString().trim().isEmpty()){
                                        Log.i("MyAppDb",line.toString().trim());
                                    }

                                    line.setLength(0);
                                } else
                                    line.append(toAppend);
                            }
                            sleep(1000);
                        }
                        catch (Exception e) {
                            Log.e("MyAppDb", "************error reading character**********");
                        }
                    }
                }
                catch (Exception ex) {
                    Log.i("MyAppDb", ex.getMessage());
                    try {
                        tout.close();
                    }
                    catch (Exception e) {
                        Log.e("MyAppDb", "error at catch");
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
                String tmp = commandList.pop();
                toChannel.println(tmp);
                Log.i("MyAppDb", "sendCommadn() command list pop: "+tmp);
            }
        }
    }

    public void addImageCommand(Pair<String,String> cmd) {
        String pushImage = ADD_IMAGE_STRING + cmd.first + " " + cmd .second;
        commandList.push(pushImage);
    }
    public String getImageCommand(int i) {
        commandList.push(GET_IMAGE_STRING + " " + Integer.toString(i));
        waitOnResults = false;
        while (!waitOnResults){}
//        Log.i("MyAppDb",dbReturn);
        return dbReturn;
    }

    //this will return a Integer when it works 100% in test mode for now
    public String getSize() {
        commandList.push(GET_DATABASE_SIZE);
        waitOnResults = false;
        while (!waitOnResults){}
//        Log.i("MyAppDb",dbSize);
        return dbSize;
    }
    private boolean regExVerify(String line, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(line).matches();
    }
}
