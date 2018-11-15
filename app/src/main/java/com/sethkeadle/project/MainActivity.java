package com.sethkeadle.project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create the server connection
        server = Server.getInsance(this);

        //get permissions
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.INTERNET
                }, 101); // your request code

    }

    public void seeFood(View view) {
        //Toast.makeText(this, "See Food", Toast.LENGTH_SHORT).show();

        Thread thread = new Thread(server);
        try {
            thread.start();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
        }
//        try {
//            server.exec();
//        }
//        catch (Exception e) {
//            Log.i("MyApp", "Execute Failed");
//        }
    }

    public void addPhotoCam(View view) {
        Toast.makeText(this, "camera", Toast.LENGTH_SHORT).show();
    }

    public void addPhotoGal(View view) {
        Toast.makeText(this, "galery", Toast.LENGTH_SHORT).show();
    }
}
