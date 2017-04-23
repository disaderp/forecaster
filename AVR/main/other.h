#include <stdarg.h>

#define DEBUG
#define WAITFORDEBUGGER


typedef struct{
	unsigned short light;
	unsigned short clouds;
	unsigned short rain;
	bool lightning;
	short temp1;
	short temp2;
	unsigned int valid;
} Forecast;

void waitForData(SoftwareSerial S){
	while(!S.available()){
		delay(200);
	}
}

void printf(char *fmt, ... ){
        char buf[128]; // resulting string limited to 128 chars
        va_list args;
        va_start (args, fmt );
        vsnprintf(buf, 128, fmt, args);
        va_end (args);
        Serial.print(buf);
}