# Introduction
This project can do one of these tasks:-
1. Upload data from Arduino sensors (DHT22 and LDR) to Thingspeak platform.
2. Getting information from Arduino sensors to Android application and control some LEDs using HTTP Protocol.
# How to use
- Download required libraries like DHT22 library.
- Change access point ID and password (line 10) according to your access point.
- Change the "APIKEY" to your API Key channel (line 10) or you can keep it to upload on <a href="https://thingspeak.com/channels/204682"> My Channel </a>
- In case of uploading data to thingspeak keep "Support" value to 1 (line 13).
- In case of getting data from Arduino to Android change "Support" value to 2 (line 13) then install the "HomeAutomation" project using your android editor and change the IP, Port according to your ESP IP and port.
# Screenshot
![2016-12-24_04-51-05](https://user-images.githubusercontent.com/20142053/28349793-367ac6de-6c45-11e7-8ecf-eefc78da53ab.jpg)
