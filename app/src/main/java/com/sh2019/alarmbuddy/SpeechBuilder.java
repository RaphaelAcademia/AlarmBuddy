package com.sh2019.alarmbuddy;

import android.icu.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SpeechBuilder {

    private double longitude, latitude;
    private String name, reminder;
    public SpeechBuilder(double longitude, double latitude, String name, String reminder){
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = name;
        this.reminder = reminder;
    }

    public String[] generateMessage() throws IOException, JSONException {
        String finalMessage = "";

        JSONObject weatherObj = WeatherData.getJSON(latitude, longitude);
        String weatherDescription = weatherObj.getJSONArray("weather").getJSONObject(0).getString("main");
        String temperature = weatherObj.getJSONObject("main").getString("temp");
        String windSpeed = weatherObj.getJSONObject("wind").getString("speed");

        Calendar c = Calendar.getInstance();
        String day = dayOfWeek(c.get(Calendar.DAY_OF_WEEK));
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        String suffix = "AM";
        int originalHour = hour;
        if (hour > 12){
            hour -= 12;
            suffix = "PM";
        }

        String time = hour + ":" + minute + " " + suffix;



        int weatherValue = 0;
        double wind = Double.parseDouble(windSpeed);
        switch(weatherDescription){
            case "Rain":
            case "Drizzle":
            case "Thunderstorm":
            case "Mist":
                weatherValue = 1;
                break;
            case "Snow":
                weatherValue = 2;
                break;
            case "Atmosphere":
                weatherValue = 3;
                break;
            case "Clear":
                weatherValue = 4;
                break;
            case "Clouds":
                weatherValue = 5;
                break;
            default:
                weatherValue = 4;
                break;

        }
        double temp = Double.parseDouble(temperature);
        if (temp >= 1 && temp <= 6){
            if (weatherValue == 1 && (wind >= 29)){
                finalMessage = "If I were you, I" +
                        "wouldn't want to get up either" + name + ", but you have to!" +
                        "It's " + day + " today, and it's very cold. It's " + temperature + " degrees celsius, " +
                        "the wind is strong, and it's raining. Looks like it's near winter too, so you should" +
                        "get in your full winter gear today. You told me: " + reminder + ", so" +
                        "make sure you remember! It is " + time + " and make sure" +
                        "you make today your masterpiece!";
            }else if (weatherValue == 5 && (wind < 29)){
                finalMessage = "It's time to wake up " +
                        name + "! I think today, " + day + ", will be a great day, so let's " +
                        "not waste any time! It's getting a little chilly nowadays but right now, the " +
                        "sun is shining bright, and the wind isn't very strong, so it's nice out despite " +
                        "it being " + temperature + " degrees celsius. I'd recommend a sweater with a winter jacket, and" +
                        "you might be able to get away with a fall jacket too! Don't forget: " + reminder + ". " +
                        "It is " + time + " and I hope you have a ridiculously amazing day, " + name + "!";
            }

        }else if ((temp < 0) && (wind < 5) && (weatherValue == 5 || weatherValue == 2)){ // waterloo tmw
            finalMessage = "Wakey, wakey, eggs " +
                    "and bakey " + name + "! Today is " + day + " and it's not looking too bad " +
                    "outside. It is " + temperature + " degrees celsius and cloudy, but luckily there's no" +
                    "snow or wind! You're definitely going to need your winter jacket, scarf, gloves, " +
                    "the whole set! Oh, before I forget: " + reminder + ". It is currently " + time +
                    ", time to wake up! It may not be fantastic outside, but " +
                    "I hope you have a fantastic day anyways, " + name + "!";
        }else {

            String feeling = "";
            switch(weatherValue){
                case 1:
                    feeling = "looking like some rain outside.";
                    break;
                case 2:
                    feeling = "looking like some snow outside.";
                    break;
                case 3:
                    feeling = "looking foggy outside.";
                    break;
                case 4:
                    feeling = "looking clear outside.";
                    break;
                case 5:
                    feeling = "looking cloudy outside.";
                    break;
            }

            String greeting = "";
            if (originalHour >= 0 && originalHour < 12){
                greeting = "Good morning, ";
            }else if (originalHour >= 12 && originalHour < 17){
                greeting = "Good afternoon, ";
            }else if (originalHour >= 17 && originalHour <= 24){
                greeting = "Good evening, ";
            }
            finalMessage = greeting + name + "! It's " + day + " today, and it's " + feeling + " Right now, it's " +
                    temperature + " degrees celsius. Before you forget, you reminded me to tell you: " + reminder + ". It's " + time
                    + " right now. Have a nice day!";
        }


        String[] info = { finalMessage, weatherValue + "", temp + ""};
        return info;
    }
    public static String dayOfWeek(int day){
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return dayNames[day - 1];
    }

}
