package com.example.puriqtours.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.puriqtours.entity.Notification;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificationHelper {
    
    private static final String PREFS_NAME = "NotificationsPrefs";
    private static final String KEY_NOTIFICATIONS = "notifications_list";
    
    private SharedPreferences prefs;
    private Gson gson;
    
    public NotificationHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    /**
     * Agregar una nueva notificación
     */
    public void addNotification(String type, String tourId, String tourName, 
                               String reservaId, String guideName, int clientCount) {
        List<Notification> notifications = getAllNotifications();
        
        String id = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        
        Notification notification = new Notification(
            id, type, tourId, tourName, reservaId, guideName, clientCount, timestamp
        );
        
        notifications.add(0, notification); // Agregar al inicio
        saveNotifications(notifications);
    }
    
    /**
     * Obtener todas las notificaciones
     */
    public List<Notification> getAllNotifications() {
        String json = prefs.getString(KEY_NOTIFICATIONS, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<Notification>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    /**
     * Obtener notificaciones no leídas
     */
    public List<Notification> getUnreadNotifications() {
        List<Notification> all = getAllNotifications();
        List<Notification> unread = new ArrayList<>();
        
        for (Notification n : all) {
            if (!n.isRead()) {
                unread.add(n);
            }
        }
        return unread;
    }
    
    /**
     * Contar notificaciones no leídas
     */
    public int getUnreadCount() {
        return getUnreadNotifications().size();
    }
    
    /**
     * Marcar una notificación como leída
     */
    public void markAsRead(String notificationId) {
        List<Notification> notifications = getAllNotifications();
        
        for (Notification n : notifications) {
            if (n.getId().equals(notificationId)) {
                n.setRead(true);
                break;
            }
        }
        
        saveNotifications(notifications);
    }
    
    /**
     * Marcar todas como leídas
     */
    public void markAllAsRead() {
        List<Notification> notifications = getAllNotifications();
        
        for (Notification n : notifications) {
            n.setRead(true);
        }
        
        saveNotifications(notifications);
    }
    
    /**
     * Eliminar una notificación
     */
    public void deleteNotification(String notificationId) {
        List<Notification> notifications = getAllNotifications();
        notifications.removeIf(n -> n.getId().equals(notificationId));
        saveNotifications(notifications);
    }
    
    /**
     * Eliminar todas las notificaciones
     */
    public void clearAll() {
        prefs.edit().remove(KEY_NOTIFICATIONS).apply();
    }
    
    /**
     * Guardar la lista de notificaciones
     */
    private void saveNotifications(List<Notification> notifications) {
        String json = gson.toJson(notifications);
        prefs.edit().putString(KEY_NOTIFICATIONS, json).apply();
    }
}
