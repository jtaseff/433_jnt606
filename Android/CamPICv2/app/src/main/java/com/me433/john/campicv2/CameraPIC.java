package com.me433.john.campicv2;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;


public class CameraPIC extends Activity {

    private TextView textStatusCam;
    private TextView textStatusComm;
    private TextView textStatusValue;
    private SurfaceView previewSurface;
    private Preview myPreview;
    private Camera mycam;

    private int direction = 0;
    private int frames = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_pic);


        //grab the objects we need
        textStatusCam = (TextView) findViewById(R.id.textStatusCam);
        textStatusComm = (TextView) findViewById(R.id.textStatusComm);
        textStatusValue = (TextView) findViewById(R.id.textStatusValue);
        previewSurface = (SurfaceView) findViewById(R.id.cameraPreview);

        SurfaceHolder previewHolder = previewSurface.getHolder();
        myPreview = new Preview(this);
        previewHolder.addCallback(myPreview);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);



    }


// Camera handlers

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mycam = Camera.open(id);
            qOpened = (mycam != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        myPreview.setCamera(null);
        if (mycam != null) {
            mycam.release();
            mycam = null;
        }
    }


//Button handlers

    public void btnCamStart(View view) {
        textStatusCam.setText("analyzing live");
    }

    public void btnCamStop(View view) {
        textStatusCam.setText("idle");
        myPreview.stopPreviewAndFreeCamera();
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


    public void newFrame() {
        textStatusValue.setText("we have some shit");
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        myPreview.stopPreviewAndFreeCamera();
    }
}




class Preview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    Context context;
    //SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Camera mycamS;
    TextView textStatusCam;
    Camera.Parameters parameters;
    Camera.Size previewSize;


    int numFrames = 0;

    Preview(Context contextPass) {
        super(contextPass);
        this.context = contextPass;

//        mSurfaceView = new SurfaceView(context);
//        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        textStatusCam = (TextView) findViewById(R.id.textStatusCam);

    }



    public void onPreviewFrame(byte[] data, Camera camera) {

        if (mHolder == null) {
            return;
        }

        numFrames ++;
        //Log.d("Camera", "Frame " + numFrames + "   size " + previewSize.height + "h x " + previewSize.width + "w");

        int size = previewSize.width*previewSize.height;
        int [] grpx = new int[size];
        int p;

        //extract the luminance value into a new array
        for (int ii = 0; ii < size; ii++) {
            p = (int) data[ii] & 0xFF;
            grpx[ii] = p;
        }

        int value = 0;
        int findSum = 0;
        int findNum = 0;
        int linePos = 1000;


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
        int mean = previewSize.height / 2;
        if (findNum > 0)
            mean = findSum / findNum;



        Log.d("Camera", "mean " + mean);

    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        mycamS = Camera.open();
        try {
            mycamS.setPreviewDisplay(holder);

        } catch (IOException exception) {
            mycamS.release();
            mycamS = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        parameters = mycamS.getParameters();

        // use the default preview size supported by this device
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        previewSize = previewSizes.get(1);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        // want a high-quality, small image
        parameters.setJpegQuality(95);
        parameters.setPictureSize(640, 480);
        //parameters.setColorEffect(Camera.Parameters.EFFECT_MONO); // try EFFECT_BLACKBOARD, EFFECT_NEGATIVE, EFFECT_WHITEBOARD

        mycamS.setParameters(parameters);
        mycamS.setDisplayOrientation(90); // correct preview rotation
        mycamS.startPreview();
        mycamS.setPreviewCallback(this);
    }

    public void setCamera(Camera camera) {
        if (mycamS == camera) { return; }

        stopPreviewAndFreeCamera();

        mycamS = camera;

        if (mycamS != null) {
            List<Camera.Size> localSizes = mycamS.getParameters().getSupportedPreviewSizes();
            //mSupportedPreviewSizes = localSizes;
            requestLayout();

            try {
                mycamS.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            mycamS.startPreview();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreviewAndFreeCamera();

        // Surface will be destroyed when we return, so stop the preview.
        if (mycamS != null) {
            // Call stopPreview() to stop updating the preview surface.
            mycamS.stopPreview();

            mycamS.release();
            mycamS = null;
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    public void stopPreviewAndFreeCamera() {

        if (mycamS != null) {
            // Call stopPreview() to stop updating the preview surface.
            mycamS.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mycamS.release();

            mycamS = null;
        }
    }
}