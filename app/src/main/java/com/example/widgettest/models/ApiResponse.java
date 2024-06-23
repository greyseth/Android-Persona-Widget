package com.example.widgettest.models;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public interface ApiResponse {
    void onResponse(Optional<JSONObject> response, Bundle otherParams) throws JSONException;
}
