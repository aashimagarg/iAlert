const int btnPinRed = 12;//button pin
const int btnPinGreen = 11;

const int redOut = 2;
const int greenOut = 3;

const char emergency[] = "Emergency";
const char updateLocation[] = "Update";

boolean wasPressedRed = false;
boolean wasPressedGreen = false;

void setup() { 
  //setup pins
  pinMode(btnPinRed, INPUT);
  pinMode(btnPinGreen, INPUT);
  pinMode(redOut, OUTPUT);
  pinMode(greenOut, OUTPUT);
  //setup communication
  Serial.begin(115200);
}

void loop() {
  delay(1000);
  int currentButtonStateRed = digitalRead(btnPinRed);//read button state
  int currentButtonStateGreen = digitalRead(btnPinGreen);
  if(wasPressedRed && wasPressedGreen) {
    if(currentButtonStateRed == LOW) {
       digitalWrite(redOut, HIGH);
       digitalWrite(greenOut, LOW);
       wasPressedRed = false;//update the last button state
    }
  } else if(wasPressedRed && !wasPressedGreen) {
    if(currentButtonStateRed == LOW) {
       digitalWrite(redOut, HIGH);
       digitalWrite(greenOut, LOW);
       wasPressedRed = false;//update the last button state
    }
  } else if(!wasPressedRed && wasPressedGreen) {
    if(currentButtonStateGreen == LOW) {
      digitalWrite(greenOut, HIGH);
      digitalWrite(redOut, LOW);
      wasPressedGreen = false;
    }
  }
  if(currentButtonStateRed == HIGH) {
    wasPressedRed = true;
    digitalWrite(redOut, LOW);
  }
  if(currentButtonStateGreen == HIGH) {
    wasPressedGreen = true;
    digitalWrite(greenOut, LOW);
  }

  Serial.print(digitalRead(redOut));
  Serial.print(" ");
  Serial.print(digitalRead(greenOut));
}

void serialEvent(){//if any data was sent
  if(Serial.available() > 0){//and there's at least 1 byte to look at
    char data = Serial.read();//read the data
  }
}
