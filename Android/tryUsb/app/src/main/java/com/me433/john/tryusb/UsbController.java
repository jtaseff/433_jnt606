package com.me433.john.tryusb;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class UsbController {

    private final Context mApplicationContext;
    private final UsbManager mUsbManager;
    private final IUsbConnectionHandler mConnectionHandler;
    private final int VID;
    private final int PID;
    protected static final String ACTION_USB_PERMISSION = "com.me433.john.tryusb";

    /**
     * Activity is needed for onResult
     *
     * @param parentActivity
     */
    public UsbController(Activity parentActivity,
                         IUsbConnectionHandler connectionHandler, int vid, int pid) {
        mApplicationContext = parentActivity.getApplicationContext();
        mConnectionHandler = connectionHandler;
        mUsbManager = (UsbManager) mApplicationContext
                .getSystemService(Context.USB_SERVICE);
        VID = vid;
        PID = pid;
        init();
    }

    private void init() {
        enumerate(new IPermissionListener() {
            @Override
            public void onPermissionDenied(UsbDevice d) {
                UsbManager usbman = (UsbManager) mApplicationContext
                        .getSystemService(Context.USB_SERVICE);
                PendingIntent pi = PendingIntent.getBroadcast(
                        mApplicationContext, 0, new Intent(
                                ACTION_USB_PERMISSION), 0);
                mApplicationContext.registerReceiver(mPermissionReceiver,
                        new IntentFilter(ACTION_USB_PERMISSION));
                usbman.requestPermission(d, pi);
            }
        });
    }

    public void stop() {
        mStop = true;
        synchronized (sSendLock) {
            sSendLock.notify();
        }
        try {
            if (mUsbThread != null)
                mUsbThread.join();
        } catch (InterruptedException e) {
            e(e);
        }
        mStop = false;
        mLoop = null;
        mUsbThread = null;

        try {
            mApplicationContext.unregisterReceiver(mPermissionReceiver);
        } catch (IllegalArgumentException e) {
        }
    }

    private UsbRunnable mLoop;
    private Thread mUsbThread;

    private void startHandler(UsbDevice d) {
        if (mLoop != null) {
            mConnectionHandler.onErrorLooperRunningAlready();
            return;
        }
        mLoop = new UsbRunnable(d);
        mUsbThread = new Thread(mLoop);
        mUsbThread.start();
    }

    public void send(byte data) {
        mData = data;
        synchronized (sSendLock) {
            sSendLock.notify();
        }
    }

    private void enumerate(IPermissionListener listener) {
        l("enumerating");
        HashMap<String, UsbDevice> devlist = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviter = devlist.values().iterator();
        while (deviter.hasNext()) {
            UsbDevice d = deviter.next();
            l("Found device: "
                    + String.format("%04X:%04X", d.getVendorId(),
                    d.getProductId()));
            if (d.getVendorId() == VID && d.getProductId() == PID) {
                l("Device under: " + d.getDeviceName());
                if (!mUsbManager.hasPermission(d)) {
                    listener.onPermissionDenied(d);
                    l("permission fubar");
                }
                else {
                    startHandler(d);
                    l("starting handler");
                    return;
                }
                break;
            }
        }
        l("no more devices found");
        mConnectionHandler.onDeviceNotFound();
    }

    private class PermissionReceiver extends BroadcastReceiver {
        private final IPermissionListener mPermissionListener;

        public PermissionReceiver(IPermissionListener permissionListener) {
            mPermissionListener = permissionListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mApplicationContext.unregisterReceiver(this);
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                if (!intent.getBooleanExtra(
                        UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    mPermissionListener.onPermissionDenied((UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE));
                } else {
                    l("Permission granted");
                    UsbDevice dev = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (dev != null) {
                        if (dev.getVendorId() == VID
                                && dev.getProductId() == PID) {
                            startHandler(dev);// has new thread
                        }
                    } else {
                        e("device not present!");
                    }
                }
            }
        }

    }

    // MAIN LOOP
    private static final Object[] sSendLock = new Object[] {};
    private boolean mStop = false;
    private byte mData = 0x00;
    private boolean epINExists;
    private boolean epOUTExists;

    private class UsbRunnable implements Runnable {
        private final UsbDevice mDevice;

        UsbRunnable(UsbDevice dev) {
            mDevice = dev;
        }

        @Override
        public void run() {// here the main USB functionality is implemented
            l("opening");
            UsbDeviceConnection conn = mUsbManager.openDevice(mDevice);
            l("opened, trying to claim");
            if (!conn.claimInterface(mDevice.getInterface(0), true)) {
                l("could not claim");
                return;
            }
            l("claimed");

            UsbEndpoint epIN = null;
            UsbEndpoint epOUT = null;

            UsbInterface usbIf = mDevice.getInterface(1);
            for (int i = 0; i < usbIf.getEndpointCount(); i++) {
                if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                        epIN = usbIf.getEndpoint(i);
                        epINExists = true;
                    } else {
                        epOUT = usbIf.getEndpoint(i);
                        epOUTExists = true;
                    }
                }
            }

            for (;;) {// this is the main loop for transferring
                synchronized (sSendLock) {
                    try {
                        sSendLock.wait();
                    } catch (InterruptedException e) {
                        if (mStop) {
                            mConnectionHandler.onUsbStopped();
                            return;
                        }
                        e.printStackTrace();
                    }
                }
                //UsbControllerActivity.mDataSent = new byte[] { (byte) (0x34) };
//                UsbControllerActivity.bulkXFEROutReply = conn.bulkTransfer(
//                        epOUT, UsbControllerActivity.mDataSent, 1, 0);
                byte [] shit = new byte[16];
                shit[0] = 0;
                shit[1] = (byte) 0x88;

                conn.bulkTransfer(epOUT, shit, 1, 0);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                UsbControllerActivity.bulkXFERInReply = conn.bulkTransfer(epIN,
//                        UsbControllerActivity.mDataReceived, 20, 0);
                int reply = conn.bulkTransfer(epIN, shit, 20, 0);

                if (mStop) {
                    mConnectionHandler.onUsbStopped();
                    return;
                }

                l("reply " + reply);
                l("replydata " + shit);
            }
        }
    }

    // END MAIN LOOP
    private BroadcastReceiver mPermissionReceiver = new PermissionReceiver(
            new IPermissionListener() {
                @Override
                public void onPermissionDenied(UsbDevice d) {
                    l("Permission denied on " + d.getDeviceId());
                }
            });

    private static interface IPermissionListener {
        void onPermissionDenied(UsbDevice d);
    }

    public final static String TAG = "USBController";

    private void l(Object msg) {
        Log.d(TAG, ">==< " + msg.toString() + " >==<");
    }

    private void e(Object msg) {
        Log.e(TAG, ">==< " + msg.toString() + " >==<");
    }
}