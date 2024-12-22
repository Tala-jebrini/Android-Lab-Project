package com.example.a1200493_courseproject;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "My_Shared_Preferences";
    private static final int SHARED_PREF_PRIVATE = Context.MODE_PRIVATE;
    private static SharedPrefManager ourInstance = null;
    private static SharedPreferences sharedPreferences = null;
    private SharedPreferences.Editor editor = null;

    // Singleton instance
    public static SharedPrefManager getInstance(Context context) {
        if (ourInstance != null) {
            return ourInstance;
        }
        ourInstance = new SharedPrefManager(context);
        return ourInstance;
    }

    // Private constructor for Singleton
    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, SHARED_PREF_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Write a String value
    public boolean writeString(String key, String value) {
        editor.putString(key, value);
        return editor.commit();
    }

    // Read a String value
    public String readString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public boolean setBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        return editor.commit();
    }
    public boolean getBoolean(String key, boolean defaultValue) {

        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }

}
