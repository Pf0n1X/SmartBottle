#include <SoftwareSerial.h>
#include <stdlib.h>

SoftwareSerial MyBlue(0, 1); // RX, TX
const int echo = 2;
const int trigger = 3;
float dist;

void setup() {
  pinMode(trigger,OUTPUT);
  pinMode(echo,INPUT);

  MyBlue.begin(9600);
}

void loop() {
  
  // Get the distance from the water
  digitalWrite(trigger,LOW);
  delayMicroseconds(5);        
  digitalWrite(trigger,HIGH);  
  delayMicroseconds(10);      
  digitalWrite(trigger,LOW); 
  dist=pulseIn(echo,HIGH);      
  dist = dist/58;    
  MyBlue.println(dist);

  delay (200); 
}