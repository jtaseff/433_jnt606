package com.me433.john.campicv3;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceHolder holder;
    Camera.Parameters parameters;
    Camera.Size previewSize;



    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreview(Context context) {
        super(context);
    }

    public void init(Camera camera) {
        this.camera = camera;
        initSurfaceHolder();
    }

    private void initSurfaceHolder() {
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //camera = Camera.open();
        initCamera(holder);
    }



    private void initCamera(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            Log.e("Camera", "Error setting camera preview" + e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        parameters = camera.getParameters();

        // use the default preview size supported by this device
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        previewSize = previewSizes.get(1);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        // want a high-quality, small image
        parameters.setJpegQuality(95);
        parameters.setPictureSize(640, 480);
        //parameters.setColorEffect(Camera.Parameters.EFFECT_MONO); // try EFFECT_BLACKBOARD, EFFECT_NEGATIVE, EFFECT_WHITEBOARD

        camera.setParameters(parameters);
        camera.setDisplayOrientation(90); // correct preview rotation
        camera.startPreview();
        //camera.setPreviewCallback(this);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}


