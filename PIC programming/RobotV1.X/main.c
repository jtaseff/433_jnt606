/* 
 * File:   main_init.c
 * Author: John
 *
 * Created on June 1, 2015, 11:51 AM
 */

#include <stdio.h>
#include <stdlib.h>
#include <xc.h>
#include "config_pic32_mx250.h"
#include "main.h"

/*
 * 
 */
int main(int argc, char** argv) {
    startup();
    config_robot();
    config_usb();

    return (EXIT_SUCCESS);
}



void config_robot() {
    //if we're first being powered on, wait for OLED to warm

    


}

void config_USB() {

}