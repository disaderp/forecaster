#define NEWMOON 0
#define FULLMOON 1
#define FQUARTER 2
#define LQUARTER 3
#define SUNNY 4
#define CLOUDY 5

void updateLED(int light){
	switch(light){
		case NEWMOON:
			digitalWrite(2, 0);
			digitalWrite(3, 0);
			digitalWrite(4, 0);
			break;
		case FULLMOON:
			digitalWrite(2, 1);
			digitalWrite(3, 0);
			digitalWrite(4, 0);
			break;
		case FQUARTER:
			digitalWrite(2, 1);
			digitalWrite(3, 1);
			digitalWrite(4, 0);
			break;
		case LQUARTER:
			digitalWrite(2, 0);
			digitalWrite(3, 1);
			digitalWrite(4, 0);
			break;
		case SUNNY:
			digitalWrite(2, 0);
			digitalWrite(3, 1);
			digitalWrite(4, 1);
			break;
		case CLOUDY:
			digitalWrite(2, 1);
			digitalWrite(3, 1);
			digitalWrite(4, 1);
			break;
		default:
			digitalWrite(2, 0);
			digitalWrite(3, 0);
			digitalWrite(4, 0);
			break;
	}
}

void createLightning(){
	unsigned short rn = rand() % 3;
	if(rn == 0){
		digitalWrite(6, 1);
		delay(45);
		digitalWrite(6, 0);
	}else if(rn == 1){
		digitalWrite(6, 1);
		delay(20);
		digitalWrite(6, 0);
		delay(20);
		digitalWrite(6, 1);
		delay(20);
		digitalWrite(6, 0);
		delay(20);
		digitalWrite(6, 1);
		delay(50);
		digitalWrite(6, 0);
	}else if(rn == 2){
		digitalWrite(6, 1);
		delay(50);
		digitalWrite(6, 0);
		delay(50);
		digitalWrite(6, 1);
		delay(20);
		digitalWrite(6, 0);
		delay(20);
		digitalWrite(6, 1);
		delay(20);
		digitalWrite(6, 0);
	}
	
}
