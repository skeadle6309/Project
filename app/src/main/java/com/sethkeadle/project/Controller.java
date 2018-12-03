package com.sethkeadle.project;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import java.util.Stack;

public class Controller {
    private Context context;
    private SSH_Server_Testing server;
    private SFTP_Server sftp;
    private SSH_Server_DataBase dataBase;
    private Stack<String> databaseCommandList;
    private String fileNameReturned, fileResultsReturned;
    final private Integer two = 2;
    final private Integer one = 1;
    final private String REGEX_FILE_RETURN = ".+\\.(jpg|png|jpeg|JPG|PNG|JPEG|img|IMG)";

    public Controller(Context context) {
        this.context = context;
        server = SSH_Server_Testing.getInsance(context);
        sftp = SFTP_Server.getInsance(context);
        dataBase = SSH_Server_DataBase.getInsance(context);
        //start the servers now
        server.start();
        dataBase.start();
        sftp.start();
    }

    public void seeFood(String fileName) {
        server.addFile(fileName);
    }
    public String getreturn() {
        return convertSSH(server.getSshPairReturn());
    }
    public void sftpAddImage(String filePath) {
        Log.i("MyApp","sftp add image");
//        sftp.start(filePath);
        sftp.pushCommand(new Pair<>(1,filePath));
    }
    public void sftpGetImage(String imgName) {
        sftp.pushCommand(new Pair<>(two,"images/"+imgName));
    }
    private String convertSSH(String string) {
        string = string.replace("[","");
        string = string.replace("]","");
        return string.trim();
    }
    public void getDbImageReturn(int i) {
        String dbImg = dataBase.getImageCommand(i);
        dbImg = dbImg.replace(i+" /home/guest/images/","").trim();
        Log.i("MyAppCtrl",dbImg);
        seperateFileReturn(dbImg);
        Log.i("MyAppCtrl","fileName: "+fileNameReturned + " Results: " + fileResultsReturned);
    }
    public void addDbImage(Pair<String,String> cmd) {
        dataBase.addImageCommand(cmd);
    }
    public Integer getDbSize() {
        String size = dataBase.getSize();
        Integer i = Integer.parseInt(size);
        return i;
    }
    private void seperateFileReturn (String returnImg) {
        String[] arrOfStr = returnImg.split(" ", 5);
        fileNameReturned = arrOfStr[0];
        fileResultsReturned = arrOfStr[1] + " " + arrOfStr[2];
    }
}
