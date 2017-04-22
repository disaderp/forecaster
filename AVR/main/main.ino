#include <SoftwareSerial.h>
#include <Wire.h>
#include <Time.h>
#include <TimeLib.h>
#include <DS1307RTC.h>
#include "display.h"
#include "LED.h"

SoftwareSerial BT(12, 13); // RX, TX
Forecast fdata[10];
unsigned short current = 0;
bool data = false;

void setup() {
	delay(2000);//let all pheripherals set up

	pinMode(2, OUTPUT);
	pinMode(3, OUTPUT);
	pinMode(4, OUTPUT); //3-8 MUX control

	pinMode(5, OUTPUT);//rain(PWM)
	pinMode(6, OUTPUT);//lightning led
	pinMode(7, OUTPUT);//clouds
	
	BT.begin(9600);
	Serial.begin(9600);//USB debugging
	Wire.begin();
	
	Wire.beginTransmission(0x20);
	Wire.write(0x00); // IODIRA register
	Wire.write(0x00); // set all of port A to outputs
	Wire.endTransmission();
	Wire.beginTransmission(0x20);
	Wire.write(0x01); // IODIRB register
	Wire.write(0x00); // set all of port B to outputs
	Wire.endTransmission();

	if(!RTC.get() || RTC.get() < 1000000000 || RTC.get() > 1000086400){ //24h -> s
		RTC.set(1000000000);//if no time - set to beginning
	}else{
		current = (RTC.get()-1000000000) / fdata[0].valid * 3600; //h * s
		if (current < 10) {data = true;}
	}
}

void loop() {
	if(BT.available()){
		char d = BT.read();
		if(d == 'E'){
			BT.write("ok");
			unsigned short entry = 0;
			while(true){
				waitForData();
				d = BT.read();
				if(d == 'N') {entry = 0; continue;}
				else if(d == 'R') {RTC.set(1000000000); break;}
				else if(d != 'A') {BT.write("error"); continue;}

				waitForData();
				bool daytime = BT.parseInt(); BT.read();
				fdata[entry].clouds = BT.parseInt(); BT.read();
				fdata[entry].rain = BT.parseInt(); BT.read();
				fdata[entry].lightning = BT.parseInt(); BT.read();
				
				short temp = BT.parseInt(); BT.read();
				if(temp<=-10) {fdata[entry].temp1 = findNum(temp / -10); fdata[entry].temp2 = findNum(-(temp % -10));}
				else if(temp<0) {fdata[entry].temp1 = Dx; fdata[entry].temp2 = findNum(-temp);}
				else if(temp<10) {fdata[entry].temp1 = 0; fdata[entry].temp2 = findNum(temp);}
				else {fdata[entry].temp1 = findNum(temp / 10); fdata[entry].temp2 = findNum(temp % 10);}
				
				fdata[entry].valid = BT.parseInt(); BT.read();
				if(daytime == true) {
					fdata[entry].light = BT.parseInt(); BT.read();
				}else{
					fdata[entry].light = fdata[entry].clouds < 80 ? SUNNY : CLOUDY;
				}
				
				++entry;
				BT.write("ok");
			}
			data = true;
			current = 0;
		}
	}
	
	if(!data){
		writeFirstDigit(Dx);
		writeSecondDigit(Dx);
		Serial.print("Time elapsed: ");
		Serial.println(RTC.get()-1000000000);
		delay(3000);
	}else{
		writeFirstDigit(fdata[current].temp1);
		writeSecondDigit(fdata[current].temp2);

		updateLED(fdata[current].light);
		if (fdata[current].lightning) createLightning();

		analogWrite(5, fdata[current].rain);//dutycycle 1/4, small rain

		if(fdata[current].clouds > 0){
			digitalWrite(7, 1); //make clouds
			delay(fdata[current].clouds * 20);
			digitalWrite(7, 0);//enough clouds
		}
	
		//delay(100);//TODO timers
	}
}

void waitForData(){
	while(!BT.available()){
		delay(200);//nothing
	}
}
