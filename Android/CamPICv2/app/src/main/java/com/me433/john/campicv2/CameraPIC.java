package com.me433.john.campicv2;

import android.app.Activity;
import android.graphics.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;


public class CameraPIC extends Activity {

    private TextView textStatusCam;
    private TextView textStatusComm;
    private TextView textStatusValue;
    private SurfaceView preview;
    private Camera mycam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_pic);


        //grab the objects we need
        textStatusCam = (TextView) findViewById(R.id.textStatusCam);
        textStatusComm = (TextView) findViewById(R.id.textStatusComm);
        textStatusValue = (TextView) findViewById(R.id.textStatusValue);
        preview = (SurfaceView) findViewById(R.id.cameraPreview);
    }


    // Camera handlers


//Button handlers

    public void btnCamStart(View view) {
        textStatusCam.setText("analyzing live");
    }

    public void btnCamStop(View view) {
        textStatusCam.setText("idle");
    }

    public void btnCommStart(View view) {
        textStatusComm.setText("data link waiting.");
    }

    public void btnCommStop(View view) {
        textStatusComm.setText("data link stopped.");
    }

    public void btnMotionStart(View view) {
        textStatusValue.setText("started, waiting for value");
    }

    public void btnMotionStop(View view) {
        textStatusValue.setText("stopped. Last value NULL");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera_pic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
