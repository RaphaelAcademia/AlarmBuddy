package com.sh2019.alarmbuddy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static android.view.View.INVISIBLE;

public class WakeupActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, LocationListener {

    private MediaPlayer music;
    private String name;
    private String reminder;
    private TextToSpeech tts;
    private String[] messageInfo;
    private int weatherValue;
    private double correctTemp;
    private EditText answerBox;
    private Button submitButton;
    private int incorrectCount = 0;

    private int currentApiVersion;
    public LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wakeup);

        submitButton = (Button) findViewById(R.id.submitButton);
        answerBox = (EditText) findViewById(R.id.answerText);

        submitButton.setVisibility(INVISIBLE);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        currentApiVersion = android.os.Build.VERSION.SDK_INT;

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility)
                        {
                            if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                            {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }

        disableAlarm();

        ArrayList<String> content = getFileContents("alarms.ini");
        for (String line : content){
            String key = line.split("=")[0];
            String value = "";
            if (line.split("=").length > 1){
                value = line.split("=")[1];
            }

            if (key.equals("reminder")){
                reminder = value;
            }else if (key.equals("name")){
                name = value;
            }
        }

        try {
            generateTTS();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void generateTTS() throws JSONException {
        try {

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
                return;
            }


            Criteria criteria = new Criteria();
            String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true));

            Location location = locationManager.getLastKnownLocation(bestProvider);


            if (location != null) {

                final double longitude = location.getLongitude();
                final double latitude = location.getLatitude();
                final Context context = this;
                final TextToSpeech.OnInitListener listener = this;
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            SpeechBuilder speechBuilder = new SpeechBuilder(longitude, latitude, name, reminder);
                            messageInfo = speechBuilder.generateMessage();
                            weatherValue = Integer.parseInt(messageInfo[1]);
                            correctTemp = Double.parseDouble(messageInfo[2]);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
                thread.join();

                tts = new TextToSpeech(context, listener, "com.google.android.tts");
            }
            else{

                locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);

            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void disableAlarm() {
        File temp = new File(getFilesDir(), "alarmsTemp.ini");

        try {
            temp.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> contents = getFileContents("alarms.ini");
        for (String line : contents){
            if (line.startsWith("enabled")){
                writeToFile("enabled=false", "alarmsTemp.ini");
            }else{
                writeToFile(line, "alarmsTemp.ini");
            }
        }

        File original = new File(getFilesDir(), "alarms.ini");
        original.delete();
        temp.renameTo(original);
    }

    public void writeToFile(String data, String fileName) {
        try{
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(fileName, Context.MODE_APPEND));
            outputStreamWriter.write(data + '\n');
            outputStreamWriter.close();
        }catch(IOException e){}
    }

    public ArrayList<String> getFileContents(String fileName){
        ArrayList<String> list = new ArrayList<>();
        File file = new File(getFilesDir(), fileName);
        if (file.exists()){
            try {
                FileInputStream is = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                while (line != null){
                    list.add(line);
                    line = reader.readLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public void onBackPressed(){ }

    @Override
    public void onPause(){
        if (music != null){
            music.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume(){
        if (music != null){
            music.start();
        }
        super.onResume();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){

            int id;
            switch(weatherValue){
                case 1:
                case 2:
                case 3:
                    id = R.raw.rainysong;
                    break;
                case 4:
                    id = R.raw.sunnysong;
                    break;
                case 5:
                    id = R.raw.cloudysong;
                    break;
                default:
                    id = R.raw.cloudysong;
                    break;
            }

            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float percent = 1f;
            int seventyVolume = (int) (maxVolume*percent);
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);

            music = new MediaPlayer();
            AssetFileDescriptor afd = getResources().openRawResourceFd(id);

            try{
                music.reset();
                music.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
                music.prepare();
                music.setLooping(true);
                music.start();
                afd.close();
            }catch(Exception e){
                e.printStackTrace();
            }


            final Context context = this;
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!answerBox.getText().toString().isEmpty()){
                        try{
                            double answer = Double.parseDouble(answerBox.getText().toString());
                            if (answer == correctTemp){
                                tts.stop();
                                tts.shutdown();
                                System.exit(0);
                            }else{
                                answerBox.setText("");
                                incorrectCount++;
                                if (incorrectCount < 5){
                                    Toast.makeText(context, "Incorrect answer!", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(context, "The temperature is " + correctTemp + " celsius.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch(NumberFormatException e){
                            answerBox.setText("");
                        }
                    }
                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    music.setVolume(0.5f, 0.5f);
                    tts.speak(messageInfo[0], TextToSpeech.QUEUE_FLUSH, null, null);
                    submitButton.setVisibility(View.VISIBLE);
                }
            }, 10000);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        locationManager.removeUpdates(this);

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();

        final Context context = this;
        final TextToSpeech.OnInitListener listener = this;
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    SpeechBuilder speechBuilder = new SpeechBuilder(longitude, latitude, name, reminder);

                    messageInfo = speechBuilder.generateMessage();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        tts = new TextToSpeech(context, listener, "com.google.android.tts");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
