# AlarmBuddy

## Inspiration
We like to sleep a lot. Sometimes too much.
## What it does
This app acts as an alarm clock, first playing gentle music, then presenting the day's weather data in text to speech format. To dismiss the alarm, the user must answer how many degrees it is outside (if they were paying attention).
## How we built it
We built it using Android Studio, Gradle, and OpenWeatherMap. 
## Challenges we ran into
GCP TTS API didn't work for Android, since we couldn't get the credentials working. It was frustrating, as to authenticate with the API, we were required to set an environment path to point to the directory of a json file containing credentials. However, we speculated that it was not possible to do on an Android device, unless the device was rooted.
## Accomplishments that we're proud of
The core functionality works, including successfully hooking our app's alarm activity into the android system's Alarm Manager. We also managed to get location services working, which we could then use to retrieve weather data using the OpenWeatherMap API.
## What we learned
Many different aspects of app development.


Made by Andrew Peng, Keefe Ho, and Raphael Academia
