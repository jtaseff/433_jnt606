package com.me433.john.tryusb;

/**
 * Created by John on 5/24/2015.
 */
public interface IUsbConnectionHandler {

    void onUsbStopped();

    void onErrorLooperRunningAlready();

    void onDeviceNotFound();
}