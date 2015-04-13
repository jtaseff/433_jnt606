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
#include "oled_control_i2c.h"

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

    _CP0_SET_COUNT(0);
    while(_CP0_GET_COUNT() < 4000000);
    //i2c_master_setup();
    display_init();
    display_clear();
    display_pixel_set(5,5,1);
    display_pixel_set(4,4,0);
    display_draw();
    display_command(0xA5);

    while(1) {
        _CP0_SET_COUNT(0);
        LATBINV = 0x01;
        while (_CP0_GET_COUNT() < 10000000);
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
