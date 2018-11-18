package com.sethkeadle.project;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import java.io.File;
import java.net.URL;
import java.sql.Blob;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SSH_Server_Testing server;
    private SFTP_Server sftp;
    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private ImageView imageView;
    private Image image;
    private String filePath, fileName;
    private Blob blob;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //instantiate vars
        server = SSH_Server_Testing.getInsance(this).getInsance(this);
        sftp = SFTP_Server.getInsance(this);
        //get permissions
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.INTERNET
                }, 101); // your request code


        //trying shit
    }

    public void seeFood(View view) {
        //Log.i("MyApp","Trying to Exec");
        //server.execute();
        server.start(fileName);

        //Log.i("MyApp","Exec");
    }

    public void addPhotoCam(View view) {
        //Toast.makeText(this, "camera", Toast.LENGTH_SHORT).show();

//        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(takePicture, 0);

        ImagePicker.create(this).start();

    }

    public void addPhotoGal(View view) {
        //Toast.makeText(this, "galery", Toast.LENGTH_SHORT).show();
//        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(pickPhoto , 1);
        //Log.i("MyApp",file.toString());
        Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
        server.addFile("cookies.png");
    }

    public void toastTest(View view) {
        String lastLine = server.getLastLine();
        Toast.makeText(this, lastLine, Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // get a single image only
            image = ImagePicker.getFirstImageOrNull(data);
            Toast.makeText(this, image.getPath(), Toast.LENGTH_LONG).show();
            filePath = image.getPath();
            fileName = image.getName();
            Log.i("MyApp",filePath);
            Log.i("MyApp","start sftp");
            sftp.start(filePath);
            Log.i("MyApp","sftp completed");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
