/* 
 * File:   main.c
 * Author: John
 *
 * Created on May 20, 2015, 11:06 PM
 */

#include <stdio.h>
#include <stdlib.h>
#include <xc.h> 
#include "oled_graphics.h"
#include "config_pic32_mx250.h"
#include "main.h"

int waitii;

int main(int argc, char** argv) {

    //if we're first being powered on, wait for OLED to warm
    if (RCONbits.POR)   
        for (waitii = 0; waitii < 1000000; waitii++) {
        }
    RCONbits.POR = 0;

    //run setup routine for ports and communications
    config();

    while (1) {

        oled_clear_buffer();
        oled_draw_string(0, 0, "Waiting for user...", 1);
        oled_update();

        LATBSET = 0b1000;       //direction forward
        LATBCLR = 0b0010;

        OC1RS = 0;
        OC4RS = 0;

        while (PORTBbits.RB13) {
            //wait until user says go
        }

        int power;
        char msg[20];

        //section
        sprintf(msg, "Forward 50%");
        oled_clear_buffer();
        oled_draw_string(0, 0, msg, 1);
        oled_update();

        OC1RS = 500;
        OC4RS = 500;

        for (waitii = 0; waitii < 10000000; waitii++) {
        }   //wait 1 sec


        //section
        sprintf(msg, "R50 L25%");
        oled_clear_buffer();
        oled_draw_string(0, 0, msg, 1);
        oled_update();

        OC1RS = 500;
        OC4RS = 250;

        for (waitii = 0; waitii < 10000000; waitii++) {
        }   //wait 1 sec


        //section
        sprintf(msg, "R50 L0");
        oled_clear_buffer();
        oled_draw_string(0, 0, msg, 1);
        oled_update();

        OC1RS = 500;
        OC4RS = 0;

        for (waitii = 0; waitii < 10000000; waitii++) {
        }   //wait 1 sec



        //switch direction of right motor
        LATBSET = 0b0010;

        //section
        sprintf(msg, "R50 LB25");
        oled_clear_buffer();
        oled_draw_string(0, 0, msg, 1);
        oled_update();

        OC1RS = 500;
        OC4RS = 250;

        for (waitii = 0; waitii < 10000000; waitii++) {
        }   //wait 1 sec


        //section
        sprintf(msg, "R50 LB50");
        oled_clear_buffer();
        oled_draw_string(0, 0, msg, 1);
        oled_update();

        OC1RS = 500;
        OC4RS = 500;

        for (waitii = 0; waitii < 10000000; waitii++) {
        }   //wait 1 sec
    }

    return (EXIT_SUCCESS);
}

void config() {
    startup();  //extra bit of code to get some things set up
//
//    ANSELBCLR = 0b1010000000001110;     //disable AN for our pins
//    TRISBCLR = 0x8080;              //LED1 and 2 on B7 and B15
//    LATBCLR = 0x8080;               //LEDs off

    oled_init();                    //initialize I2C and configure the OLED


    //configure PWM
    //configure ports we need
    TRISBCLR = 0b11110;           //outputs on ports B1-4
    LATBCLR = 0b11110;              //zero output
    RPB2Rbits.RPB2R = 0b0101;       //enable OC4 onto B2, right motor
    RPB4Rbits.RPB4R = 0b0101;       //enable OC1 onto B4, left motor

    //configure T2 to drive PWM - most defaults are good
    T2CONbits.TCKPS = 0b010;        //prescale 1:4
    PR2 = 999;                      //period 1000 * ps4 * 40MHz = 10kHz
    TMR2 = 0;                       //reset
    T2CONbits.ON = 1;               //turn on

    //configure OC1 and OC4 to drive PWMs
    OC1CONbits.OCM = 0b110;         //PWM without fault pin
    OC1RS = 0x00;                   //buffer zero output
    OC1R = 0x00;                    //zero output
    OC1CONbits.ON = 1;              //enable output

    OC4CONbits.OCM = 0b110;         //PWM
    OC4RS = 0x00;                   //zero
    OC4R = 0x00;                    //zero
    OC4CONbits.ON = 1;              //enable output


}