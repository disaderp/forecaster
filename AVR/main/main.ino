#include <SoftwareSerial.h>
#include <Wire.h>
#include <Time.h>
#include <TimeLib.h>
#include <DS1307RTC.h>
#include <EEPROM.h>
#include "display.h"
#include "LED.h"
#include "other.h"

SoftwareSerial BT(12, 13); // RX, TX
Forecast fdata[10];
unsigned short current = 0;
unsigned short ecount = 0;
bool data = false;

//timers
unsigned short rain;
unsigned short clouds;
unsigned short lightning;
//timers


void setup() {
	delay(2000);//setup time

	pinMode(2, OUTPUT);
	pinMode(3, OUTPUT);
	pinMode(4, OUTPUT); //3->8 MUX control

	pinMode(5, OUTPUT);//rain(PWM)
	pinMode(6, OUTPUT);//lightning led
	pinMode(7, OUTPUT);//clouds
	
	BT.begin(9600);
	#ifdef DEBUG
		Serial.begin(9600);//USB debugging
		#ifdef WAITFORDEBUGGER
			Serial.println("(USB)Debugger mode - press any key to start...");
			while(!Serial.available()){}
		#endif
		Serial.println("(USB)Debugger connected.");
	#endif

	Wire.begin();

	Wire.beginTransmission(0x20);
	Wire.write(0x00); // IODIRA register
	Wire.write(0x00); // set all of port A to outputs
	Wire.endTransmission();
	Wire.beginTransmission(0x20);
	Wire.write(0x01); // IODIRB register
	Wire.write(0x00); // set all of port B to outputs
	Wire.endTransmission();
	#ifdef DEBUG
		Serial.println("(7SEG)Module activated");
		Serial.println("(RNG)Setting seed");
	#endif
	srand(RTC.get());

	if(!RTC.get() || RTC.get() < 1000000000 || RTC.get() > 1000086400){ //24h -> s
		#ifdef DEBUG
			Serial.print("(RTC)No time: ");
			Serial.println(RTC.get());
		#endif
		RTC.set(1000000000);//if no time - set to beginning
		EEPROM.write(CONFIGSTART-1, 0x00);//invalidate previous data
	}else{
		if(readConf()){
			#ifdef DEBUG
				Serial.println("(EEPROM)Previous data read successful");
			#endif
		}else{
			#ifdef DEBUG
				Serial.println("(EEPROM)Previous data read unsuccessful");
			#endif
		
		}
		for(int i = 0; i < 10; i++){
			if(fdata[0].valid > RTC.get()){
				current = i;
				#ifdef DEBUG
					Serial.print("(EEPROM)Found current: ");
					Serial.println(current);
				#endif
				data = true;
				ecount = 10;//temp
				break;
			}
		}
		if (!data){
			#ifdef DEBUG
				Serial.println("(EEPROM)Cannot find current - timeout");
			#endif
			EEPROM.write(CONFIGSTART-1, 0x00);//invalidate previous data
		}
	}
	#ifdef DEBUG
		Serial.println("(SETUP)Completed");
	#endif
}

void loop() {
	if(BT.available()){
		char d = BT.read();
		if(d == 'E'){
			#ifdef DEBUG
				Serial.println("(BT)Input mode:");
			#endif
			unsigned short entry = 0;
			while(true){
				d = BT.read();
				if(d == 'N') {entry = 0; ecount = 0; continue;}
				else if(d == 'R') {
					RTC.set(1000000000); 
					ecount = entry; 
					#ifdef DEBUG 
						Serial.println("(BT)Connection closed successfully"); 
					#endif 
					break;}
				else if(d != 'A') {BT.write("ERROR\0"); break;}
				
				bool daytime = BT.parseInt(); BT.read();//remove semicolon
				fdata[entry].clouds = BT.parseInt(); BT.read();//...
				fdata[entry].rain = BT.parseInt(); BT.read();
				fdata[entry].lightning = BT.parseInt(); BT.read();
				
				short temp = BT.parseInt(); BT.read();
				if(temp<=-10) {fdata[entry].temp1 = findNum(temp / -10); fdata[entry].temp2 = findNum(-(temp % -10));}
				else if(temp<0) {fdata[entry].temp1 = Dx; fdata[entry].temp2 = findNum(-temp);}
				else if(temp<10) {fdata[entry].temp1 = 0; fdata[entry].temp2 = findNum(temp);}
				else {fdata[entry].temp1 = findNum(temp / 10); fdata[entry].temp2 = findNum(temp % 10);}
				
				fdata[entry].valid = BT.parseInt() + (entry == 0 ? 1000000000 : fdata[entry-1].valid); BT.read();
				if(daytime == true) {
					fdata[entry].light = BT.parseInt(); BT.read();
					if (fdata[entry].light > 1) fdata[entry].light = 1;//HARDWARE FAILURE WONTFIX
				}else{
					fdata[entry].light = fdata[entry].clouds < 80 ? SUNNY : CLOUDY;//@TODO: check
				}
				
				#ifdef DEBUG
					Serial.print("(BT)DataInput: ");
					char buffer[100];
					sprintf(buffer, "L:%hu C:%hu R:%hu L:%hhd T:%hu%hu V:%lu\n", fdata[entry].light, fdata[entry].clouds, fdata[entry].rain, fdata[entry].lightning, fdata[entry].temp1, fdata[entry].temp2, fdata[entry].valid);
					Serial.print(buffer);
				#endif
				++entry;
			}
			BT.write("OK\0");
			data = true;
			writeConf();
			#ifdef DEBUG
				Serial.println("(EEPROM)Wrote config");
			#endif
			current = 0;
		}
	}
	
	if(!data){
		writeFirstDigit(Do);
		writeSecondDigit(Do);
		#ifdef DEBUG
			Serial.println("(DATA)No data");
			Serial.print("(RTC)Time elapsed: ");
			Serial.println(RTC.get()-1000000000);
		#endif
		digitalWrite(7, 1);
		delay(4000); 
		writeFirstDigit(Do);
		writeSecondDigit(Dx);
		delay(200);
	}else{
		writeFirstDigit(fdata[current].temp1);
		writeSecondDigit(fdata[current].temp2);

		updateLED(fdata[current].light);

		//timers
		if (fdata[current].lightning){
			if(lightning > 100){//every 10 sec //@TODO: check timing
				createLightning();
				lightning = 0;
				#ifdef DEBUG
					Serial.println("(DISPLAY)Make lightning");
				#endif
			}else{
				++lightning;
			}
		}

		if(fdata[current].rain > 0){
			if(rain > 50) {//every 5 seconds //@TODO: check timing
				digitalWrite(5, 1);
				rain = 0;
				#ifdef DEBUG
					Serial.println("(DISPLAY)Make rain");
				#endif
			}else if(rain == fdata[current].rain){//@TODO: check timing
				digitalWrite(5, 0);
				++rain;
			}else{
				++rain;
			}
		}

		if(fdata[current].clouds > 0){
			if(clouds > 200){//every 20 seconds //@TODO: check timing
				digitalWrite(7, 1);
				clouds = 0;
				#ifdef DEBUG
					Serial.println("(DISPLAY)Make clouds");
				#endif
			}else if(clouds == 2*fdata[current].clouds){ //@TODO: check timing
				digitalWrite(7, 0);
				++clouds;
			}else{
				++clouds;
			}			
		}
		//timers

		//timeout check
		if(RTC.get() > fdata[current].valid){
			//reset outputs
			digitalWrite(5,0);
			digitalWrite(7,0);
			updateLED(0);
			//
			++current;
			if (current == ecount){
				data = false;
				EEPROM.write(CONFIGSTART-1, 0x00);//invalidate previous data
				#ifdef DEBUG
					Serial.println("(DATA)Timeout: No new data");
				#endif
			}
			#ifdef DEBUG
				Serial.print("(DATA)Timeout - Next data: ");
				Serial.println(current);
			#endif
		}
		//timeout check

		delay(100);
	}
}

bool readConf(){
	if(EEPROM.read(CONFIGSTART-1) == SAFETYNUMBER){
		for (unsigned int t=0; t < sizeof(fdata); t++)
			*((char*)&fdata + t) = EEPROM.read(CONFIGSTART + t);
		return true;
	}else{
		return false;
	}
}

void writeConf(){
	EEPROM.write(CONFIGSTART-1, SAFETYNUMBER);
	for (unsigned int t=0; t<sizeof(fdata); t++)
		EEPROM.write(CONFIGSTART + t, *((char*)&fdata + t));
}
