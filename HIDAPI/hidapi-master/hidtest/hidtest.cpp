//HIDAPI microchip test example


#ifdef WIN32
#include <windows.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#include "hidapi.h"
#include <string.h>

#define MAX_STR 255

int main(int argc, char* argv[])
{
	int res;
	unsigned char buf[65];
	wchar_t wstr[MAX_STR];
	hid_device *handle;
	short accx;
	short accy;
	short accz;
	char msg[24];
	int row = 0;
	char butt[10];
	
	printf("Msg >");
	scanf("%s", msg);
	printf("Row >");
	scanf("%i", &row);
	
	// start a data log file
	FILE *accdata;
	accdata = fopen("accdata.txt", "w");

	// Initialize the hidapi library
	res = hid_init();

	// Open the device using the VID, PID,
	// and optionally the Serial number.
	handle = hid_open(0x4d8, 0x3f, NULL);
	printf("opened");

	
	// Toggle LED (cmd 0x80). The first byte is the report number (0x0).
	buf[0] = 0x0;
	buf[1] = 0x80;	// LED cmd
	res = hid_write(handle, buf, 65);
	
	// Send oled string and prep for data output
	buf[0] = 0x0;
	buf[1] = 0x83;	// "get ready" cmd
	buf[2] = (unsigned char) row;
	strcpy((char *) buf+3, msg);
	res = hid_write(handle, buf, 65);
	
	
	int replies = 0;
	while (replies < 10) {
	
		buf[0] = 0x0;
		buf[1] = 0x82;	// data cmd
		res = hid_write(handle, buf, 65);
		

		res = hid_read(handle, buf, 65);
		accx = (buf[1] << 8) | buf[2];
		accy = (buf[3] << 8) | buf[4];
		accz = (buf[5] << 8) | buf[6];
		printf("x: %+06d  y: %+06d  z: %+06d\n", accx, accy, accz);	//to screen
		fprintf(accdata, "%d %d %d\n", accx, accy, accz);			//to data log
	
		replies++;
	
	}
	
	// send a "done" cmd
	buf[0] = 0x0;
	buf[1] = 0x84;
	res = hid_write(handle, buf, 65);
	
	buf[0] = 0x0;
	buf[1] = 0x80;
	res = hid_write(handle, buf, 65);
	
	//close data log file
	fclose(accdata);
	printf("Data written to file.\n");
	
	// Finalize the hidapi library
	res = hid_exit();

	return 0;
}
