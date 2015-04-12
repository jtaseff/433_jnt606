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

/*
 * Start DEVCFGs
 */


// our new CFG set
// DEVCFG0
#pragma config DEBUG = OFF // no debugging
#pragma config JTAGEN = OFF // no jtag
#pragma config ICESEL = ICS_PGx3 // use PGED1 and PGEC1 // using 3
#pragma config PWP = OFF // no write protect
#pragma config BWP = OFF // not boot write protect
#pragma config CP = OFF // no code protect

// DEVCFG1
#pragma config FNOSC = PRIPLL // use primary oscillator with pll
#pragma config FSOSCEN = OFF // turn off secondary oscillator
#pragma config IESO = OFF // no switching clocks
#pragma config POSCMOD = HS // high speed crystal mode
#pragma config OSCIOFNC = OFF // free up secondary osc pins
#pragma config FPBDIV = DIV_1 // divide CPU freq by 1 for peripheral bus clock
#pragma config FCKSM = CSDCMD // do not enable clock switch
#pragma config WDTPS = PS1 // slowest wdt
#pragma config WINDIS = OFF // no wdt window
#pragma config FWDTEN = OFF // wdt off by default
#pragma config FWDTWINSZ = WISZ_25 // wdt window at 25%

// DEVCFG2 - get the CPU clock to 40MHz
#pragma config FPLLIDIV = DIV_2 // divide input clock to be in range 4-5MHz
#pragma config FPLLMUL = MUL_20 // multiply clock after FPLLIDIV
#pragma config UPLLIDIV = DIV_2 // divide clock after FPLLMUL
#pragma config UPLLEN = ON // USB clock on
#pragma config FPLLODIV = DIV_2 // divide clock by 2 to output on pin

// DEVCFG3
#pragma config USERID = 11 // some 16bit userid
#pragma config PMDL1WAY = ON // not multiple reconfiguration, check this
#pragma config IOL1WAY = ON // not multimple reconfiguration, check this
#pragma config FUSBIDIO = ON // USB pins controlled by USB module
#pragma config FVBUSONIO = ON // controlled by USB module





/*
 * MAIN
 */
int main(int argc, char** argv) {
    startup(); // run some additional chip config

    // LED output
    ANSELBCLR = 0x01;
    TRISBCLR = 0x01;
    LATBSET = 0x01;

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
