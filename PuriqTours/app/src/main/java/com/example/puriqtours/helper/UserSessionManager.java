package com.example.puriqtours.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.puriqtours.entity.Usuario;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserSessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_UID = "uid";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NAME = "name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_BIRTHDATE = "birthdate";
    private static final String KEY_DOC_TYPE = "doc_type";
    private static final String KEY_DOCUMENT = "document";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_ROL = "rol";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_ACTIVITIES = "activities";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public UserSessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // 🔹 Guardar sesión desde un objeto Usuario
    public void saveUser(Usuario user) {
        if (user == null) return;

        editor.putString(KEY_UID, user.getUid());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_NAME, user.getName());
        editor.putString(KEY_LAST_NAME, user.getLast_name());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_BIRTHDATE, user.getBirthdate());
        editor.putString(KEY_DOC_TYPE, user.getDoc_type());
        editor.putString(KEY_DOCUMENT, user.getDocument());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_ADDRESS, user.getAddress());
        editor.putString(KEY_LANGUAGE, user.getLanguage());
        editor.putString(KEY_ROL, user.getRol());
        editor.putString(KEY_PROFILE_IMAGE, user.getProfile_image());

        if (user.getActivities() != null) {
            editor.putStringSet(KEY_ACTIVITIES, new HashSet<>(user.getActivities()));
        }

        editor.apply();
    }

    // 🔹 Obtener usuario actual
    public Usuario getUser() {
        Usuario user = new Usuario();
        user.setUid(prefs.getString(KEY_UID, null));
        user.setUsername(prefs.getString(KEY_USERNAME, null));
        user.setName(prefs.getString(KEY_NAME, null));
        user.setLast_name(prefs.getString(KEY_LAST_NAME, null));
        user.setEmail(prefs.getString(KEY_EMAIL, null));
        user.setBirthdate(prefs.getString(KEY_BIRTHDATE, null));
        user.setDoc_type(prefs.getString(KEY_DOC_TYPE, null));
        user.setDocument(prefs.getString(KEY_DOCUMENT, null));
        user.setPhone(prefs.getString(KEY_PHONE, null));
        user.setAddress(prefs.getString(KEY_ADDRESS, null));
        user.setLanguage(prefs.getString(KEY_LANGUAGE, "es"));
        user.setRol(prefs.getString(KEY_ROL,null));
        user.setProfile_image(prefs.getString(KEY_PROFILE_IMAGE,null));

        Set<String> actSet = prefs.getStringSet(KEY_ACTIVITIES, new HashSet<>());
        if (actSet != null) {
            user.setActivities(actSet.stream().collect(java.util.stream.Collectors.toList()));
        } else {
            user.setActivities(null);
        }
        return user;
    }

    public void debugPrintUser() {
        Map<String, ?> allPrefs = prefs.getAll();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            Log.d("USER_PREFS_DEBUG", entry.getKey() + " = " + entry.getValue());
        }
    }

    // 🔹 Verificar si hay sesión activa
    public boolean isLoggedIn() {
        return prefs.contains(KEY_UID);
    }

    // 🔹 Cerrar sesión
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    // 🔹 Obtener UID directamente
    public String getUid() {
        return prefs.getString(KEY_UID, null);
    }
}
