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
        //Log.i("MyApp","Trying to Exec");
        server.execute();
        //server.start();
        //Log.i("MyApp","Exec");
    }

    public void addPhotoCam(View view) {
        //Toast.makeText(this, "camera", Toast.LENGTH_SHORT).show();
        Server.getInsance(this).sendCommand();
        //Server.getInsance(this).readerThread();

    }

    public void addPhotoGal(View view) {
        Toast.makeText(this, "galery", Toast.LENGTH_SHORT).show();
    }
}
