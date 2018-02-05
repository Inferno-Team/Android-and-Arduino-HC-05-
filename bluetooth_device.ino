#include <SoftwareSerial.h>       //import Software Serial library
#include"DHT.h"
int _pins[]={2,3,4,5,6,8,9,12,13}; //pins to remote from phone
SoftwareSerial myStream(10,11);     //pins for Rx and Tx respectively
String command = "";              //read and store user's choice
boolean done = false;
#define DHTTYPE DHT11
#define DHTPIN A0
#define LEDPIN 7 // LED To know if data sent or not
String _pin="";
String _state="";
DHT dht(DHTPIN,DHTTYPE);
void setup() {
  Serial.begin(57600);
  myStream.begin(9600);
  dht.begin();
  pinMode(LEDPIN,OUTPUT);
  for(int i=0;i<9;i++){
    pinMode(_pins[i],OUTPUT);
  }
}
void getMess(){
  command="";
  while (myStream.available()){    //if the Bluetooth client is available
    char ch = myStream.read();    //read data from stream
    command = command + ch;       //create string command
    delay(3);
  }
}
void loop() {
  getMess();
  if (!(myStream.available())){
    done = false;
  }
  if (command.length() > 0 && done == false ){
     Serial.println(command);
    if(command=="send")reciveFromPhone();
    else if(command=="receive")SendToPhone();
  }
  delay(10);
}
void reciveFromPhone(){
  Serial.print("Command : ");
  Serial.println(command);
  if(command=="send"){
    Serial.println("OK !");
    while(!myStream.available()){
      delay(10);
    }
     getMess();
    Serial.println(command);
    getMessageFromCommand(command);
     command="";
    if(_pin!="" && _state!=""){
      int pin=_pin.toInt();
      if(_state=="ON"){
        digitalWrite(pin,HIGH);
      }else if(_state=="OFF"){
        digitalWrite(pin,LOW);
      }
    }
    command="";
   
  }
   done=true;
}
void SendToPhone(){
  while(!myStream.available())delay(3);
  getMess();
   Serial.print("sensor type : ");
   Serial.println(command);
       String hh,tt;
       char chars[6];
       bool xxx=false;
    if(command == "H&T"){
       xxx=true;
       command = "";                       //set 'command' to null 
    }else if(command=="close") xxx=false;
    while(xxx){
        int h =(int) dht.readHumidity();
        int t = (int)dht.readTemperature();
        if(isnan(h) || isnan(t))continue;
        hh=String(h);
        tt=String(t);
      Serial.println(hh);
      Serial.println(tt);
      done = true;  
      digitalWrite(LEDPIN,HIGH);
       int index=0;
    for(int i=0;i<hh.length();i++){
      chars[index++]=hh[i];
    }
    chars[index++]=' ';
    for(int i=0;i<tt.length();i++){
      chars[index++]=tt[i];
    }
    for (int x = 0; x < index; x++){//send each character of data to HC-05
      char ch = chars[x];
       Serial.println(ch);
      myStream.write(ch);
    }
    delay(2990);
    digitalWrite(LEDPIN,LOW);
    getMess();
    if(command=="close") {
      xxx=false;
      done=false;
      command="";
      }
    } 
}
void getMessageFromCommand(String str){
  int _first=0;
  _pin="";
  _state="";
  for(int i=0;i<str.length();i++){
    char c=str.charAt(i);
    
    if(c!=' '){
      if(_first==0){
        _pin+=c;
      }else _state+=c;
    }else _first=1;
  }
 Serial.println(_pin);
 Serial.println(_state);
}
