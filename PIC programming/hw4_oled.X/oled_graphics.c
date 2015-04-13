#include "oled_control_i2c.h"

void oled_init() {
    display_init();
}

void oled_command(unsigned char cmd) {
    display_command(cmd);
}

void oled_update() {
    display_draw();     // update/draw the buffer to the actual screen
}

void oled_clear_buffer() {      //clear the buffer
    display_clear();
}

void oled_clear_screen() {      //clear the buffer and send to the screen
    display_clear();
    display_draw();
}

void oled_pixel(int row, int col, int val) {
    display_pixel_set(row, col, val);
}


