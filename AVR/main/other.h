#define DEBUG
//#define WAITFORDEBUGGER
#define CONFIGSTART 16
#define SAFETYNUMBER 0x36 /*change this everytime the struct below changes*/


typedef struct{
	unsigned long valid;
	unsigned short light;
	unsigned short clouds;
	unsigned short rain;
	unsigned short temp1;
	unsigned short temp2;
	bool lightning;
} Forecast;
