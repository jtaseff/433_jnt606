
#include "system_config.h"
#include "system_definitions.h"
#include "peripheral/osc/plib_osc.h"


void SYS_CLK_Initialize( const SYS_CLK_INIT const * clkInit )
{
    SYS_DEVCON_SystemUnlock ( );
    
    PLIB_OSC_FRCDivisorSelect( OSC_ID_0, OSC_FRC_DIV_2);
    /* Enable Peripheral Bus 1 */
    PLIB_OSC_PBClockDivisorSet (OSC_ID_0, 0, 1 );







    SYS_DEVCON_SystemLock ( );
}

//******************************************************************************
/* Function:
    inline uint32_t SYS_CLK_SystemFrequencyGet ( void )

  Summary:
    Gets the system clock frequency in Hertz.

  Description:
    This function gets the System clock frequency in Hertz.

  Precondition:
    None.

  Parameters:
    None.

  Returns:
    System clock frequency in Hertz.

  Example:
    <code>
    uint32_t sysClockHz;

    sysClockHz = SYS_CLK_SystemFrequencyGet ( );
    </code>

  Remarks:
 */

inline uint32_t SYS_CLK_SystemFrequencyGet ( void )
{
    return SYS_CLK_FREQ;
}

//******************************************************************************
/* Function:
    inline uint32_t SYS_CLK_PeripheralFrequencyGet ( CLK_BUSES_PERIPHERAL peripheralBus )

  Summary:
    Gets the selected clock peripheral bus frequency in Hertz.

  Description:
    This function gets the selected peripheral bus clock frequency in Hertz.

  Precondition:
    None.

  Parameters:
	peripheralBus - Reference clock bus selection. One of the possible value from
				CLK_BUSES_PERIPHERAL enum. For devices that do not have multiple
				clock channels for Reference clock, CLK_BUS_PERIPHERAL_1 should be
				the selection.

  Returns:
    Clock frequency in Hertz.

  Example:
    <code>
    unsigned long peripheralClockHz;

    peripheralClockHz = SYS_CLK_PeripheralFrequencyGet ( CLK_BUS_PERIPHERAL_5 );
    </code>

  Remarks:
	Most of the devices doesn't have multiple Peripheral clock buses. In that case, 
	pass CLK_USB_PERIPHERAL_1 as the bus number.
 */

inline uint32_t SYS_CLK_PeripheralFrequencyGet ( CLK_BUSES_PERIPHERAL peripheralBus )
{
    return SYS_CLK_BUS_PERIPHERAL_1;
}


//******************************************************************************
/* Function:
    inline uint32_t SYS_CLK_ReferenceClockFrequencyGet ( CLK_BUSES_REFERENCE referenceBus )

  Summary:
    Gets the selected Reference clock bus frequency in Hertz.

  Description:
    This function gets frequency of the selected Reference clock bus in Hertz.

  Precondition:
    None.

  Parameters:
	peripheralBus - Reference clock bus selection. One of the possible value from
				CLK_BUSES_REFERENCE enum. For devices that do not have multiple
				clock channels for Reference clock, CLK_BUS_REFERENCE_1 should be
				the selection.

  Returns:
    Clock frequency in Hz.

  Example:
    <code>
    unsigned long sysClockOutputHz;

    sysClockOutputHz = SYS_CLK_ReferenceClockFrequencyGet ( CLK_BUS_REFERENCE_3 );
    </code>

  Remarks:
 */

inline uint32_t SYS_CLK_ReferenceClockFrequencyGet ( CLK_BUSES_REFERENCE referenceBus )
{
	return 0;
}