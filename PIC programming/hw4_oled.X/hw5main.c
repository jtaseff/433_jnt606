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
#include "hw5_gravity.h"

#include "config_pic32_mx250.h"

/*
 * MAIN
 */
int main(int argc, char** argv) {
    startup(); // run some additional chip config

    //call this to run HW5, endless loop there
    //hw5_function();


    //code below is a test to periodically dim the OLED screen with its command set

    //ANSELBCLR = 0x01;   //don't need for PCB
    TRISBCLR = 0x80;    //moving to PCB, was 0x01
    LATBSET = 0x80;     //same

    oled_init();
    oled_draw_string(0, 0, "My spoon is TOO BIG", 1);
    oled_update();

    while (1) {
        _CP0_SET_COUNT(0);
        while (_CP0_GET_COUNT() < 40000000);

        oled_command(0x81);
        oled_command(0x01);
        LATBINV = 0x80;


        _CP0_SET_COUNT(0);
        while (_CP0_GET_COUNT() < 40000000);

        oled_command(0x81);
        oled_command(0xFF);
        LATBINV = 0x80;
    }




    return (EXIT_SUCCESS);
}

/*
 * extra functions
 */


