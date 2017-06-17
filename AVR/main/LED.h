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
	digitalWrite(6, 1);
	delay(50);//@TODO: 3 modes of lightning, rand
	digitalWrite(6, 0);
}
