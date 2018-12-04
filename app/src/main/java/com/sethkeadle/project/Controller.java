package com.sethkeadle.project;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import java.util.Stack;

public class Controller {
    private Context context;
    private SSH_Server_Tensorflow server;
    private SFTP_Server sftp;
    private SSH_Server_DataBase dataBase;
    private Stack<String> databaseCommandList;
    private String fileNameReturned, fileResultsReturned, seeFoodReturn;
    private int currentDbSize;

    final private Integer two = 2;
    final private Integer one = 1;
    final private String REGEX_FILE_RETURN = ".+\\.(jpg|png|jpeg|JPG|PNG|JPEG|img|IMG)";

    public Controller(Context context) {
        this.context = context;
        server = SSH_Server_Tensorflow.getInsance(context);
        sftp = SFTP_Server.getInsance(context);
        dataBase = SSH_Server_DataBase.getInsance(context);
        //start the servers now
        server.start();
        dataBase.start();
        sftp.start();
    }

    //submit file to the SSH server to be processed by seefood
    public void seeFood(String fileName) {
        seeFoodReturn = server.addFile(fileName);
    }
    //gets the return from server. verifies the server has completed the process before retuning the
    //results
    public String getreturn() {
        Log.i("MyAppCtrl","waiting on seeFoodResults");
        while (seeFoodReturn == null){}
        Log.i("MyAppCtrl","Found seeFoodResults");
        String tmp = seeFoodReturn;
        seeFoodReturn = null;
        return tmp;
    }

    //add a new image via sftp
    public void sftpAddImage(String filePath) {
        Log.i("MyApp","sftp add image");
        sftp.pushCommand(new Pair<>(1,filePath));
    }
    //pull an image from the EC2 instance to the phone
    public void sftpGetImage(String imgName) {
        sftp.pushCommand(new Pair<>(two,"images/"+imgName));
    }
    //pull a image from the database with a key id of int i
    public String getDbImageReturn(int i) {
        String dbImg = dataBase.getImageCommand(i);
        dbImg = dbImg.replace(i+" /home/guest/images/","").trim();
        Log.i("MyAppCtrl",dbImg);
        seperateFileReturn(dbImg);
        Log.i("MyAppCtrlThis","fileName: "+fileNameReturned + " Results: " + fileResultsReturned);
        return fileNameReturned;
    }
    //get the size of the database
    public int getDbSize() {
        String size = dataBase.getSize();
        currentDbSize = Integer.parseInt(size);
        return currentDbSize;
    }
    //parse the return
    private void seperateFileReturn (String returnImg) {
        String[] arrOfStr = returnImg.split(" ", 5);
        fileNameReturned = arrOfStr[0];
        fileResultsReturned = arrOfStr[1] + " " + arrOfStr[2];
    }
    //check if the SFTP is running
    public Boolean isSFTP_Running() {
        return sftp.getSFTP_Running();
    }
    //return the database pair results
    public String getDbResultsReturn(){
        return fileResultsReturned;
    }
    //returns the percent of food confidence for seefood return.
    public Double getPercentSeeFood(){
        return getPercent(getreturn());
    }
    //returns the percent of food confidence for database return.
    public Double getPercentDb() {
        return getPercent(fileResultsReturned);
    }
    //super secret algorithm that compares results and gives a confidence percentage
    private Double getPercent(String totalScore) {
        String[] scores = totalScore.split("\\s+");

        //Convert the Strings into doubles
        double score_1 = Double.parseDouble(scores[0]);
        double score_2 = Double.parseDouble(scores[1]);

        double finalScore2 = score_1 - Math.abs(score_2);

        //Put the score on a 0 to -10 scale
        double test = (finalScore2) + 5;

        //Convert the scale to 0 to 10 and find a percentage
        double finalScore = test * 10;

        if (finalScore > 100){
            finalScore = 100.0;
        } else if (finalScore < 0){
            finalScore = 0.0;
        }

        //Return final percentage
        return finalScore;
    }
}
