/* 
 * File:   hw1main.c
 * Author: John Taseff
 *
 * Created on April 1, 2015, 2:17 PM
 */

#include <stdio.h>
#include <stdlib.h>
#include <xc.h> // processor SFR definitions
#include <sys/attribs.h> // __ISR macro
#include "i2c_master.h"
#include "oled_graphics.h"
#include "accel_spi.h"

#include "config_pic32_mx250.h"


/*
 * MAIN
 */
int main(int argc, char** argv) {
    startup(); // run some additional chip config

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



    return (EXIT_SUCCESS);
}






/*
 * extra functions
 */

int startup(void) {
// Startup code to run as fast as possible and get pins back from bad defaults

__builtin_disable_interrupts();

// set the CP0 CONFIG register to indicate that
// kseg0 is cacheable (0x3) or uncacheable (0x2)
// see Chapter 2 "CPU for Devices with M4K Core"
// of the PIC32 reference manual
__builtin_mtc0(_CP0_CONFIG, _CP0_CONFIG_SELECT, 0xa4210583);

// 0 data RAM access wait states
BMXCONbits.BMXWSDRM = 0x0;

// enable multi vector interrupts
INTCONbits.MVEC = 0x1;

// disable JTAG to be able to use TDI, TDO, TCK, TMS as digital
DDPCONbits.JTAGEN = 0;

__builtin_enable_interrupts();
}
