package com.me433.john.campicv3;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.TextView;


public class CameraPIC extends Activity implements Camera.PreviewCallback {

    private Camera camera;
    private CameraPreview cameraPreview;
    private Camera.Parameters parameters;
    private Camera.Size previewSize;

    private TextView textStatusCam;
    private TextView textStatusComm;
    private TextView textStatusValue;
    private TextView textLine1Value;

    int numFrames = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_pic);

        textStatusCam = (TextView) findViewById(R.id.textStatusCam);
        textStatusComm = (TextView) findViewById(R.id.textStatusComm);
        textStatusValue = (TextView) findViewById(R.id.textStatusValue);
        textLine1Value = (TextView) findViewById(R.id.textLine1Value);


        initCameraPreview();
        textStatusCam.setText("analyzing live");

    }

    //Buttons
    public void btnCamStart(View view) {
        if (camera != null) {
            camera.startPreview();
            textStatusCam.setText("started again");
        }
    }

    public void btnCamStop(View view) {
        if (camera != null) {
            camera.stopPreview();
            textStatusCam.setText("stopped");
        }
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


    //Camera stuff below


    private void initCameraPreview() {
        try {
            try {
                camera = Camera.open();
                camera.setPreviewCallback(this);
            } catch (Exception e) {
                Log.e("Camera", "Can't open camera. " + e);
            }

            cameraPreview = (CameraPreview) findViewById(R.id.cameraPreview2);
            cameraPreview.init(camera);

            camera.autoFocus(null);

            parameters = camera.getParameters();
            previewSize = parameters.getPreviewSize();

        } catch (Exception e) {
            Log.e("Camera", "Error initializing camera. " + e);
        }

        Log.d("Camera", "Initialized to size " + previewSize.width +"w x " + previewSize.height + "h");
    }


    int size;
    int [] grpx;
    int p;
    int value;
    int findSum;
    int findNum;
    int linePos = 1000;
    int mean;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        numFrames ++;

        size = previewSize.width*previewSize.height;
        grpx = new int[size];

        //extract the luminance value into a new array
        for (int ii = 0; ii < size; ii++) {
            p = (int) data[ii] & 0xFF;
            grpx[ii] = p;
        }

        value = 0;
        findSum = 0;
        findNum = 0;



        //look at a chunk of 5 rows, every other, to eliminate noise in a single line
        for (int jj = 0; jj < 10; jj += 2) {
            //go across the row and tally up the location of all dark pixels
            for (int ii = 0; ii < previewSize.height; ii++) {
                value = grpx[(ii * previewSize.width) + linePos + jj];
                if (value < 100) {
                    findSum += ii;
                    findNum++;
                }
            }
        }

        //now take the average of the dark pixel locations to get the line center
        mean = previewSize.height / 2;
        if (findNum > 0)
            mean = findSum / findNum;

        textLine1Value.setText("" + mean);

    }


//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//        // Now that the size is known, set up the camera parameters and begin
//        // the preview.
//        parameters = camera.getParameters();
//
//        // use the default preview size supported by this device
//        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//        previewSize = previewSizes.get(1);
//        parameters.setPreviewSize(previewSize.width, previewSize.height);
//
//        // want a high-quality, small image
//        parameters.setJpegQuality(95);
//        parameters.setPictureSize(640, 480);
//        parameters.setColorEffect(Camera.Parameters.EFFECT_MONO); // try EFFECT_BLACKBOARD, EFFECT_NEGATIVE, EFFECT_WHITEBOARD
//
//        camera.setParameters(parameters);
//        camera.setDisplayOrientation(90); // correct preview rotation
//        camera.startPreview();
//        camera.setPreviewCallback(this);
//    }


//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        //super.surfaceCreated(holder);
//    }


    @Override
    protected void onPause() {

        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
        super.onDestroy();
    }


}
