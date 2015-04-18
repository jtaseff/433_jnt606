#include <stdio.h>
#include <stdlib.h>
#include <xc.h> // processor SFR definitions
#include <sys/attribs.h> // __ISR macro
#include "i2c_master.h"
#include "oled_graphics.h"
#include "accel_spi.h"

void hw5_function() {
    // LED output
    ANSELBCLR = 0x01;
    TRISBCLR = 0x01;
    LATBSET = 0x01;

    //set up accelerometer
    acc_setup();
    //variables for accel data reads
    short accels[3];
    short mags[3];
    short temp;
    //short accdisp[3];

    //set up OLED
    //pause for OLED to power up
    _CP0_SET_COUNT(0);
    while(_CP0_GET_COUNT() < 4000000);
    oled_init();
    oled_clear_buffer();


    char magmsg[25];
    char tempmsg[10];

    char msg[25];

    //start doing shit
    while (1) {
        //read all the accel axes
        acc_read_register(OUT_X_L_A, (unsigned char *) accels, 6);
        acc_read_register(OUT_X_L_A, (unsigned char *) mags, 6);
        acc_read_register(TEMP_OUT_L, (unsigned char *) &temp, 2);

        oled_clear_buffer();

        // chop y so it fits on screen
        if(accels[1] > 16387)
            accels[1] = 16387;
        if(accels[1] < -16387)
            accels[1] = -16387;

        oled_draw_rect_fill(64,64-(accels[0]/512),30,34);  //draw x across screen
        oled_draw_rect_fill(62,66,30,30-(accels[1]/512));  //draw y
        //oled_draw_rect_fill(63,63-(accels[2]/512),20,26);  //draw z
        //sprintf(magmsg, "X: %+5d", mags[0]);
        //sprintf(tempmsg, "Temp: %d", temp);
        //oled_draw_string(0,40,magmsg, 1);     //x-mag
        //oled_draw_string(0,50,tempmsg, 1);     //temp

        oled_update();


        //wait a while, run at 5hz
        _CP0_SET_COUNT(0);
        while (_CP0_GET_COUNT() < 1600000);
    }

}
