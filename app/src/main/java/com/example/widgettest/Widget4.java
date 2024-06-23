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
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.widgettest.data.Keys;
import com.example.widgettest.models.API;
import com.example.widgettest.models.ApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Optional;

/**
 * Implementation of App Widget functionality.
 */
public class Widget4 extends AppWidgetProvider {
    //Persona 4 Widget

    String ACTION_AUTO_UPDATE = "AUTO_UPDATE";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Bundle dataBundle) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget4);

        int date = dataBundle.getInt("date");
        int month = dataBundle.getInt("month");
        String dayOfWeek = dataBundle.getString("dayOfWeek");
        String timeOfDay = dataBundle.getString("timeOfDay");

        String dateString = date < 10 ? "0 " + date : String.valueOf(date);
        String monthString = month < 10 ? "0 " + month : String.valueOf(month);

        views.setTextViewText(R.id.dateDisplay, monthString + " / " + dateString);
        views.setTextViewText(R.id.dayDisplay, dayOfWeek);
        if (dayOfWeek.equals("SUN"))
            views.setTextColor(R.id.dayDisplay, Color.parseColor("#B50000"));
        else views.setTextColor(R.id.dayDisplay, Color.parseColor("#FFFFFFFF"));

        views.setTextViewText(R.id.timeDescription, timeOfDay);

        views.setImageViewResource(R.id.widgetBackground, dataBundle.getInt("backgroundImage"));

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

                    weatherRequest(context, appWidgetManager, appWidgetId, latitude, longitude);
                }else {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000 * 30, 100, new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location location) {
                            String latitude = String.valueOf(location.getLatitude());
                            String longitude = String.valueOf(location.getLongitude());

                            weatherRequest(context, appWidgetManager, appWidgetId, latitude, longitude);

                            lm.removeUpdates(this);
                        }
                    });
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void weatherRequest(Context context, AppWidgetManager appWidgetManager, int appWidgetId, String latitude, String longitude) {
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
            case Calendar.MONDAY:
                dayOfWeek = "MON";
                break;
            case Calendar.TUESDAY:
                dayOfWeek = "TUE";
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = "WED";
                break;
            case Calendar.THURSDAY:
                dayOfWeek = "THU";
                break;
            case Calendar.FRIDAY:
                dayOfWeek = "FRI";
                break;
            case Calendar.SATURDAY:
                dayOfWeek = "SAT";
                break;
            case Calendar.SUNDAY:
                dayOfWeek = "SUN";
                break;
        }

        if (hour > 0 && hour < 8) timeOfDay = "Morning";
        else if (hour >= 8 && hour < 12) timeOfDay = "Daytime";
        else if (hour >= 12 && hour < 14) timeOfDay = "Lunchtime";
        else if (hour >= 14 && hour < 16) timeOfDay = "Afternoon";
        else if (hour >= 16 && hour < 20) timeOfDay = "After School";
        else if (hour >= 20 && hour <= 23) timeOfDay = "Evening";

        //Makes request to API
        String weatherAPIurl = "https://api.weatherapi.com/v1/current.json?key="+ Keys.apiKey +"&q="+latitude+",%20"+longitude;
        Bundle requestParams = new Bundle();
        requestParams.putString("timeOfDay", timeOfDay);
        requestParams.putString("dayOfWeek", dayOfWeek);
        System.out.println("Making API request now");
        API.get(context, weatherAPIurl, requestParams, new ApiResponse() {
            @Override
            public void onResponse(Optional<JSONObject> response, Bundle otherParams) throws JSONException {
                if (response.isPresent()) {
                    int backgroundImage = R.drawable.bg_widget_4;

                    String weather = response.get().getJSONObject("current").getJSONObject("condition").getString("text").toLowerCase();
                    if (weather.contains("cloudy") || weather.contains("mist") || weather.contains("fog")) {
                        backgroundImage = R.drawable.bg_widget_4_fog;
                    }else if (weather.contains("rain")) backgroundImage = R.drawable.bg_widget_4_rain;

                    Bundle dataBundle = new Bundle();
                    dataBundle.putString("timeOfDay", otherParams.getString("timeOfDay"));
                    dataBundle.putString("dayOfWeek", otherParams.getString("dayOfWeek"));
                    dataBundle.putInt("date", date);
                    dataBundle.putInt("month", month+1);
                    dataBundle.putInt("hour", hour);
                    dataBundle.putInt("backgroundImage", backgroundImage);
                    //testng
                    dataBundle.putInt("minute", minute);
                    dataBundle.putInt("second", second);

                    System.out.println("Trying to update display");
                    updateAppWidget(context, appWidgetManager, appWidgetId, dataBundle);
                }else {
                    Toast.makeText(context, "Weather response not found", Toast.LENGTH_SHORT).show();
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