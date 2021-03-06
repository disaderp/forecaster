#define D0 0x3F
#define D1 0x06
#define D2 0x5B
#define D3 0x4F
#define D4 0x66
#define D5 0x6D
#define D6 0x7D
#define D7 0x07
#define D8 0x7F
#define D9 0x67
#define Dx 0x40
#define Do 0x5C

int findNum(short num){
	if (num == 0) return D0;
	if (num == 1) return D1;
	if (num == 2) return D2;
	if (num == 3) return D3;
	if (num == 4) return D4;
	if (num == 5) return D5;
	if (num == 6) return D6;
	if (num == 7) return D7;
	if (num == 8) return D8;
	if (num == 9) return D9;
	return Dx;
}

void writeFirstDigit(int num){
	Wire.beginTransmission(0x20);
	Wire.write(0x12);//first digit
	Wire.write(~num);//display digit //active low
	Wire.endTransmission();
}
void writeSecondDigit(int num){
	Wire.beginTransmission(0x20);
	Wire.write(0x13);//second digit
	Wire.write(~num);//display digit //active low
	Wire.endTransmission();
}
