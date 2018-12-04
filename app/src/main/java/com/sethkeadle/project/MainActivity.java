package com.sethkeadle.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import java.net.URI;
import java.sql.Blob;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivity extends AppCompatActivity {
    private Controller controller;

    private ImageView imageView;
    private Image image;
    private String filePath, fileName = "cookies.png";
    private int currentImg, dbSize;
    private final String phoneFileLocation = "/sdcard/images/";
    private Button nextBtn, prevBtn, seeFoodBtn, dataBaseBtn;
    private CustomGauge gauge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        //instantiate variables
        imageView = (ImageView)findViewById(R.id.imageView);
        nextBtn = (Button)findViewById(R.id.nextBtn);
        prevBtn = (Button)findViewById(R.id.prevBtn);
        seeFoodBtn = (Button)findViewById(R.id.seeFoodBtn);
        dataBaseBtn = (Button)findViewById(R.id.dataBaseBtn);
        gauge = findViewById(R.id.gauge);
        nextBtn.setVisibility(View.INVISIBLE);
        prevBtn.setVisibility(View.INVISIBLE);
        controller = new Controller(this);

        //get permissions
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.INTERNET
                }, 101); // your request code
    }

    //take the current image selected from the gallery and submit it to AWS to get seefood reults
    public void seeFood(View view) {
        //submit the photo to see if food exist
        controller.seeFood(fileName);
        gauge.setValue((int) Math.round(controller.getPercentSeeFood()));
        gauge.setVisibility(View.VISIBLE);
        Log.i("Finished", "return: " + controller.getreturn());
    }


    //keeps the current image index and compares it to the database size
    //if the current image index size is smaller than the database size update the current image.
    public void dataBase(View view) {
        currentImg = 1;
        dbSize = controller.getDbSize();
        if (dbSize >= 1)
        {
            updateImg();
        }
    }

    //take the current image index and submit it to the controller to fetch that index in the database
    //where the database returns the full path to the image and the seefood results in string format
    //the controller also updates the guage to provider user with visual representation of food
    private void updateImg() {
        String tmpImg = controller.getDbImageReturn(currentImg);
        Log.i("MyAppMain","getDbImgReturn(): "+tmpImg);
        controller.sftpGetImage(tmpImg);
        while (controller.isSFTP_Running()){}
        setDataBaseViews();
        imageView.setImageURI(Uri.parse(phoneFileLocation + tmpImg));
//        Toast.makeText(this, controller.getPercentDb().toString() , Toast.LENGTH_LONG).show();
        gauge.setValue((int) Math.round(controller.getPercentDb()));
        Log.i("Finished", "return: " + controller.getDbResultsReturn());
    }

    //override function for the image picker. sets the image view to the selected picture and saves
    //the image file name and the path to the image.
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // get a single image only
            image = ImagePicker.getFirstImageOrNull(data);
//            Toast.makeText(this, image.getPath(), Toast.LENGTH_LONG).show();
            filePath = image.getPath().trim();
            fileName = image.getName().trim();
            Log.i("MyApp",filePath + " controller.sftp()");
            controller.sftpAddImage(filePath);
            setSeefoodViews();
            imageView.setImageURI(Uri.parse(filePath));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //starts the image picker
    public void getPhoto(View view) {
        ImagePicker.create(this).start();
    }

    //increments the current image index and verifies it is within the database index, then updates
    public void nextPressed(View view) {
        if (currentImg < dbSize) {
            currentImg = currentImg+1;
            updateImg();
        }
        else {
            Toast.makeText(this, "Last Image in the database", Toast.LENGTH_SHORT).show();
        }
    }

    //increments the current image index and verifies it is within the database index, then updates
    public void prevPressed(View view) {
        if (currentImg > 1) {
            currentImg = currentImg -1;
            updateImg();
        }
        else {
            Toast.makeText(this, "First Image in the database", Toast.LENGTH_SHORT).show();
        }
    }

    //update the views easy functions.....
    public void setDataBaseViews(){
        seeFoodBtn.setVisibility(View.GONE);
        dataBaseBtn.setVisibility(View.GONE);
        nextBtn.setVisibility(View.VISIBLE);
        prevBtn.setVisibility(View.VISIBLE);
    }

    public void setSeefoodViews() {
        seeFoodBtn.setVisibility(View.VISIBLE);
        dataBaseBtn.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        prevBtn.setVisibility(View.INVISIBLE);
        gauge.setVisibility(View.INVISIBLE);
    }


}
