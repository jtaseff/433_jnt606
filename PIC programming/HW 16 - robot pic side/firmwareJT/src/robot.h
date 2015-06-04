/*
 * File:   robot.h
 * Author: John
 *
 * Created on June 1, 2015, 12:00 PM
 */

#ifndef ROBOT_H
#define	ROBOT_H



///////////////
////  PWM  ////
///////////////

void robot_PWM_disable();

void robot_PWM_enable();

// set the left and right PWM outputs - range 0 to 1000
void robot_PWM_setLR(int L, int R);




/////////////////
////  PORTS  ////
/////////////////

void robot_config_ports();

// check the status of the hardware button
char robot_get_button();




//////////////////
////  SCREEN  ////
//////////////////

void robot_config_screen();

void robot_screen_soft(char * st);

void robot_screen_comm(char * st);

void robot_screen_PWM(char * st);

void robot_screen_motors(int L, int R);

void robot_screen_motors_none();

void robot_screen_powerdir(int P, int D);

void robot_screen_update();





#endif	/* ROBOT_H */

