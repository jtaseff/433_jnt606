package com.me433.john.campicv3;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CameraPIC extends Activity implements Camera.PreviewCallback {

    private Camera camera;
    private CameraPreview cameraPreview;
    private Camera.Parameters parameters;
    private Camera.Size previewSize;

    private TextView textStatusCam;
    private TextView textStatusComm;
    private TextView textStatusValue;
    private TextView textLine1Value;

    UsbManager manager = null;
    UsbSerialDriver driver = null;
    UsbDeviceConnection connection = null;
    UsbSerialPort port = null;

    private final String st = "Cereal";

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
        camera.stopPreview();
        textStatusCam.setText("video feed ready, paused");


    }

    //Buttons
    public void btnCamStart(View view) {
        if (camera != null) {
            camera.startPreview();
            textStatusCam.setText("Video feed started");
        }
    }

    public void btnCamStop(View view) {
        if (camera != null) {
            camera.stopPreview();
            textStatusCam.setText("video feed paused");
        }
    }

    public void btnCommStart(View view) {

        Log.i(st, "starting manager");
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.i(st, "starting prober");
        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x04D8, 0x000A, CdcAcmSerialDriver.class);
        UsbSerialProber prober = new UsbSerialProber(customTable);
        List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Log.i(st, "no available drivers");
            return;
        }
        Log.i(st, "found a device, getting driver");
        driver = availableDrivers.get(0);
        if (driver == null)
            Log.i(st, "driver is null");
        Log.i(st, "got driver, requesting permission");
        //manager.requestPermission(driver.getDevice(), null);
        Log.i(st, "passed permission, opening device");
        connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            Log.i(st, "connection is null");
            return;
        }
        Log.i(st, "have a connection, opening port");
        List<UsbSerialPort> ports = driver.getPorts();
        port = ports.get(0);
        try {
            port.open(connection);
            Log.i(st, "opened port!");


        } catch (IOException e) {
            Log.i(st, "caught at opening port: " + e);
        }

        textStatusComm.setText("data link opened");

        byte[] outmsg = {'f', 'a', 'r', 't'};
        try {
            port.write(outmsg, 500);
        } catch (IOException e) {
            Log.e(st, "error writing to port: " + e.getMessage());
        }


    }

    public void btnCommStop(View view) {

        textStatusComm.setText("data link stopped.");

        try {
            port.close();
        } catch (IOException e) {
            Log.e(st, "error closing port :" + e.getMessage());
        }

    }

    public void btnMotionStart(View view) {


        textStatusValue.setText("sent a thing");


    }

    public void btnMotionStop(View view) {

        textStatusValue.setText("stopped. Last value NULL");
    }


    //Serial stuff below


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

        Log.d("Camera", "Initialized to size " + previewSize.width + "w x " + previewSize.height + "h");
    }


    int size;
    int[] grpx;
    int p;
    int value;
    int findSum;
    int findNum;
    int linePos = 1000;
    int mean;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        numFrames++;

        size = previewSize.width * previewSize.height;
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
        if (port != null) {
            try {
                port.close();
            } catch (IOException e) {
                Log.e(st, "error closing port :" + e.getMessage());
            }
            port = null;

        }
        super.onDestroy();
    }


}
