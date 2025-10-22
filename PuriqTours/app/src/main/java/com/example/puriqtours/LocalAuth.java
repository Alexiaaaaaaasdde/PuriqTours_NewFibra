package com.example.puriqtours;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalAuth {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_NAME = "name";
    private static final String KEY_LASTNAME = "lastname";
    private static final String KEY_IS_LOGGED = "isLogged";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public LocalAuth(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Guardar datos
    public void saveUser(String email, String password, String name, String lastname) {
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_LASTNAME, lastname);
        editor.putBoolean(KEY_IS_LOGGED, true);
        editor.apply();
    }

    // Obtener datos
    public String getEmail() { return prefs.getString(KEY_EMAIL, ""); }
    public String getPassword() { return prefs.getString(KEY_PASSWORD, ""); }
    public String getName() { return prefs.getString(KEY_NAME, ""); }
    public String getLastname() { return prefs.getString(KEY_LASTNAME, ""); }
    public boolean isLogged() { return prefs.getBoolean(KEY_IS_LOGGED, false); }

    // Cerrar sesión
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
