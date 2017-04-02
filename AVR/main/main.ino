#include <SoftwareSerial.h>
#include <Wire.h>
#include "display.h"
#include "LED.h"

SoftwareSerial BT(12, 13); // RX, TX
int BTData;

void setup() {
	delay(2000);//let all pheripherals set up

	pinMode(0, OUTPUT);
	pinMode(1, OUTPUT);
	pinMode(2, OUTPUT); //3-8 MUX control

	pinMode(4, OUTPUT);//lightning led

	pinMode(5, OUTPUT);//rain(PWM)

	pinMode(7, OUTPUT);//clouds
	
	BT.begin(9600);
	Wire.begin();
	
	Wire.beginTransmission(0x20);
	Wire.write(0x00); // IODIRA register
	Wire.write(0x00); // set all of port A to outputs
	Wire.endTransmission();
	
	Wire.beginTransmission(0x20);
	Wire.write(0x01); // IODIRB register
	Wire.write(0x00); // set all of port B to outputs
	Wire.endTransmission();
}

void loop() {
	if (BT.available()){
		BTData=BT.read();
		if(BTData=='1') BT.println("TestOK");
	}
	
	writeFirstDigit(D1);
	writeSecondDigit(D5);//display: 15

	updateLED(SUNNY);

	createLightning();

	analogWrite(5, 255/4);//dutycycle 1/4, small rain

	digitalWrite(7, 1); //make clouds
	delay(2000);
	digitalWrite(7, 0);//enough clouds
	
	delay(100);
}
