#define DEBUG
#define WAITFORDEBUGGER
#define CONFIGSTART 16
#define SAFETYNUMBER 0x36


typedef struct{
	unsigned long valid;
	unsigned short light;
	unsigned short clouds;
	unsigned short rain;
	unsigned short temp1;
	unsigned short temp2;
	bool lightning;
} Forecast;

void waitForData(SoftwareSerial S){
	while(!S.available()){
		delay(200);
	}
}
