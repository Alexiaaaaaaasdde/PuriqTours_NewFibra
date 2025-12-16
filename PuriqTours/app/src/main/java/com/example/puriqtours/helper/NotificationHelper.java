package com.example.puriqtours.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.puriqtours.R;
import com.example.puriqtours.admin.GuidesActivity;
import com.example.puriqtours.cliente.ChatActivityDos;
import com.example.puriqtours.cliente.ToursActivity;

public class NotificationHelper {

    private static final String CHANNEL_TOURS = "tours_channel";
    private static final String CHANNEL_CHATS = "chats_channel";
    private static final String CHANNEL_GUIDES = "guides_channel";

    private Context context;
    private NotificationManagerCompat notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannels();
    }

    // =========================================================
    // 🔔 CREAR CANALES
    // =========================================================
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel toursChannel = new NotificationChannel(
                    CHANNEL_TOURS,
                    "Tours",
                    NotificationManager.IMPORTANCE_HIGH
            );
            toursChannel.setDescription("Notificaciones sobre tours creados y actividades");
            toursChannel.enableVibration(true);
            toursChannel.enableLights(true);

            NotificationChannel chatsChannel = new NotificationChannel(
                    CHANNEL_CHATS,
                    "Mensajes de Chat",
                    NotificationManager.IMPORTANCE_HIGH
            );
            chatsChannel.setDescription("Notificaciones de nuevos mensajes de chat");
            chatsChannel.enableVibration(true);
            chatsChannel.enableLights(true);

            NotificationChannel guidesChannel = new NotificationChannel(
                    CHANNEL_GUIDES,
                    "Guías",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            guidesChannel.setDescription("Notificaciones sobre guías y actividades");

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(toursChannel);
            manager.createNotificationChannel(chatsChannel);
            manager.createNotificationChannel(guidesChannel);
        }
    }

    // =========================================================
    // 🗺️ TOUR CREADO
    // =========================================================
    public void notifyTourCreated(String tourName, String destination) {

        Intent intent = new Intent(context, ToursActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_TOURS)
                        .setSmallIcon(R.drawable.ic_tour)
                        .setContentTitle("✅ Tour creado exitosamente")
                        .setContentText(tourName + " • " + destination)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("El tour '" + tourName +
                                        "' con destino a " + destination +
                                        " ha sido creado exitosamente y está disponible para reservas."))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

        if (hasNotificationPermission()) {
            notificationManager.notify(1001, builder.build());
        }
    }

    // =========================================================
    // 🧑‍🏫 TOUR PROPUESTO A GUÍA
    // =========================================================
    public void notifyTourProposedToGuide(String tourName, String guideName, String destination) {

        Intent intent = new Intent(context, GuidesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_GUIDES)
                        .setSmallIcon(R.drawable.ic_guide)
                        .setContentTitle("📋 Tour propuesto a guía")
                        .setContentText("Tour: " + tourName + " • Guía: " + guideName)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Se ha propuesto el tour '" + tourName +
                                        "' en " + destination +
                                        " al guía " + guideName + "."))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);

        if (hasNotificationPermission()) {
            notificationManager.notify(1002, builder.build());
        }
    }

    // =========================================================
    // 💬 MENSAJE DE CHAT (CLIENTE ↔ ADMIN)
    // =========================================================
    public void notifyChatMessage(
            String chatId,
            String idCliente,
            String idEmpresa,
            String idReserva,
            String clientName,
            String tourName,
            String message
    ) {

        Intent intent = new Intent(context, ChatActivityDos.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("idCliente", idCliente);
        intent.putExtra("idEmpresa", idEmpresa);
        intent.putExtra("idReserva", idReserva);
        intent.putExtra("clientName", clientName);
        intent.putExtra("tourName", tourName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                chatId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String shortMessage =
                message.length() > 50 ? message.substring(0, 47) + "..." : message;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_CHATS)
                        .setSmallIcon(R.drawable.ic_chat)
                        .setContentTitle("💬 " + clientName)
                        .setContentText(shortMessage)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message)
                                .setBigContentTitle("Mensaje sobre " + tourName))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        if (hasNotificationPermission()) {
            int notificationId = Math.abs(chatId.hashCode());
            notificationManager.notify(notificationId, builder.build());
        }
    }

    // =========================================================
    // ➕ GUÍA AGREGADO
    // =========================================================
    public void notifyGuideAdded(String guideName, String specialty) {

        Intent intent = new Intent(context, GuidesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_GUIDES)
                        .setSmallIcon(R.drawable.ic_guide)
                        .setContentTitle("Nuevo guía registrado")
                        .setContentText(guideName + " - " + specialty)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("El guía " + guideName +
                                        " especializado en " + specialty +
                                        " ha sido registrado en el sistema."))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        if (hasNotificationPermission()) {
            notificationManager.notify(1003, builder.build());
        }
    }

    // =========================================================
    // 🧪 SIMULACIÓN DE MENSAJE (DEMO)
    // =========================================================
    public void simulateIncomingMessage() {

        String chatId = "chat_demo_001";
        String idCliente = "cliente_demo";
        String idEmpresa = "empresa_demo";
        String idReserva = "reserva_demo";
        String clientName = "Cliente Demo";
        String tourName = "Tour Machu Picchu";
        String message = "Hola, tengo una consulta sobre el tour 😊";

        notifyChatMessage(
                chatId,
                idCliente,
                idEmpresa,
                idReserva,
                clientName,
                tourName,
                message
        );
    }

    // =========================================================
    // 🧪 SIMULACIÓN COMPLETA
    // =========================================================
    public void simulateAllNotifications() {

        notifyTourCreated("Tour Machu Picchu Premium", "Cusco");

        new android.os.Handler().postDelayed(() ->
                notifyTourProposedToGuide(
                        "Tour Valle Sagrado",
                        "Carlos Mendoza",
                        "Cusco"
                ), 2000);

        new android.os.Handler().postDelayed(this::simulateIncomingMessage, 4000);
    }

    // =========================================================
    // 🔐 PERMISOS ANDROID 13+
    // =========================================================
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}
