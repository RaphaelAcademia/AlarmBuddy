package com.sh2019.alarmbuddy; //Replace with appropriate package
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

public class WeatherData {

    public static JSONObject getJSON (double latitudeLocation, double longitudeLocation) throws IOException {   //Returns appropriate weather data given the location

        String urlForOpenWeatherMap = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitudeLocation + "&lon=" + longitudeLocation + "&appid=775f8b67a3a3da847a9b9ad13194bded&units=metric";
        URL url = null;
        try {
            url = new URL(urlForOpenWeatherMap);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            connection.setRequestMethod ("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        }catch(Exception e){
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String tmp="";
        String a = "";
        while((tmp=reader.readLine())!=null)
        {
            a = tmp + "\n";
        }
        reader.close();

        JSONObject data = null;

        try {
            data = new JSONObject(a);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;


    }


}
