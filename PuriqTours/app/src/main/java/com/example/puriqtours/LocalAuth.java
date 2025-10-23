package com.example.puriqtours;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalAuth {
    private static final String PREF_NAME = "UserPrefs";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public LocalAuth(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Guardar datos básicos
    public void saveUser(String email, String password, String name, String lastname) {
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putString("name", name);
        editor.putString("lastname", lastname);
        editor.putBoolean("isLogged", true);
        editor.apply();
    }

    // Extensión: nickname, idioma, foto, actividades
    public void saveProfile(String nickname, String language, String activities, String photoUri) {
        editor.putString("nickname", nickname);
        editor.putString("language", language);
        editor.putString("activities", activities);
        editor.putString("photoUri", photoUri);
        editor.apply();
    }

    // Getters
    public String getEmail() { return prefs.getString("email", ""); }
    public String getPassword() { return prefs.getString("password", ""); }
    public String getName() { return prefs.getString("name", ""); }
    public String getLastname() { return prefs.getString("lastname", ""); }
    public String getNickname() { return prefs.getString("nickname", ""); }
    public String getLanguage() { return prefs.getString("language", ""); }
    public String getActivities() { return prefs.getString("activities", ""); }
    public String getPhotoUri() { return prefs.getString("photoUri", ""); }
    public boolean isLogged() { return prefs.getBoolean("isLogged", false); }

    // 🔹 Verificar si un correo existe
    public boolean userExists(String email) {
        String savedEmail = prefs.getString("email", "");
        return savedEmail.equalsIgnoreCase(email);
    }

    // 🔹 Actualizar contraseña de un usuario
    public void updatePassword(String email, String newPassword) {
        String savedEmail = prefs.getString("email", "");
        if (savedEmail.equalsIgnoreCase(email)) {
            editor.putString("password", newPassword);
            editor.apply();
        }
    }


    // Limpiar sesión
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
