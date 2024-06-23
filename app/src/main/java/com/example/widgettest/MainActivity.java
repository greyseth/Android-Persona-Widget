package com.example.widgettest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.widgettest.data.Keys;
import com.example.widgettest.models.API;
import com.example.widgettest.models.ApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public class MainActivity extends AppCompatActivity {

    Button moonBtn, weatherBtn, posBtn, locBtn;
    EditText latInput, longInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moonBtn = findViewById(R.id.moonBtn);
        weatherBtn = findViewById(R.id.weatherBtn);
        posBtn = findViewById(R.id.posBtn);
        locBtn = findViewById(R.id.locBtn);

        latInput = findViewById(R.id.latInput);
        longInput = findViewById(R.id.longInput);

        SharedPreferences sp = getSharedPreferences("defaultPosition", MODE_PRIVATE);
        latInput.setText(sp.getString("lat", ""));
        longInput.setText(sp.getString("long", ""));

        weatherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();

                SharedPreferences sp = getSharedPreferences("defaultPosition", MODE_PRIVATE);
                String latitude = sp.getString("lat", "");
                String longitude = sp.getString("long", "");

                if (latitude.isEmpty() || longitude.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Testing uses default coordinates", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = "https://api.weatherapi.com/v1/current.json?key=" + Keys.apiKey + "&q="+latitude+",%20"+longitude;
                API.get(MainActivity.this, url, null, new ApiResponse() {
                    @Override
                    public void onResponse(Optional<JSONObject> response, Bundle otherParams) throws JSONException {
                        if (response.isPresent()) {
                            String weather = response.get().getJSONObject("current").getJSONObject("condition").getString("text");
                            Toast.makeText(MainActivity.this, weather, Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "Response object not found", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        moonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();

                SharedPreferences sp = getSharedPreferences("defaultPosition", MODE_PRIVATE);
                String latitude = sp.getString("lat", "");
                String longitude = sp.getString("long", "");

                if (latitude.isEmpty() || longitude.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Testing uses default coordinates", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = "https://api.weatherapi.com/v1/astronomy.json?key=" + Keys.apiKey + "&q="+latitude+",%20"+longitude;
                API.get(MainActivity.this, url, null, new ApiResponse() {
                    @Override
                    public void onResponse(Optional<JSONObject> response, Bundle otherParams) throws JSONException {
                        if (response.isPresent()) {
                            String moonPhase = response.get().getJSONObject("astronomy").getJSONObject("astro").getString("moon_phase");
                            Toast.makeText(MainActivity.this, moonPhase, Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "Response object not found", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        posBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String latitude = latInput.getText().toString();
                String longitude = longInput.getText().toString();

                if (latitude.isEmpty() || longitude.isEmpty()) {
                    Toast.makeText(MainActivity.this, "You must fill in latidude and longitude values", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences sp = getSharedPreferences("defaultPosition", MODE_PRIVATE);
                SharedPreferences.Editor spEdit = sp.edit();
                spEdit.putString("lat", latitude);
                spEdit.putString("long", longitude);
                spEdit.apply();
            }
        });

        locBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                try {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                    }
                }catch(Exception e) {
                    Toast.makeText(MainActivity.this, "Something bad happened", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000 * 30, 100, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        Toast.makeText(MainActivity.this, "Latitude: "+location.getLatitude()+"\nLongitude: "+location.getLongitude(), Toast.LENGTH_SHORT).show();
                        locationManager.removeUpdates(this);
                    }
                });
            }
        });
    }
}