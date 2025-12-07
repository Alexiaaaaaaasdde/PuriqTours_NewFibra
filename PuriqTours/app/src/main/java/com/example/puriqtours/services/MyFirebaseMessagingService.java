package com.example.puriqtours.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.puriqtours.R;
import com.example.puriqtours.cliente.HistorialActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "puriq_tours_notifications";
    private static final String CHANNEL_NAME = "Puriq Tours";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Mensaje recibido de: " + remoteMessage.getFrom());

        // Verificar si el mensaje contiene notificación
        if (remoteMessage.getNotification() != null) {
            String titulo = remoteMessage.getNotification().getTitle();
            String mensaje = remoteMessage.getNotification().getBody();
            mostrarNotificacion(titulo, mensaje);
        }

        // Verificar si contiene datos
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Datos del mensaje: " + remoteMessage.getData());

            String tipo = remoteMessage.getData().get("type");

            if ("payment".equals(tipo)) {
                String titulo = remoteMessage.getData().get("title");
                String mensaje = remoteMessage.getData().get("message");
                mostrarNotificacion(titulo, mensaje);
            }
        }
    }

    private void mostrarNotificacion(String titulo, String mensaje) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Crear canal de notificación para Android O y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones de tours y pagos de Puriq Tours");
            channel.enableVibration(true);
            channel.enableLights(true);
            notificationManager.createNotificationChannel(channel);
        }

        // Intent para abrir la app al tocar la notificación
        Intent intent = new Intent(this, HistorialActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje))
                .setVibrate(new long[]{0, 500, 200, 500});

        // Mostrar notificación
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token FCM: " + token);

        // Guardar el token en Firestore para el usuario actual
        guardarTokenEnFirestore(token);
    }

    private void guardarTokenEnFirestore(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "Token FCM guardado exitosamente"))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error al guardar token FCM", e));
        }
    }
}
