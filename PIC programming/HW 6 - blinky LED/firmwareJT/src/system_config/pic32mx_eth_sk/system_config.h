
#ifndef _SYSTEM_CONFIG_H
#define _SYSTEM_CONFIG_H

/* This is a temporary workaround for an issue with the peripheral library "Exists"
   functions that causes superfluous warnings.  It "nulls" out the definition of
   The PLIB function attribute that causes the warning.  Once that issue has been
   resolved, this definition should be removed. */
#define _PLIB_UNSUPPORTED

#include "bsp_config.h"


#define SYS_VERSION_STR           "1.03"
#define SYS_VERSION               10300

// *****************************************************************************
/* Clock System Service Configuration Options
*/
#define SYS_CLK_FREQ                        40000000ul
#define SYS_CLK_BUS_PERIPHERAL_1            40000000ul
#define SYS_CLK_UPLL_BEFORE_DIV2_FREQ       7999992ul
#define SYS_CLK_CONFIG_PRIMARY_XTAL         8000000ul
#define SYS_CLK_CONFIG_SECONDARY_XTAL       32768ul
   
/*** Ports System Service Configuration ***/

#define SYS_PORT_AD1PCFG        ~0xffff
#define SYS_PORT_CNPUE          0x0
#define SYS_PORT_CNEN           0x0
  

//#define SYS_PORT_D_TRIS         0xfff8
//#define SYS_PORT_D_LAT          0x0
//#define SYS_PORT_D_ODC          0x0

//JT - make these for our ports
#define SYS_PORT_B_TRIS         0xFFFC
#define SYS_PORT_B_LAT          0x0000
#define SYS_PORT_B_ODC          0x0000


/*** Console System Service Configuration DISABLED ***/

#define SYS_CONSOLE_MESSAGE(message)
#define SYS_CONSOLE_PRINT(fmt, ...)


/*** Command Processor System Service Configuration DISABLED ***/

#define SYS_CMD_MESSAGE(message)
#define SYS_CMD_PRINT(fmt, ...)
#define SYS_CMD_READY_TO_READ()



#endif // _SYSTEM_CONFIG_H

