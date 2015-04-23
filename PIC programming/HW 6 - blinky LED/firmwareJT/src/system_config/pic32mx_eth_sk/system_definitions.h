

#ifndef _SYS_DEFINITIONS_H
#define _SYS_DEFINITIONS_H

#include <stddef.h>
#include "system/common/sys_common.h"
#include "system/common/sys_module.h"
#include "system/clk/sys_clk.h"
#include "framework/system/clk/sys_clk_static.h"
#include "system/devcon/sys_devcon.h"
#include "system/ports/sys_ports.h"
#include "system/debug/sys_debug.h"

typedef struct {
    SYS_MODULE_OBJ sysDevcon;

} SYSTEM_OBJECTS;


extern SYSTEM_OBJECTS sysObj;

#endif /* _SYS_DEFINITIONS_H */

