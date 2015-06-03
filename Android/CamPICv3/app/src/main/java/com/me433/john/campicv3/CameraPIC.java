package com.me433.john.campicv3;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
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
    private TextView textStatusPWM;
    private TextView textLine1Value;

    private SeekBar seekPower;
    private SeekBar seekProp;
    private SeekBar seekThresh;
    private SeekBar seekLinepos;
    private TextView seekPowerText;
    private TextView seekPropText;
    private TextView seekThreshText;
    private TextView seekLineposText;

    UsbManager manager = null;
    UsbSerialDriver driver = null;
    UsbDeviceConnection connection = null;
    UsbSerialPort port = null;

    private final String st = "Cereal";

    int numFrames = 0;

    double slowing = 0.5;   //  overall max power factor
    double Pscale = 0.5;    //  scaling factor - proportional control
    int threshold = 100;    //  image analysis dark pixel threshold
    int linePos = 200;     //  position of line to look at


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_pic);

        textStatusCam = (TextView) findViewById(R.id.textStatusCam);
        textStatusComm = (TextView) findViewById(R.id.textStatusComm);
        textLine1Value = (TextView) findViewById(R.id.textLine1Value);
        textStatusPWM = (TextView) findViewById(R.id.textStatusPWM);

        seekPower = (SeekBar) findViewById(R.id.seekPower);
        seekProp = (SeekBar) findViewById(R.id.seekProp);
        seekPowerText = (TextView) findViewById(R.id.seekPowerText);
        seekPropText = (TextView) findViewById(R.id.seekPropText);
        seekThresh = (SeekBar) findViewById(R.id.seekThresh);
        seekThreshText = (TextView) findViewById(R.id.seekThreshText);
        seekLinepos = (SeekBar) findViewById(R.id.seekLinepos);
        seekLineposText = (TextView) findViewById(R.id.seekLineposText);

        seekPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                slowing = (double) seekBar.getProgress() / 100;
                seekPowerText.setText(seekBar.getProgress() + "% Power");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekProp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Pscale = (double) seekBar.getProgress() / 100;
                seekPropText.setText(seekBar.getProgress() + "% Proportional");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        seekThresh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold = seekBar.getProgress();
                seekThreshText.setText(seekBar.getProgress() + " img threshold");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }


        });



        seekLinepos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                linePos = seekBar.getProgress();
                seekLineposText.setText(seekBar.getProgress() + " line position");

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) textLine1Value.getLayoutParams();
                params.bottomMargin = (1000 - linePos)*3/8 + 80;
                textLine1Value.setLayoutParams(params);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        initCameraPreview();
        textStatusCam.setText("video feed running");



    }

    //Buttons
    public void btnFocus(View view) {
        if (camera != null) {
            camera.autoFocus(null);
        }
    }

    public void btnCamStart(View view) {
        if (camera != null) {
            camera.startPreview();
            textStatusCam.setText("Video feed started");

            textStatusCam.setTextColor(Color.BLACK);
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

        byte[] outmsg = {0, 0, 0, 0};
        try {
            port.write(outmsg, 500);
        } catch (IOException e) {
            Log.e(st, "error writing to port: " + e.getMessage());
        }

        serialReady = true;


    }

    public void btnCommStop(View view) {

        textStatusComm.setText("data link stopped.");

        try {
            port.close();
        } catch (IOException e) {
            Log.e(st, "error closing port :" + e.getMessage());
        }

    }

    String PWMstatus = "Stopped  ";

    public void btnMotionStart(View view) {
        motionOn = 2;
        updateRobot();
        PWMstatus = "Started  ";

    }

    public void btnMotionIdle(View view) {
        motionOn = 1;
        updateRobot();
        PWMstatus = "Idle  ";
    }

    public void btnMotionStop(View view) {
        motionOn = 0;
        updateRobot();
        PWMstatus = "Stopped  ";
    }


    //Serial stuff below
    boolean serialReady = false;
    byte motionOn = 0;
    int left;
    int right;
    int diff;
    boolean lastwasleft;
    boolean lost;

    byte[] outmsg = new byte[4];

    private void updateRobot() {
        if (motionOn < 2) {
            // idle or stopped
            left = 0;
            right = 0;
        }
        if (motionOn == 2) {
            // motion enabled


            if (mean < 360){
                lost = false;
                lastwasleft = false;
                diff = 360 - mean;
                right = (int) ((255 - Pscale*diff) * slowing);
                left = (int) (255 * slowing);
            } else if (mean > 360) {
                lost = false;
                lastwasleft = true;
                diff = mean - 360;
                right = (int) (255 * slowing);
                left = (int) ((255 - Pscale*diff) * slowing);
            } else {    // we have 360 - maybe keep the last values??
                if (!lost) {
                    textStatusCam.setText("LOST");
                    textStatusCam.setTextColor(Color.RED);
                    lost = true;
                    left = left / 2;
                    right = right / 2;
                }
//                if (lastwasleft) {
//                    left = 0;
//                    right = (int) (255 * slowing * 0.5);
//                }
//                else {
//                    left = (int) (255 * slowing * 0.5);
//                    right = 0;
//                }
            }
            if (left < 0)
                left = 0;
            if (left > 255)
                left = 255;
            if (right < 0)
                right = 0;
            if (right > 255)
                right = 255;

        }
        outmsg[0] = motionOn;
        outmsg[1] = (byte) left;
        outmsg[2] = (byte) right;
        textStatusPWM.setText(PWMstatus + outmsg[0] + " : " + left + " : " + right);

        if (serialReady) {
            try {
                port.write(outmsg, 500);
            } catch (IOException e) {
                Log.e(st, "error writing to port: " + e.getMessage());
            }
        }
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

        Log.d("Camera", "Initialized to size " + previewSize.width + "w x " + previewSize.height + "h");
    }


    int size;
    int[] grpx;
    int p;
    int value;
    int findSum;
    int findNum;
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
                if (value < threshold) {
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

        updateRobot();

    }



    @Override
    protected void onPause() {

        motionOn = 0;
        updateRobot();

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

        motionOn = 0;
        updateRobot();

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
