#include <stdio.h>
#include <string.h>
#include "oled_control_i2c.h"
#include "fonts.h"

void oled_init() {
    display_init();
}

void oled_command(unsigned char cmd) {
    display_command(cmd);
}

void oled_update() {
    display_draw(); // update/draw the buffer to the actual screen
}

void oled_clear_buffer() { //clear the buffer
    display_clear();
}

void oled_clear_screen() { //clear the buffer and send to the screen
    display_clear();
    display_draw();
}

void oled_pixel(int row, int col, int val) {
    display_pixel_set(row, col, val);
}

void oled_draw_char(int xpos, int ypos, char fontwd, unsigned char c, char color) {
    // draws a single character to the buffer at the given potition (top left)
    // fontwd allows choosing different font sizes, only 5x8px implemented now

    if (fontwd != 5)
        return;

    int fontht = 8;

    if ((xpos < 0) || // check if char will fit on screeen
            (ypos < 0) ||
            (xpos + fontwd > WIDTH) ||
            (ypos + fontht > HEIGHT)) {

        return;
    }

    int iirow = 0;
    int jjcol = 0;
    int line;

    for (jjcol = 0; jjcol < fontwd; jjcol++) {

        line = font5x8sys[5 * (c - 32) + jjcol]; // grab font data

        for (iirow = 0; iirow < fontht; iirow++) {
            display_pixel_set(xpos + jjcol, ypos + iirow, (line >> iirow) & 1);
        }
    }
}

void oled_draw_string(int xpos, int ypos, char * s) {
    // check size/positioning
    // check wrapping - maybe recurse?

    int numchars = strlen(s);  //length of string

    int strcount;
    for (strcount = 0; strcount < numchars; strcount++) {
        oled_draw_char(xpos + strcount * 6, ypos, 5, s[strcount], 1);
    }
}