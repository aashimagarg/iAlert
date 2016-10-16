import processing.serial.*;
import processing.serial.*;

void setup(){
  try{
    Serial wifiShield = new Serial(this,"usbserial-DN00HX25",115200);
    wifiShield.bufferUntil('\n');//buffer until a new line is encountered
  } catch(Exception e){
    System.err.println("Error opening serial connection! (check cables and port/baud settings!");
    e.printStackTrace();
  }
}

void draw(){}
void serialEvent(Serial s){
  String[] command = s.readString().trim().split(",");//trim spaces, split to String[] for args
  println(command[0]);//see what we got
  for(String str : command) {
    s.write(str);
  }
} 