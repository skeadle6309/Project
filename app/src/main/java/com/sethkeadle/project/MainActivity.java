package com.sethkeadle.project;

import android.Manifest;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.Toast;


import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import java.sql.Blob;

public class MainActivity extends AppCompatActivity {

    private SSH_Server_Testing server;
    private SFTP_Server sftp;

    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private ImageView imageView;
    private Image image;
    private String filePath, fileName = "cookies.png";

    private Blob blob;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //instantiate vars
        imageView = (ImageView)findViewById(R.id.imageView);
        server = SSH_Server_Testing.getInsance(this);
        sftp = SFTP_Server.getInsance(this);

        //get permissions
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.INTERNET
                }, 101); // your request code

        //start the server now
        server.start();
    }

    public void seeFood(View view) {
        //submit the photo to see if food exist

        //change this to filename when the database is working properly
        server.addFile(fileName);
        String lastLine = server.getSshPairReturn();
        Toast.makeText(this, lastLine, Toast.LENGTH_LONG).show();
    }

    public void toastTest(View view) {


        //Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // get a single image only
            image = ImagePicker.getFirstImageOrNull(data);
            Toast.makeText(this, image.getPath(), Toast.LENGTH_LONG).show();
            filePath = image.getPath().trim();
            fileName = image.getName().trim();
            Log.i("MyApp",filePath);
            Log.i("MyApp","start sftp");


            sftp.start(filePath);
            Log.i("MyApp","sftp completed");

            //display image
            imageView.setImageURI(Uri.parse(filePath));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void getPhoto(View view) {
        ImagePicker.create(this).start();
    }
}
