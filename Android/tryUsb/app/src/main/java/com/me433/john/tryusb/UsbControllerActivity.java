package com.me433.john.tryusb;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;


    /**
     * (c) Neuxs-Computing GmbH Switzerland
     * @author Manuel Di Cerbo, 02.02.2012
     *
     */
    public class UsbControllerActivity extends Activity {
        /** Called when the activity is first created. */
        private static final int VID = 0x04D8;
        private static final int PID = 0x003F;
        private static UsbController sUsbController;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            if(sUsbController == null){
                sUsbController = new UsbController(this, mConnectionHandler, VID, PID);
            }
            ((SeekBar)(findViewById(R.id.seekBar1))).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    if(fromUser){
                        if(sUsbController != null){
                            sUsbController.send((byte)(progress&0xFF));
                        }
                    }
                }
            });
            ((Button)findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(sUsbController == null)
                        sUsbController = new UsbController(UsbControllerActivity.this, mConnectionHandler, VID, PID);
                    else{
                        sUsbController.stop();
                        sUsbController = new UsbController(UsbControllerActivity.this, mConnectionHandler, VID, PID);
                    }
                }
            });

        }

        private final IUsbConnectionHandler mConnectionHandler = new IUsbConnectionHandler() {
            @Override
            public void onUsbStopped() {
                Log.e("USB", "Usb stopped!");
            }

            @Override
            public void onErrorLooperRunningAlready() {
                Log.e("USB", "Looper already running!");
            }

            @Override
            public void onDeviceNotFound() {
                if(sUsbController != null){
                    sUsbController.stop();
                    sUsbController = null;
                }
            }
        };
    }
