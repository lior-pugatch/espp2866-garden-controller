

#include <LinkedList.h>

#include <Time.h>
#include <TimeLib.h>
#include <TimeAlarms.h>
#include <NtpClientLib.h>
#include <ESP8266WiFi.h>
#include <WiFiUDP.h>

#define DAYS_OF_WEEK 7
#define PULSE_TIME_MS 200
#define MSG_OPCODE_BYTE_OFFSET 0
#define MSG_LINE_SIZE_LEN 4
// Add 1 for opcode
#define MSG_SIZE_HEADER_AND_SCHED ((MSG_LINE_SIZE_LEN*DAYS_OF_WEEK) + 1)

extern "C" {  //required for read Vdd Voltage
#include "user_interface.h"
  // uint16 readvdd33(void);
}

int status = WL_IDLE_STATUS;
const char* ssid = "xxx";  //  your network SSID (name)
const char* pass = "xxx";       // your network password

unsigned int localPort = 10080;      // local port to listen for UDP packets
int remotePort = 10070;
byte packetBuffer[512]; //buffer to hold incoming and outgoing packets

// A UDP instance to let us send and receive packets over UDP
WiFiUDP Udp;



/* definitions for esp2866 */
const int outPinOn = 0;
const int outPinOff = 4;
/* Enale negative and positive circuits */
const int outPinEnable = 5;
const int ledPin = 13;

typedef struct
{
  int enabled;
  int internalId;
  timeDayOfWeek_t dayOfWeek;
  int startHour;
  int startMin;
  int durationInMins;
}waterTimer_t;

LinkedList<waterTimer_t> timerList = LinkedList<waterTimer_t>(); 

const timeDayOfWeek_t int2DayOfWeek[] = 
{
  dowSunday,
  dowMonday,
  dowTuesday,
  dowWednesday,
  dowThursday,
  dowFriday,
  dowSaturday
};

waterTimer_t waterTimers[DAYS_OF_WEEK] = 
{
  {0, dtINVALID_ALARM_ID , dowSunday, 7, 0, 0},
  {1, dtINVALID_ALARM_ID , dowMonday, 7, 0, 60},
  {0, dtINVALID_ALARM_ID , dowTuesday, 7, 0, 0},
  {0, dtINVALID_ALARM_ID , dowWednesday, 7, 0, 0},
  {1, dtINVALID_ALARM_ID , dowThursday, 7, 0, 60},
  {0, dtINVALID_ALARM_ID , dowFriday, 7, 0, 0},
  {1, dtINVALID_ALARM_ID , dowSaturday, 7, 0, 50},
}
;

bool istimeSynced = false;

void setup() 
{
  Serial.begin(115200);
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);

  pinMode(outPinOn, OUTPUT);
  pinMode(outPinOff, OUTPUT);
  pinMode(outPinEnable, OUTPUT);
  pinMode(ledPin, OUTPUT);

  Serial.print(dtNBR_ALARMS);
  Serial.println("Setting alarms and timers...");
//  Alarm.alarmRepeat(dowSaturday, 8, 16, 30, alarmEvent);

   Alarm.timerRepeat(15, Repeats);            // timer for every 15 seconds   

//   waterTimer_t waterTimer = {1, 0, dowFriday, 14, 26, 1};
//   setAlarm(&waterTimer);


  // setting up Station AP
  WiFi.begin(ssid, pass);
  
  // Wait for connect to AP
  Serial.print("[Connecting]");
  Serial.print(ssid);
  int tries=0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    tries++;
    if (tries > 30){
      break;
    }
  }

  Serial.println();


  printWifiStatus();

  Serial.println("Connected to wifi");
  Serial.print("Udp server started at port ");
  Serial.println(localPort);
  Udp.begin(localPort);

  ntpInit();

  while (timeStatus() == timeNotSet)
  {
    delay(100);
    // Busy wait
  }
  
// Set init alarms
  updateAlarms();
}

void ntpInit()
{
  NTP.onNTPSyncEvent([](NTPSyncEvent_t error) {
        if (error) {
            Serial.print("Time Sync error: ");
            if (error == noResponse)
                Serial.println("NTP server not reachable");
            else if (error == invalidAddress)
                Serial.println("Invalid NTP server address");
        }
        else {
            Serial.print("Got NTP time: ");
            Serial.println(NTP.getTimeDateString(NTP.getLastNTPSync()));
            istimeSynced = true;
        }
    });
    NTP.begin("pool.ntp.org", 1, true);
    NTP.setInterval(1800);
}

void printWifiStatus() 
{
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your WiFi shield's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);
}

void loop() 
{
  int nBytes = Udp.parsePacket();
  
  if (nBytes > 0)
  {
    Udp.read(packetBuffer,nBytes);
    for (int i=1;i<=nBytes;i++)
    {
      Serial.print(packetBuffer[i-1],HEX);
      if (i % 32 == 0)
      {
        Serial.println();
      }
      else Serial.print(' ');
    } // end for
    Serial.println();
    
    parseMessage(packetBuffer, nBytes);
  }

  Alarm.delay(20);
//  digitalClockDisplay();
}

void Repeats()
{
  //Serial.println("Every 60 secs alarm");
  digitalClockDisplay();
}
void digitalClockDisplay(){
  // digital clock display of the time
  Serial.print(hour());
  printDigits(minute());
  printDigits(second());
  Serial.print(" ");
  Serial.print(day());
  Serial.print(" ");
  Serial.print(month());
  Serial.print(" ");
  Serial.print(year()); 
  Serial.println(); 
}


void printDigits(int digits){
  // utility function for digital clock display: prints preceding colon and leading 0
  Serial.print(":");
  if(digits < 10)
    Serial.print('0');
  Serial.print(digits);
}

void setAlarm(waterTimer_t* waterTimer)
{
  waterTimer->internalId = Alarm.alarmRepeat(waterTimer->dayOfWeek, waterTimer->startHour, waterTimer->startMin, 0, alarmEvent);
  Serial.print("Setting alarm: ");
  Serial.print(waterTimer->dayOfWeek);
  Serial.print(",");
  Serial.print(waterTimer->startHour);
  Serial.print(",");
  Serial.print(waterTimer->startMin);
  Serial.println("");
  
  //timerList.add(*waterTimer);
   
}
void alarmEvent()
{
  int internalId;
  waterTimer_t* waterTimer;
  
 //  digitalWrite(ESP8266_LED, LOW); // LED on

Serial.println(" Event happend ");
   
   
   internalId = Alarm.getTriggeredAlarmId();
   Serial.print("Got internal ID");
   Serial.println(internalId);
   waterTimer = getListEntryByInternalId(internalId);
   if (waterTimer != NULL)
   {
     Serial.print("Water On for duartion of (mins): ");
     Serial.println(waterTimer->durationInMins);
   }
   else
   {
    Serial.println("Couldn't find alarm internal ID in table");
   }
   /* Convert seconds to minutes */
   turnWaterOn();
   Alarm.timerOnce(waterTimer->durationInMins * 60, timerEvent);
}

void timerEvent()
{
  turnWaterOff();
}

waterTimer_t* getListEntryByInternalId(int id)
{
  int i;  
  
   for(i = 0; i < 7; i++)
  {
    if(waterTimers[i].internalId == id)
      return &waterTimers[i];  
  }
  return NULL;
}

void turnWaterOn()
{
    /* Signal that water is on - led is inverse */
  digitalWrite(ledPin, HIGH);

  Serial.println("Water On!");
  /* first set the enable pin, then give a short voltage to on pin */
 
  digitalWrite(outPinOn, HIGH);
  digitalWrite(outPinEnable, HIGH);
  delay(PULSE_TIME_MS);
  digitalWrite(outPinEnable, LOW);
  digitalWrite(outPinOn, LOW); 
}

void turnWaterOff()
{
    /* Signal that water is off - led is inverse */
  digitalWrite(ledPin, LOW);

  Serial.println("Water Off!");
  /* Same logic as on*/

  digitalWrite(outPinOff, HIGH);
  digitalWrite(outPinEnable, HIGH);
  delay(PULSE_TIME_MS);
  digitalWrite(outPinEnable, LOW);
  digitalWrite(outPinOff, LOW); 
}

void parseMessage(byte* buf, unsigned int size)
{
  if (buf == NULL)
    return;

  switch(buf[MSG_OPCODE_BYTE_OFFSET])
  {
    case 0xA0:
    sendSched();
    break;
    
    case 0xA1:
    parseSchedual(&buf[1]);
    updateAlarms();
    break;
    
    case 0xA2:
    turnWaterOn();
    break;
    
    case 0xA3:
   turnWaterOff();
    break; 

    case 0xA4:
    sendTime();
    break;
  }
}



void parseSchedual(byte* buf)
{
  int i;
  
  // Go over all days of week
  for (i = 0; i < 7; i++)
  {
    waterTimers[i].enabled = buf[i*MSG_LINE_SIZE_LEN];
    waterTimers[i].startHour = buf[i*MSG_LINE_SIZE_LEN + 1];
    waterTimers[i].startMin = buf[i*MSG_LINE_SIZE_LEN + 2];
    waterTimers[i].durationInMins = buf[i*MSG_LINE_SIZE_LEN + 3];
  }
  
}

void updateAlarms()
{
  int i;

  for (i = 0; i < 7; i++)
  {
    // need to test the water off affect
    //turnWaterOff();
    
    // if alarm has valid id free it
    if (waterTimers[i].internalId != dtINVALID_ALARM_ID )
    { 
       Serial.print("Freeing alarm: ");
       Serial.println(waterTimers[i].internalId);
       
       Alarm.free(waterTimers[i].internalId);
       waterTimers[i].internalId = dtINVALID_ALARM_ID;
    }
    // if alarm is enabled, create it
    if (waterTimers[i].enabled == true)
    {
      waterTimers[i].internalId = Alarm.alarmRepeat(waterTimers[i].dayOfWeek, waterTimers[i].startHour, waterTimers[i].startMin, 0, alarmEvent);

      Serial.print("Setting alarm: ");
      Serial.print(waterTimers[i].internalId);
      Serial.print(",");
      Serial.print(waterTimers[i].dayOfWeek);
      Serial.print(",");
      Serial.print(waterTimers[i].startHour);
      Serial.print(",");
      Serial.print(waterTimers[i].startMin);
      Serial.println("");
    }
//    else
//    {
//      time_t newTime = AlarmHMS(
//        waterTimers[i].startHour,
//        waterTimers[i].startMin,
//        0/* Seconds */ );
//      Alarm.write(waterTimers[i].internalId, newTime );
//
//      // handle case where water is currently on
//      turnWaterOff();
//    }
//    
//    if (waterTimers[i].enabled == 0)
//    {
//      Alarm.disable(waterTimers[i].internalId);
//    }


        
//    setAlarm(&waterTimers[i]);
  }
}

void sendSched()
{
  int i;
  char printCmd = 0xC0;
  byte buf [MSG_SIZE_HEADER_AND_SCHED] = {0};
  
  buf[MSG_OPCODE_BYTE_OFFSET] = printCmd;

  for (i = 0; i < 7; i++)
  {
    buf[i*MSG_LINE_SIZE_LEN + 1] = waterTimers[i].enabled;
    buf[i*MSG_LINE_SIZE_LEN + 2] = waterTimers[i].startHour;
    buf[i*MSG_LINE_SIZE_LEN + 3] = waterTimers[i].startMin;
    buf[i*MSG_LINE_SIZE_LEN + 4] = waterTimers[i].durationInMins;
  }

  //print buffer for debug
  for (i = 0; i < MSG_SIZE_HEADER_AND_SCHED; i++)
  {
    Serial.print(buf[i], HEX);
    Serial.print(" ");
    if (i % MSG_LINE_SIZE_LEN == 0)
    {
       Serial.println("");
    }
  }
  Serial.println("");

  
  Udp.beginPacket(Udp.remoteIP(), remotePort);
  Udp.write(buf, MSG_SIZE_HEADER_AND_SCHED);
  Udp.endPacket();
}

void sendTime()
{
  String timeStr = String(String('T') + String(hour()) + String(':') + String(minute())
                    + String(':') + String(second()) + String(' ') 
                    + String (day()) + String('-') + String (month())
                     + String('-') + String (year()));

 Serial.println(" Sending time");
 Serial.println(timeStr);
  Udp.beginPacket(Udp.remoteIP(), remotePort);
  Udp.write(timeStr.c_str(), timeStr.length());
  Udp.endPacket();
}

