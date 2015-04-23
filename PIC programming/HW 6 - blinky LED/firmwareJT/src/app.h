
#ifndef _APP_H
#define _APP_H


#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdlib.h>
#include "system_config.h"

/* Modify this value to alter the LED blink rate */
#define APP_LED_BLINK_DELAY     (SYS_CLK_FREQ / 100)

typedef enum {
    /* Increment the counter */
    APP_STATE_COUNT,

    /* Toggle the LED */
    APP_STATE_BLINK_LED

} APP_STATES;

typedef struct {
    /* The application's current state */
    APP_STATES state;

} APP_DATA;


void APP_Initialize(void);

void APP_Tasks(void);


#endif /* _APP_H */

