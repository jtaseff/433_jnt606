/* 
 * File:   hw1main.c
 * Author: John Taseff
 *
 * Created on April 1, 2015, 2:17 PM
 */

#include <stdio.h>
#include <stdlib.h>
#include<xc.h> // processor SFR definitions
#include<sys/attribs.h> // __ISR macro

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


// function definitions
int readADC(void);





/*
 * MAIN
 */
int main(int argc, char** argv) {
    // run some additional chip config
    startup();

    // set up I/O pins
    // LED output on RB0 and USER button as input on RB2
    ANSELBCLR = 0x05; // RB0 and RB2 set digital
    TRISBCLR = 0x81;  // RB0 and RPB7 as output
    LATBSET = 0x01;   // set RB0

    // set up analog out
    // set up timer2
    T2CONbits.TCKPS = 2;    //scale 1:4 for 10 MHz
    PR2 = 9999;              //count to 1000 for 1 kHz
    TMR2 = 0;               //reset
    T2CONbits.ON = 1;       //turn on
    // set up OC1
    OC1CON = 0x0000;          //disable for setup
    OC1RS = 5000;              //initial set duty 0%
    OC1R = 5000;               //init duty 0
    OC1CONbits.OCM = 0b110;   //PWM mode
    RPB7Rbits.RPB7R = 0b0101; //enable OC1 onto RPB7
    OC1CONbits.ON = 1;        //enable

    // set up analog input
    AD1CON3bits.ADCS = 3; //clock x8?
    AD1CHSbits.CH0SA = 3; //select AN3 for ch0
    AD1CON1bits.ADON = 1;

    //variables
    int ADval = 0;

    while (1) {
        _CP0_SET_COUNT(0);
        LATBINV = 0x01;


        while (_CP0_GET_COUNT() < 10000000) {
            //delay 0.5s for blink

            //while we wait, read pot and set OC duty
            ADval = readADC(); // read AD at pre-set port
            OC1RS = ADval * 10; //set PWM duty (scaled)


            // if USER button pushed, toggle LED1 super fast
            if (PORTBbits.RB2 == 0)
                LATBINV = 0x01;
        }
    }
    
//    while (1) {
//        _CP0_SET_COUNT(0); // set core timer to 0, remember it counts at half the CPU clock
//        LATBINV = some_pin; // invert a pin
//
//    // wait for half a second, setting LED brightness to pot angle while waiting
//        while (_CP0_GET_COUNT() < 10000000) {
//            val = readADC();
//            OC1RS = val * normalize_to_pr;
//            if (push_button_pin == 1) {
//                // nothing
//            } else {
//                LATBINV = some_pin;
//        }
//    }
//    }
//

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




int readADC(void) {


    //sample code provided
    //there's definitely a more correct way to do this
    int elapsed = 0;
    int finishtime = 0;
    int sampletime = 20;
    int a = 0;

    AD1CON1bits.SAMP = 1;
    elapsed = _CP0_GET_COUNT();
    finishtime = elapsed + sampletime;
    while (_CP0_GET_COUNT() < finishtime) {
    }
    AD1CON1bits.SAMP = 0;
    while (!AD1CON1bits.DONE) {
    }
    a = ADC1BUF0;
    return a;
}