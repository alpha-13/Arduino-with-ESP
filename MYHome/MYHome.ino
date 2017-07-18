#include<SoftwareSerial.h>
#include <DHT.h>

#define DHTPIN 2
#define DHTTYPE DHT11

SoftwareSerial ESP(12,13);
DHT dht(DHTPIN, DHTTYPE);

const String ID = "UnKnown", Password = "alpha-13", APIKEY = "Y8HEU7P68J9IQZSI";
bool ME;

#define Support 1
// 1 => thingspeak
// 2 => android

/////////////////////////////////////////////////// Define Input-Output Pins ///////////////////////////////////////////////////

#define RLED 3
#define YLED 4
#define GLED 5

#define Buzzer 8
#define LDR A0

#define RedTest 6
#define GreenTest 7
#define YellowTest 9

void setup() {
  Serial.begin(115200);
  ESP.begin(115200);
  dht.begin();
  
  pinMode(Buzzer, OUTPUT);

  tone(Buzzer, 1000);
  delay(500);
  noTone(Buzzer);
  
  Serial.println("Start ESP!");
  SendCommand("AT+RST", 5000);

  SendCommand("AT+CWMODE=1", 2000);

  SendCommand("AT+CWJAP=\"" + ID + "\",\"" + Password + "\"", 5000);

  delay(3000);
  
  if(Support == 1){
    SendCommand("AT+CIPMUX=0",1500);
    SendCommand("AT+CIPSERVER=0,80",1000);
  }
  else{
    SendCommand("AT+CIFSR",1500);
    SendCommand("AT+CIPMUX=1",1500);
    SendCommand("AT+CIPSERVER=1,80",1500);
  }
    
  pinMode(LDR, INPUT);
  
  pinMode(GLED, OUTPUT);
  pinMode(YLED, OUTPUT);
  pinMode(RLED, OUTPUT);

  pinMode(RedTest, OUTPUT);
  pinMode(GreenTest, OUTPUT);
  pinMode(YellowTest, OUTPUT);
  
  LED(GLED);
  LED(YLED);
  LED(RLED);
  
  tone(Buzzer, 1000);
  digitalWrite(GreenTest, HIGH);
  delay(500);
  noTone(Buzzer);
  delay(500);
  digitalWrite(GreenTest, LOW);
  digitalWrite(YellowTest, HIGH);

  Serial.println("ESP Ready!");
}

void loop() {
  
  float H = dht.readHumidity();
  float C = dht.readTemperature();
  float F = dht.readTemperature(true);
  
  float HIC = dht.computeHeatIndex(C, H);
  float HIF = dht.computeHeatIndex(F, H);
  
  int LDRValue = analogRead(LDR);

  if(Support == 2)
  {
    SendDataToAndroid(HIC, HIF, H, LDRValue);
  }
  else if(Support == 1)
    {
      ThingSpeakWrite(HIC, HIF, H, LDRValue);
      delay(3000);
    }
    else
    {
      Serial.println("TempC = "+ String(C) + " oC");
      Serial.println("TempF = "+ String(F) + " oF");
      Serial.println("Humidity = "+ String(H) + " %");
      
      Serial.println("TempC Index = "+ String(HIC) + " oC");
      Serial.println("TempF Index = "+ String(HIF) + " oF");
      
      Serial.println("LDR = " + String(LDRValue));
      
      delay(2000);
    }
}

///////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////// Methods ////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////

////////////////////////// ESP Response ///////////////////////
String SendCommand(String command,int waitTime){
  String response = "";
  ESP.print((command + "\r\n"));
  
  long t = millis();
  
  while((t + waitTime) > millis()){
    while(ESP.available()){
      char c = ESP.read();
        response += c;
    }
  }
  Serial.print(response);
  return response;
}
////////////////////////// ESP Response ///////////////////////

/////////////////////////// LED /////////////////////////////

void LED(int OUT){
      digitalWrite(OUT, HIGH);
      delay(500);
      digitalWrite(OUT, LOW);
      delay(500);
}

/////////////////////////// LED /////////////////////////////

//////////////////////// Buzzer ////////////////////////////

void LoopBuzzer(int Iteration, int Value, int d){
  while(Iteration > 0)
  {
    tone(Buzzer, Value);
    delay(d);
    noTone(Buzzer);
    delay(d);
    Iteration--;
  }
}

//////////////////////// Buzzer ////////////////////////////


////////////////////// Write on ThingSpeak ///////////////////

void ThingSpeakWrite(float TempC, float TempF, float Humidity, int Light){
  
  String cmd = "AT+CIPSTART=\"TCP\",\"184.106.153.149\",80\r\n";
  ESP.print(cmd);
  Serial.println("\r\n\r\n\r\n\t\t\t================\r\n\t\t\t=== Connect !===\r\n\t\t\t================\r\n");
  delay(1000);
  if(ESP.find("OK")){
    Serial.println("TCP Connected");
  }
  else{
    Serial.println("TCP Not Connected!");
  }
 
  String getStr = "GET /update?api_key=" + APIKEY + "&field1=" + TempC + "&field2=" + TempF + "&field3=" + Humidity + "&field4=" + Light +"\r\n";

  cmd = "AT+CIPSEND=";
  cmd += String(getStr.length());
  cmd += "\r\n";
  ESP.print(cmd);
  Serial.println("\r\nSend Data!");

  delay(1000);
  if(ESP.find(">")){
     ESP.print(getStr);
      Serial.println("\t\t\t================\r\n\t\t\t===Successful===\r\n\t\t\t================");
      digitalWrite(GreenTest, HIGH);
      LoopBuzzer(2, 1000, 200);
      digitalWrite(GreenTest, LOW);
    }
    else{
      Serial.println("\t\t\t----------------\r\n\t\t\t-----Failed-----\r\n\t\t\t----------------");
      digitalWrite(RedTest, HIGH);
      LoopBuzzer(2, 1000, 1000);
      digitalWrite(RedTest, LOW);
      delay(500);
      digitalWrite(RedTest, HIGH);
      delay(500);
      digitalWrite(RedTest, LOW);
    }
    ESP.print("AT+CIPCLOSE\r\n");
    Serial.println("\r\n\t\t\t================\r\n\t\t\tColse Connection\r\n\t\t\t================");
}

////////////////////// Write on ThingSpeak ///////////////////


/////////////////////// Android /////////////////////////////

void SendDataToAndroid(float TempC, float TempF, float Humidity, int Light){
  
  if(ESP.find("+IPD,"))
  {
    digitalWrite(YellowTest, LOW);
    delay(500);
    digitalWrite(YellowTest, HIGH);
    delay(500);
    digitalWrite(YellowTest, LOW);
    delay(500);
    digitalWrite(YellowTest, HIGH);
    
    //delay(1000);
    
    int ConnectedDevice = ESP.read()-48;
    
    String Reply = "Error, Please Try Again !";
    String ReadRequest = "";
    while (ESP.available()) {
      delay(3); 
      char c = ESP.read();
      ReadRequest += c;
    }

    ME = false;

  if (ReadRequest.length() >0) {
    if(ReadRequest.indexOf("pin=") >=0)
    { 
      int EqualIndex = ReadRequest.indexOf('=');
      int PinNumber = (ReadRequest.substring(EqualIndex + 1, EqualIndex + 2)).toInt();
      digitalWrite(PinNumber, !digitalRead(PinNumber));
      switch(PinNumber){
        case 3: Reply = "Red "; break;
        case 4: Reply = "Yellow "; break;
        case 5: Reply = "Green "; break;
        Default: Reply = "Unknown ";
      }
      
      Reply += "LED is ";
      if(digitalRead(PinNumber)){
        Reply += "ON\r\n";
      }
      else{
        Reply += "OFF\r\n";
      }
      ME = true;
    }
    
    if(ReadRequest.indexOf("GETALL") >=0)
    {
      Reply = "ALLPING=" + String(digitalRead(GLED)) + "&PINY=" + String(digitalRead(YLED)) + "&PINR=" + String(digitalRead(RLED)) 
      + "&TEMPC=" + TempC + "&TEMPF=" + TempF + "&H=" + Humidity + "&Light=" + Light;
      ME = true;
      
    }
    if(ME)
      digitalWrite(GreenTest, HIGH);
    else
      digitalWrite(RedTest, HIGH);
  }
  else
  {
    digitalWrite(RedTest, HIGH);
  }
    Serial.println("\n\t\t\t=======> Reply <======\n" + Reply);

    SendHTTPResponse(ConnectedDevice, Reply);
    SendCommand(("AT+CIPCLOSE=" + String(ConnectedDevice) + "\r\n"), 1500);
    if(ME)
    {
      LoopBuzzer(2, 1000, 200);
      digitalWrite(GreenTest, LOW);
    }
    else
    {
      LoopBuzzer(2, 1000, 1000);
      digitalWrite(RedTest, LOW);
      delay(200);
      digitalWrite(RedTest, HIGH);
      delay(200);
      digitalWrite(RedTest, LOW);
    }
  }
}

void SendHTTPResponse(int ConnectedDevice, String Reply){
  String HttpHeader;
  HttpHeader = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n";
  HttpHeader += "Content-Length: ";
  HttpHeader += Reply.length();
  HttpHeader += "\r\nConnection: close\r\n\r\n";
  SendDataLengthToClient(ConnectedDevice, String(HttpHeader + Reply + " "));
}

void SendDataLengthToClient(int ConnectedDevice, String Reply){
  String Sendcmd = "AT+CIPSEND=" + String(ConnectedDevice) + ",";
  Sendcmd += Reply.length();
  SendCommand(Sendcmd, 1000);
  //delay(1000);
  
  //ESP.print((Reply + "\r\n"));

  SendData(Reply, 1000);
  //delay(1000);
}

String SendData(String command, int waitTime)
{
    String response = "";
    
    int DataSize = command.length();
    char Data[DataSize];
    command.toCharArray(Data, DataSize);
           
    ESP.write(Data, DataSize);

    Serial.println("\n\t\t\t ======================\n\t\t\t ==== Sending Data ====\n\t\t\t ======================\n");
      
    long int time = millis();
    
    while((time + waitTime) > millis())
    {
      while(ESP.available())
      {
        char c = ESP.read();
        response += c;
      }  
    }
    
    Serial.print(response);
    
    return response;
}

/////////////////////// Android /////////////////////////////
