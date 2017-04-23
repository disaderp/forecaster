#include <SoftwareSerial.h>
#include <Wire.h>
#include <Time.h>
#include <TimeLib.h>
#include <DS1307RTC.h>
#include "display.h"
#include "LED.h"
#include "other.h"

SoftwareSerial BT(12, 13); // RX, TX
Forecast fdata[10];//@TODO: EEPROM
unsigned short current = 0;
bool data = false;

//timers
int starttime;
short rain;
short clouds;
short lightning;
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
			while(!Serial){}
			Serial.println("Debugger connected.");
		#endif
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
	#endif

	if(!RTC.get() || RTC.get() < 1000000000 || RTC.get() > 1000086400){ //24h -> s
		#ifdef DEBUG
			Serial.print("(RTC)No time: ");
			Serial.println(RTC.get());
		#endif
		RTC.set(1000000000);//if no time - set to beginning
	}else{
		current = (RTC.get()-1000000000) / fdata[0].valid * 3600; //h -> s
		//prevtime = (RTC.get()
		if (current < 10) {data = true;}
		#ifdef DEBUG
			Serial.print("(RTC)Current: ");
			Serial.println(RTC.get()-1000000000);
			Serial.print("(RTC)Data: ");
			Serial.println(current);
		#endif
	}
}

void loop() {
	if(BT.available()){
		char d = BT.read();
		if(d == 'E'){
			BT.write("ok");
			unsigned short entry = 0;
			while(true){
				waitForData(BT);
				d = BT.read();
				if(d == 'N') {entry = 0; continue;}
				else if(d == 'R') {RTC.set(1000000000); break;}
				else if(d != 'A') {BT.write("error"); continue;}

				waitForData(BT);
				bool daytime = BT.parseInt(); BT.read();
				fdata[entry].clouds = BT.parseInt(); BT.read();
				fdata[entry].rain = BT.parseInt(); BT.read();
				fdata[entry].lightning = BT.parseInt(); BT.read();
				
				short temp = BT.parseInt(); BT.read();
				if(temp<=-10) {fdata[entry].temp1 = findNum(temp / -10); fdata[entry].temp2 = findNum(-(temp % -10));}//@TODO: check
				else if(temp<0) {fdata[entry].temp1 = Dx; fdata[entry].temp2 = findNum(-temp);}
				else if(temp<10) {fdata[entry].temp1 = 0; fdata[entry].temp2 = findNum(temp);}
				else {fdata[entry].temp1 = findNum(temp / 10); fdata[entry].temp2 = findNum(temp % 10);}
				
				fdata[entry].valid = BT.parseInt(); BT.read();
				if(daytime == true) {
					fdata[entry].light = BT.parseInt(); BT.read();
				}else{
					fdata[entry].light = fdata[entry].clouds < 80 ? SUNNY : CLOUDY;//@TODO: check
				}
				
				#ifdef DEBUG
					Serial.print("(BT)DataInput: ");
					printf("L:%d C:%d R:%d L:%d T:%d%d V:%d\n", fdata[entry].light, fdata[entry].clouds, fdata[entry].rain, fdata[entry].lightning, fdata[entry].temp1, fdata[entry].temp2, fdata[entry].valid);
				#endif
				++entry;
				BT.write("ok");
			}
			data = true;
			starttime = 1000000000;
			current = 0;
		}
	}
	
	if(!data){
		writeFirstDigit(Dx);
		writeSecondDigit(Dx);
		#ifdef DEBUG
			Serial.print("(RTC)Time elapsed: ");
			Serial.println(RTC.get()-1000000000);
		#endif
		delay(4000);
	}else{
		writeFirstDigit(fdata[current].temp1);
		writeSecondDigit(fdata[current].temp2);

		updateLED(fdata[current].light);
		if (fdata[current].lightning) createLightning();

		analogWrite(5, fdata[current].rain);

		if(fdata[current].clouds > 0){
			digitalWrite(7, 1);
			delay(fdata[current].clouds * 20);
			digitalWrite(7, 0);
		}
		

		//timeout check
		if(RTC.get() - starttime > fdata[current].valid){
			++current;
			#ifdef DEBUG
				Serial.print("(DATA)Timeout - Next data: ");
				Serial.println(current);
			#endif
		}
		//delay(100);//@TODO timers
	}
}