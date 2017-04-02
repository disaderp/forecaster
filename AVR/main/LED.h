#define NEWMOON 0
#define FULLMOON 1
#define FQUARTER 2
#define LQUARTER 3
#define SUNNY 4
#define CLOUDY 5

void updateLED(int light){
	switch(light){
		case NEWMOON:
			digitalWrite(0, 0);
			digitalWrite(1, 0);
			digitalWrite(2, 0);
			break;
		case FULLMOON:
			digitalWrite(0, 1);
			digitalWrite(1, 0);
			digitalWrite(2, 0);
			break;
		case FQUARTER:
			digitalWrite(0, 1);
			digitalWrite(1, 1);
			digitalWrite(2, 0);
			break;
		case LQUARTER:
			digitalWrite(0, 0);
			digitalWrite(1, 1);
			digitalWrite(2, 0);
			break;
		case SUNNY:
			digitalWrite(0, 0);
			digitalWrite(1, 1);
			digitalWrite(2, 1);
			break;
		case CLOUDY:
			digitalWrite(0, 1);
			digitalWrite(1, 1);
			digitalWrite(2, 1);
			break;
		default:
			digitalWrite(0, 0);
			digitalWrite(1, 0);
			digitalWrite(2, 0);
			break;
	}
}

void createLightning(){
	digitalWrite(4, 1);
	delay(50);
	digitalWrite(4, 0);
}

