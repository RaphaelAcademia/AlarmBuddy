package com.sh2019.alarmbuddy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.AlphabeticIndex;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainABActivity extends AppCompatActivity {

    private File alarmFile;
    private FileOutputStream outputStream;
    private TextView mainTime;
    private TextView mainReminder;
    private Button nameChangeButton;
    private Switch alarmSwitch;
    private AlarmManager alarmManager;
    final private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ab);

        initListeners();
        initFileIO();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET},
                    1);
            return;
        }



    }


    private void initFileIO() {
        alarmFile = new File(getFilesDir(), "alarms.ini");
        if (!alarmFile.exists()){

            try {
                alarmFile.createNewFile();
                writeToFile("enabled=0", "alarms.ini");
                writeToFile("time=12:00", "alarms.ini");
                writeToFile("reminder=Wake up!", "alarms.ini");
                writeToFile("name=", "alarms.ini");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{
            ArrayList<String> contents = getFileContents("alarms.ini");
            for (String line : contents){

                String key = line.split("=")[0];
                String value = "";
                if (line.split("=").length > 1){
                    value = line.split("=")[1];
                }

                switch(key){
                    case "enabled":
                        alarmSwitch.setChecked(Boolean.parseBoolean(value));
                        break;
                    case "time":
                        int hour = Integer.parseInt(value.split(":")[0]);
                        int minute = Integer.parseInt(value.split(":")[1]);
                        setTime(hour, minute);
                        break;
                    case "name":
                        if (value.isEmpty()){
                            askName();
                        }
                        break;
                    case "reminder":
                        setReminder(value, false);
                        break;
                }
            }
        }
    }

    private void askName(){


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("What's your name?");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        ArrayList<String> contents = getFileContents("alarms.ini");
        for (String line : contents){
            if (line.startsWith("name")){
                if (line.split("=").length > 1){
                    input.setText(line.split("=")[1]);
                }
            }
        }


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().length() <= 20){
                    if (input.getText().toString().isEmpty()){
                        Toast.makeText(context, "Sorry, your name can't be empty.", Toast.LENGTH_SHORT).show();
                    }else{
                        setName(input.getText().toString());
                    }
                }else{
                    Toast.makeText(context, "Sorry, only up to 20 characters.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                ArrayList<String> contents = getFileContents("alarms.ini");
                for (String line : contents){
                    if (line.startsWith("name")){
                        if (line.split("=").length < 2){
                            askName();
                        }
                    }
                }
            }
        });

        builder.show();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListeners() {



        mainTime = (TextView) findViewById(R.id.mainTime);
        mainReminder = (TextView) findViewById(R.id.mainReminder);
        alarmSwitch = (Switch) findViewById(R.id.mainSwitch);
        nameChangeButton = (Button) findViewById(R.id.btnChangeName);

        mainTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showTimePickerDialog(v);
                return false;
            }
        });

        mainReminder.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Set Reminder");

                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                ArrayList<String> contents = getFileContents("alarms.ini");
                for (String line : contents){
                    if (line.startsWith("reminder")){
                        if (line.split("=").length > 1){
                            input.setText(line.split("=")[1]);
                        }
                    }
                }


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().length() <= 100){
                            setReminder(input.getText().toString(), true);
                        }else{
                            Toast.makeText(context, "Sorry, only up to 100 characters.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                return false;
            }
        });

        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                setAlarmEnabled(isChecked);

                if (isChecked){
                    setAlarm();
                }else{
                    removeAlarm();
                }
            }
        });

        nameChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askName();
            }
        });
    }

    public void showTimePickerDialog(View v) {

        Calendar c = Calendar.getInstance();

        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                setTime(view, hourOfDay, minute);
            }
        };

        new TimePickerDialog(MainABActivity.this, listener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), DateFormat.is24HourFormat(this)).show();

    }


    private void setReminder(String reminder, boolean writeToFile){


        TextView reminderElement = (TextView) findViewById(R.id.mainReminder);
        reminderElement.setText("Reminder: " + reminder);


        if (writeToFile){
            // Write to file (replace old)
            File temp = new File(getFilesDir(), "alarmsTemp.ini");

            try {
                temp.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<String> contents = getFileContents("alarms.ini");
            for (String line : contents){
                if (line.startsWith("reminder")){
                    writeToFile("reminder=" + reminder, "alarmsTemp.ini");
                }else{
                    writeToFile(line, "alarmsTemp.ini");
                }
            }

            File original = new File(getFilesDir(), "alarms.ini");
            original.delete();
            temp.renameTo(original);
        }

    }

    private void setName(String name){

        // Write to file (replace old)
        File temp = new File(getFilesDir(), "alarmsTemp.ini");

        try {
            temp.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> contents = getFileContents("alarms.ini");
        for (String line : contents){
            if (line.startsWith("name")){
                writeToFile("name=" + name, "alarmsTemp.ini");
            }else{
                writeToFile(line, "alarmsTemp.ini");
            }
        }

        File original = new File(getFilesDir(), "alarms.ini");
        original.delete();
        temp.renameTo(original);

    }

    // When changing
    private void setTime(TimePicker view, int hourOfDay, int minute){
        TextView timeElement = (TextView) findViewById(R.id.mainTime);
        String suffix = "AM";

        int originalHour = hourOfDay;
        if (hourOfDay > 12){
            hourOfDay -= 12;
            suffix = "PM";
        }

        String minStr = "" + minute;
        if (minStr.length() < 2){
            minStr = "0" + minStr;
        }
        String time = hourOfDay + ":" + minStr + " " + suffix;
        timeElement.setText(time);

        File temp = new File(getFilesDir(), "alarmsTemp.ini");

        try {
            temp.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> contents = getFileContents("alarms.ini");
        for (String line : contents){
            if (line.startsWith("time")){
                writeToFile("time=" + originalHour + ":" + minute, "alarmsTemp.ini");
            }else{
                writeToFile(line, "alarmsTemp.ini");
            }
        }

        File original = new File(getFilesDir(), "alarms.ini");
        original.delete();
        temp.renameTo(original);

        removeAlarm();
        setAlarmEnabled(false);
        alarmSwitch.setChecked(false);


    }

    // When loading
    private void setTime(int hourOfDay, int minute){
        TextView timeElement = (TextView) findViewById(R.id.mainTime);
        String suffix = "AM";

        if (hourOfDay > 12){
            hourOfDay -= 12;
            suffix = "PM";
        }

        String minStr = "" + minute;
        if (minStr.length() < 2){
            minStr = "0" + minStr;
        }

        String time = hourOfDay + ":" + minStr + " " + suffix;
        timeElement.setText(time);

    }

    public void setAlarmEnabled(boolean enabled){
        File temp = new File(getFilesDir(), "alarmsTemp.ini");

        try {
            temp.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> contents = getFileContents("alarms.ini");
        for (String line : contents){
            if (line.startsWith("enabled")){
                writeToFile("enabled=" + enabled, "alarmsTemp.ini");
            }else{
                writeToFile(line, "alarmsTemp.ini");
            }
        }

        File original = new File(getFilesDir(), "alarms.ini");
        original.delete();
        temp.renameTo(original);
    }

    public void setAlarm(){

        ArrayList<String> contents = getFileContents("alarms.ini");
        int hour = 0, minute = 0;
        for (String line : contents){
            String key = line.split("=")[0];
            String value = line.split("=")[1];
            if (key.startsWith("time")){
                hour = Integer.parseInt(value.split(":")[0]);
                minute = Integer.parseInt(value.split(":")[1]);
            }
        }

        String suffix = "AM";
        int originalHour = hour;
        if (hour > 12){
            hour -= 12;
            suffix = "PM";
        }

        String minStr = "" + minute;
        if (minStr.length() < 2){
            minStr = "0" + minStr;
        }

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar targetCal = Calendar.getInstance();

        targetCal.set(Calendar.HOUR_OF_DAY, originalHour);
        targetCal.set(Calendar.MINUTE, minute);
        targetCal.set(Calendar.SECOND, 0);
        targetCal.set(Calendar.MILLISECOND, 0);

        Calendar calNow = Calendar.getInstance();

        if (targetCal.compareTo(calNow) <= 0) {
            // Today Set time passed, count to tomorrow
            targetCal.add(Calendar.DATE, 1);
        }


        // use calNow for debug purposes
        // targetCal for normal
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);

    }

    public void removeAlarm(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 1, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);

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

    public String getPath(final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            System.out.println("getPath() uri: " + uri.toString());
            System.out.println("getPath() uri authority: " + uri.getAuthority());
            System.out.println("getPath() uri path: " + uri.getPath());

            // ExternalStorageProvider
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                System.out.println("getPath() docId: " + docId + ", split: " + split.length + ", type: " + type);

                // This is for checking Main Memory
                if ("primary".equalsIgnoreCase(type)) {
                    if (split.length > 1) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    } else {
                        return Environment.getExternalStorageDirectory() + "";
                    }
                    // This is for checking SD Card
                } else {
                    return "storage" + "/" + docId.replace(":", "/");
                }

            }
        }
        return null;
    }


}
