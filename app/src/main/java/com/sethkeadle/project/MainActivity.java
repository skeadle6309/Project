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

public class MainActivity extends AppCompatActivity {
    private Controller controller;

    private ImageView imageView;
    private Image image;
    private String filePath, fileName = "cookies.png";
    private int currentImg, dbSize;
    private final String phoneFileLocation = "/sdcard/images/";
    private Button nextBtn, prevBtn, seeFoodBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        //get permissions

        //instantiate vars
        imageView = (ImageView)findViewById(R.id.imageView);
        nextBtn = (Button)findViewById(R.id.nextBtn);
        prevBtn = (Button)findViewById(R.id.prevBtn);
        seeFoodBtn = (Button)findViewById(R.id.seeFoodBtn);
        nextBtn.setVisibility(View.INVISIBLE);
        prevBtn.setVisibility(View.INVISIBLE);
        controller = new Controller(this);

        //get permissions
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.INTERNET
                }, 101); // your request code
    }

    public void seeFood(View view) {
        //submit the photo to see if food exist
        controller.seeFood(fileName);
        Log.i("Finished", "return: " + controller.getreturn());
    }

    public void toastTest(View view) {
        //Toast.makeText(this, controller.getreturn(), Toast.LENGTH_LONG).show();
        //Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show();
//        controller.getDbImageReturn(1);
//        Integer tmp = controller.getDbSize();
//        Log.i("MyAppMain", tmp.toString());
//        controller.getDbImage(1);
        currentImg = 1;
        dbSize = controller.getDbSize();
        if (dbSize >= 1)
        {
            updateImg();
        }
    }
    private void updateImg() {
        String tmpImg = controller.getDbImageReturn(currentImg);
        Log.i("MyAppMain","getDbImgReturn(): "+tmpImg);
        controller.sftpGetImage(tmpImg);
        while (controller.isSFTP_Running()){}
        seeFoodBtn.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.VISIBLE);
        prevBtn.setVisibility(View.VISIBLE);
        imageView.setImageURI(Uri.parse(phoneFileLocation + tmpImg));
        Log.i("Finished", "return: " + controller.getDbResultsReturn());
    }
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // get a single image only
            image = ImagePicker.getFirstImageOrNull(data);
            Toast.makeText(this, image.getPath(), Toast.LENGTH_LONG).show();
            filePath = image.getPath().trim();
            fileName = image.getName().trim();
            Log.i("MyApp",filePath + " controller.sftp()");
            controller.sftpAddImage(filePath);
//            sftp.start(filePath);


            //display image and update btn's
            seeFoodBtn.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.INVISIBLE);
            prevBtn.setVisibility(View.INVISIBLE);
            imageView.setImageURI(Uri.parse(filePath));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void getPhoto(View view) {
        ImagePicker.create(this).start();
    }

    public void nextPressed(View view) {
        if (currentImg < dbSize) {
            currentImg = currentImg+1;
            updateImg();
        }
        else {
            Toast.makeText(this, "Last Image in the database", Toast.LENGTH_SHORT).show();
        }
    }

    public void prevPressed(View view) {
        if (currentImg > 1) {
            currentImg = currentImg -1;
            updateImg();
        }
        else {
            Toast.makeText(this, "First Image in the database", Toast.LENGTH_SHORT).show();
        }
    }
}
