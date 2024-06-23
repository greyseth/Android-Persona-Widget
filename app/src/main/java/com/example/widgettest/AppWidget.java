package com.example.widgettest;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextClock;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.widgettest.models.API;
import com.example.widgettest.models.ApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {
    //Persona 3 Widget

    public static final String ACTION_AUTO_UPDATE = "AUTO_UPDATE";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Bundle dataBundle) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        views.setTextViewText(R.id.timeDisplay, (dataBundle.getInt("month")+1)+" / "+dataBundle.getInt("date"));

        String dayOfWeek = dataBundle.getString("dayOfWeek");
        views.setTextViewText(R.id.dayDisplay, dayOfWeek);
        if (dayOfWeek.equals("Su")) views.setTextColor(R.id.dayDisplay, Color.parseColor("#B50000"));
        else views.setTextColor(R.id.dayDisplay, Color.parseColor("#001736"));

        String timeOfDay = dataBundle.getString("timeOfDay");
        views.setTextViewText(R.id.timeDescription, timeOfDay);
        views.setTextViewText(R.id.timeDescriptionShadow, timeOfDay);

        //dark hour
        if (timeOfDay.equals("Dark Hour")) {
            views.setImageViewResource(R.id.widgetBackground, R.drawable.bg_widget_3_dark);
            views.setTextColor(R.id.timeDescription, Color.parseColor("#3c897d"));
            views.setTextColor(R.id.timeDescriptionShadow, Color.parseColor("#142E2A"));
        }else {
            views.setImageViewResource(R.id.widgetBackground, R.drawable.bg_widget_3);
            views.setTextColor(R.id.timeDescription, Color.parseColor("#FFFFFFFF"));
            views.setTextColor(R.id.timeDescriptionShadow, Color.parseColor("#001736"));
        }

        int mp = dataBundle.getInt("moonPhase");
        views.setImageViewResource(R.id.moonPhaseImg, mp);
        if (mp == R.drawable.moon_full) views.setTextViewText(R.id.moonPhase, "FULL");
        else if (mp == R.drawable.moon_new) views.setTextViewText(R.id.moonPhase, "NEW");
        else if (mp == R.drawable.moon_2 || mp == R.drawable.moon_5) views.setTextViewText(R.id.moonPhase, "HALF");
        else views.setTextViewText(R.id.moonPhase, "");

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    SharedPreferences sp = context.getSharedPreferences("defaultPosition", Context.MODE_PRIVATE);
                    String latitude = sp.getString("lat", "");
                    String longitude = sp.getString("long", "");

                    if (latitude.isEmpty() || longitude.isEmpty()) return;

                    moonRequest(context, appWidgetManager, appWidgetId, latitude, longitude);
                }else {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000 * 30, 100, new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location location) {
                            String latitude = String.valueOf(location.getLatitude());
                            String longitude = String.valueOf(location.getLongitude());

                            moonRequest(context, appWidgetManager, appWidgetId, latitude, longitude);

                            lm.removeUpdates(this);
                        }
                    });
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void moonRequest(Context context, AppWidgetManager appWidgetManager, int appWidgetId, String latitude, String longitude) {
        Calendar calendar = Calendar.getInstance();
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        //testing
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        String timeOfDay = "";
        String dayOfWeek = "";
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:dayOfWeek = "Mo";break;
            case Calendar.TUESDAY:dayOfWeek = "Tu";break;
            case Calendar.WEDNESDAY:dayOfWeek = "We";break;
            case Calendar.THURSDAY:dayOfWeek = "Th";break;
            case Calendar.FRIDAY:dayOfWeek = "Fr";break;
            case Calendar.SATURDAY:dayOfWeek = "Sa";break;
            case Calendar.SUNDAY:dayOfWeek = "Su";break;
        }

        if (hour > 0 && hour < 8) timeOfDay = "Morning";
        else if (hour >= 8 && hour < 12) timeOfDay = "Daytime";
        else if (hour >= 12 && hour < 14) timeOfDay = "Lunchtime";
        else if (hour >= 14 && hour < 16) timeOfDay = "Afternoon";
        else if (hour >= 16 && hour < 20) timeOfDay = "After School";
        else if (hour >= 20 && hour < 23) timeOfDay = "Evening";
        else if (hour >= 23) timeOfDay = "Dark Hour";

        String moonAPIurl = "https://api.weatherapi.com/v1/astronomy.json?key=0f287d6bead449c9bc133241240305&q=-6.281576,%20106.724317";
        Bundle otherParams = new Bundle();
        otherParams.putString("timeOfDay", timeOfDay);
        otherParams.putString("dayOfWeek", dayOfWeek);
        API.get(context, moonAPIurl, otherParams, new ApiResponse() {
            @Override
            public void onResponse(Optional<JSONObject> response, Bundle otherParams) throws JSONException {
                if (response.isPresent()) {
                    //Empty: New Moon
                    //1: Waxing Crescent
                    //2: First Quarter
                    //3: Waxing Gibbous
                    //Full: Full Moon
                    //4: Waning Gibbous
                    //5: Third Quarter
                    //6: Waning Crescent

                    String moonPhase = response.get().getJSONObject("astronomy").getJSONObject("astro").getString("moon_phase").toLowerCase();
                    String mp = "new";
                    switch(moonPhase) {
                        case "new moon":mp = "new";break; case "waxing crescent":mp="1";break; case "first quarter":mp="2";break;
                        case "waxing gibbous":mp="3";break; case "full moon":mp="full";break; case "waning gibbous":mp="4";break;
                        case "third quarter":mp="5";break; case "waning crescent":mp="6"; break;
                    }

                    int moonImage = R.drawable.moon_new;
                    switch(mp) {
                        case "1":moonImage = R.drawable.moon_1;break; case"2":moonImage = R.drawable.moon_2;break;
                        case "3":moonImage = R.drawable.moon_3;break; case "full":moonImage = R.drawable.moon_full;break;
                        case "4":moonImage = R.drawable.moon_4;break; case "5":moonImage = R.drawable.moon_5;break;
                        case "6":moonImage = R.drawable.moon_6;break; case "new":moonImage = R.drawable.moon_new;break;
                    }

                    Bundle dataBundle = new Bundle();
                    dataBundle.putString("timeOfDay", otherParams.getString("timeOfDay"));
                    dataBundle.putString("dayOfWeek", otherParams.getString("dayOfWeek"));
                    dataBundle.putInt("date", date);
                    dataBundle.putInt("month", month);
                    dataBundle.putInt("hour", hour);
                    dataBundle.putInt("moonPhase", moonImage);
                    //testng
                    dataBundle.putInt("minute", minute);
                    dataBundle.putInt("second", second);

                    updateAppWidget(context, appWidgetManager, appWidgetId, dataBundle);
                }else {
                    Toast.makeText(context, "Moon response not found", Toast.LENGTH_SHORT).show();
                    System.out.println("something went wrong and you are stupid");
                }
            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_AUTO_UPDATE)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            onUpdate(context, manager, manager.getAppWidgetIds(new ComponentName(context.getPackageName(), getClass().getName())));
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        AppWidgetAlarm awa = new AppWidgetAlarm(context.getApplicationContext());
        awa.startAlarm();
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidgetComponentName = new ComponentName(context.getPackageName(),getClass().getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
        if (appWidgetIds.length == 0) {
            // stop alarm
            AppWidgetAlarm appWidgetAlarm = new AppWidgetAlarm(context.getApplicationContext());
            appWidgetAlarm.stopAlarm();
        }
    }
}