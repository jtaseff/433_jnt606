#include "robot.h"
#include "oled_graphics.h"
#include <xc.h>
#include <plib.h>



///////////////
////  PWM  ////
///////////////


void robot_PWM_disable() {
    robot_PWM_setLR(0, 0);
    OC1CONbits.ON = 0;
    OC4CONbits.ON = 0;
}

void robot_PWM_enable() {
    robot_PWM_setLR(0, 0);
    OC1RS = 0;
    OC1R = 0;
    OC1CONbits.ON = 1;
    OC4RS = 0;
    OC4R = 0;
    OC4CONbits.ON = 1;
}


// set the left and right PWM outputs - range 0 to 1000

void robot_PWM_setLR(int L, int R) {
    OC4RS = L;
    OC1RS = R;
}

//robot_PWM_setPD(int P, int D) {
//
//}




/////////////////
////  PORTS  ////
/////////////////

void robot_config_ports() {
    //get rid of stupid AN in
    ANSELB = 0;
    TRISBCLR = 0x8080;              //outputs on LEDs at B7 and B15
    LATBCLR = 0x8080;               //LEDs off


    //configure PWM
    //configure ports we need
    TRISBCLR = 0b11110;             //outputs on ports B1-4
    LATBCLR = 0b11110;              //zero output
    RPB2Rbits.RPB2R = 0b0101;       //enable OC4 onto B2, right motor
    RPB4Rbits.RPB4R = 0b0101;       //enable OC1 onto B4, left motor

    LATBSET = 0b1000;               //set direction forward

    //configure T2 to drive PWM - most defaults are good
    T2CONbits.TCKPS = 0b100;        //prescale 1:16
    PR2 = 255;                      //period 255 * ps16 * 40MHz = ~~10kHz
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


// check the status of the hardware button

char robot_get_button() {
    return !PORTBbits.RB13;
}




//////////////////
////  SCREEN  ////
//////////////////

void robot_config_screen() {
    int waitii;
    if (RCONbits.POR)
        for (waitii = 0; waitii < 1000000; waitii++) {
        }
    RCONbits.POR = 0;
    oled_init();
    robot_screen_soft("initializing");
    robot_screen_update();
}

int motor_L = 0;
int motor_R = 0;
int motor_power = 0;
int motor_dir = 0;

void robot_screen_soft(char * st) {
    oled_draw_rect_fill(4, 127, 0, 8, 0);
    oled_draw_string(4, 0, st, 1);
}

void robot_screen_comm(char * st) {
    oled_draw_rect_fill(4, 127, 10, 18, 0);
    oled_draw_string(4, 10, st, 1);
}

void robot_screen_PWM(char * st) {
    oled_draw_rect_fill(4, 127, 20, 28, 0);
    oled_draw_string(4, 20, st, 1);
}

void robot_screen_motors(int L, int R) {
    oled_draw_rect_fill(0, 127, 48, 63, 0);
    oled_draw_rect_fill(31-(L/8), 31+(L/8), 48, 64, 1);
    oled_draw_rect_fill(95-(R/8), 95+(R/8), 48, 64, 1);
}

void robot_screen_motors_none() {
    oled_draw_rect_fill(0, 127, 48, 63, 0);
}


void robot_screen_update() {

    oled_update();

}