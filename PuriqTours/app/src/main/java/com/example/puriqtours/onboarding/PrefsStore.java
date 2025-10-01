package com.example.puriqtours.onboarding;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PrefsStore {
    private static final String PREFS = "user_onboarding";
    private static final String KEY_LANGS = "langs";
    private static final String KEY_REGIONS = "regions";
    private static final String KEY_PREFS = "prefs";
    private final SharedPreferences sp;

    public PrefsStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(UserPreferences up) {
        sp.edit()
                .putString(KEY_LANGS, toJson(up.languages))
                .putString(KEY_REGIONS, toJson(up.regions))
                .putString(KEY_PREFS, toJson(up.preferences))
                .apply();
    }

    public UserPreferences load() {
        UserPreferences u = new UserPreferences();
        u.languages = fromJson(sp.getString(KEY_LANGS, "[]"));
        u.regions = fromJson(sp.getString(KEY_REGIONS, "[]"));
        u.preferences = fromJson(sp.getString(KEY_PREFS, "[]"));
        return u;
    }

    private static String toJson(List<String> list) {
        return new JSONArray(list).toString();
    }

    private static ArrayList<String> fromJson(String json) {
        ArrayList<String> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i=0; i<arr.length(); i++) out.add(arr.getString(i));
        } catch (JSONException ignored) {}
        return out;
    }
}
