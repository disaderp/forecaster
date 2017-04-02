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

writeFirstDigit(int num){
	Wire.beginTransmission(0x20);
	Wire.write(0x12);//first digit
	Wire.write(num);//display digit
	Wire.endTransmission();
}
writeSecondDigit(int num){
	Wire.beginTransmission(0x20);
	Wire.write(0x13);//second digit
	Wire.write(num);//display digit
	Wire.endTransmission();
}
