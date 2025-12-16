package com.example.puriqtours.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class WelcomePrefs {

    private static final String PREFS = "welcome_prefs";
    private static final String KEY_SEEN = "has_seen_welcome";

    public static void markSeen(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_SEEN, true)
                .apply();
    }

    public static boolean hasSeen(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_SEEN, false);
    }
}
