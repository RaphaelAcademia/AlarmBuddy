package com.sh2019.alarmbuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context k1, Intent k2) {
        Intent intent = new Intent(k1, WakeupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        k1.startActivity(intent);
    }

}
