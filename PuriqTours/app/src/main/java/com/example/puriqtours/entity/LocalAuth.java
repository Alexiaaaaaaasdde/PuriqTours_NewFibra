package com.example.puriqtours.entity;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalAuth {
    private static final String PREF_NAME = "UserAuth";
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public LocalAuth(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // 🔹 Guardar datos
    public void saveUser(String name, String lastname, String email, String password,
                         String birthdate, String document, String phone,
                         String address, String docType, String language, String activities, String photoUri) {

        editor.putString("name", name);
        editor.putString("lastname", lastname);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putString("birthdate", birthdate);
        editor.putString("document", document);
        editor.putString("phone", phone);
        editor.putString("address", address);
        editor.putString("docType", docType);
        editor.putString("language", language);
        editor.putString("activities", activities);
        editor.putString("photoUri", photoUri);
        editor.apply();
    }

    // 🔹 Validar login
    public boolean login(String email, String password) {
        String savedEmail = prefs.getString("email", null);
        String savedPass = prefs.getString("password", null);
        return email.equals(savedEmail) && password.equals(savedPass);
    }

    // 🔹 Obtener datos
    public String getName() { return prefs.getString("name", ""); }
    public String getLastname() { return prefs.getString("lastname", ""); }
    public String getEmail() { return prefs.getString("email", ""); }
    public String getPassword() { return prefs.getString("password", ""); }
    public String getBirthdate() { return prefs.getString("birthdate", ""); }
    public String getDocument() { return prefs.getString("document", ""); }
    public String getPhone() { return prefs.getString("phone", ""); }
    public String getAddress() { return prefs.getString("address", ""); }
    public String getDocType() { return prefs.getString("docType", ""); }
    public String getLanguage() { return prefs.getString("language", ""); }
    public String getActivities() { return prefs.getString("activities", ""); }
    public String getPhotoUri() { return prefs.getString("photoUri", ""); }

    // 🔹 Cerrar sesión
    public void logout() {
        editor.clear();
        editor.apply();
    }

    // 🔹 Guardar el estado de sesión iniciada
    public void setLogged(boolean value) {
        editor.putBoolean("isLogged", value);
        editor.apply();
    }

    // 🔹 Saber si hay una sesión activa
    public boolean isLogged() {
        return prefs.getBoolean("isLogged", false);
    }

    // 🔹 Verifica si existe un usuario registrado con ese correo
    public boolean userExists(String email) {
        String savedEmail = prefs.getString("email", null);
        return savedEmail != null && savedEmail.equalsIgnoreCase(email.trim());
    }

    // 🔹 Actualiza la contraseña de un usuario existente
    public void updatePassword(String email, String newPassword) {
        String savedEmail = prefs.getString("email", null);

        // Solo actualiza si el correo coincide
        if (savedEmail != null && savedEmail.equalsIgnoreCase(email.trim())) {
            editor.putString("password", newPassword);
            editor.apply();
        }
    }


}
