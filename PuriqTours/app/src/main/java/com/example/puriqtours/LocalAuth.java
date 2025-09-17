package com.example.puriqtours;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalAuth {
    private SharedPreferences prefs;

    public LocalAuth(Context context) {
        prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    // Verifica si existe un usuario
    public boolean userExists(String email) {
        return prefs.contains(email);
    }

    // Actualiza la contraseña de un usuario
    public void updatePassword(String email, String newPassword) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(email, newPassword);
        editor.apply();
    }

    // Para registrar usuarios (lo puedes usar en tu registro)
    public void registerUser(String email, String password) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(email, password);
        editor.apply();
    }

    // Obtener contraseña (debug o login)
    public String getPassword(String email) {
        return prefs.getString(email, null);
    }
}
