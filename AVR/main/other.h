#define DEBUG
#define WAITFORDEBUGGER
#define CONFIGSTART 16
#define SAFETYNUMBER 0x37


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