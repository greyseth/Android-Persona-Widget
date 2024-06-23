package com.example.widgettest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class AppWidgetAlarm {
    int alarmId = 0;
    //Updates every 10 minutes, theoretically. But it should revert to the 60 second minimum
    int interval = 1000*60*10;

    Context context;

    public AppWidgetAlarm(Context context) {
        this.context = context;
    }

    public void startAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, interval);

        Intent alarmIntent=new Intent(context, AppWidget.class);
        alarmIntent.setAction(AppWidget.ACTION_AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent alarmIntent4=new Intent(context, Widget4.class);
        alarmIntent4.setAction(AppWidget.ACTION_AUTO_UPDATE);
        PendingIntent pendingIntent4 = PendingIntent.getBroadcast(context, alarmId, alarmIntent4, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // RTC does not wake the device up
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), interval, pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), interval, pendingIntent4);
    }

    public void stopAlarm() {
        Intent alarmIntent = new Intent(AppWidget.ACTION_AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
