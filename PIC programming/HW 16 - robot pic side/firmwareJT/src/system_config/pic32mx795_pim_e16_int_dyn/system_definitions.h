

#ifndef _SYS_DEFINITIONS_H
#define _SYS_DEFINITIONS_H


// *****************************************************************************
// *****************************************************************************
// Section: Included Files
// *****************************************************************************
// *****************************************************************************

#include <stddef.h>
#include "system/common/sys_common.h"
#include "system/common/sys_module.h"
#include "system/clk/sys_clk.h"
#include "framework/system/clk/sys_clk_static.h"
#include "system/devcon/sys_devcon.h"
#include "system/int/sys_int.h"
#include "driver/usart/drv_usart.h"
#include "system/ports/sys_ports.h"
#include "system/debug/sys_debug.h"
#include "usb/usb_device.h"
#include "usb/usb_device_cdc.h"


// *****************************************************************************
// *****************************************************************************
// Section: Type Definitions
// *****************************************************************************
// *****************************************************************************

// *****************************************************************************
/* System Objects

  Summary:
    Structure holding the system's object handles

  Description:
    This structure contains the object handles for all objects in the
    MPLAB Harmony project's system configuration.

  Remarks:
    These handles are returned from the "Initialize" functions for each module
    and must be passed into the "Tasks" function for each module.
*/

typedef struct
{
    SYS_MODULE_OBJ  sysDevcon;
    SYS_MODULE_OBJ  drvUsart0;



    SYS_MODULE_OBJ  usbDevObject0;


} SYSTEM_OBJECTS;


// *****************************************************************************
// *****************************************************************************
// Section: extern declarations
// *****************************************************************************
// *****************************************************************************

extern SYSTEM_OBJECTS sysObj;

#endif /* _SYS_DEFINITIONS_H */
/*******************************************************************************
 End of File
*/

