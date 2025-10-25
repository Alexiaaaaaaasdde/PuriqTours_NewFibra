package com.example.puriqtours;

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
    
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para tours
            NotificationChannel toursChannel = new NotificationChannel(
                CHANNEL_TOURS,
                "Tours",
                NotificationManager.IMPORTANCE_HIGH
            );
            toursChannel.setDescription("Notificaciones sobre tours creados y actividades");
            toursChannel.enableVibration(true);
            toursChannel.enableLights(true);
            
            // Canal para chats
            NotificationChannel chatsChannel = new NotificationChannel(
                CHANNEL_CHATS,
                "Mensajes de Chat",
                NotificationManager.IMPORTANCE_HIGH
            );
            chatsChannel.setDescription("Notificaciones de nuevos mensajes de clientes");
            chatsChannel.enableVibration(true);
            chatsChannel.enableLights(true);
            
            // Canal para guías
            NotificationChannel guidesChannel = new NotificationChannel(
                CHANNEL_GUIDES,
                "Guías",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            guidesChannel.setDescription("Notificaciones sobre guías y actividades");
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(toursChannel);
            manager.createNotificationChannel(chatsChannel);
            manager.createNotificationChannel(guidesChannel);
        }
    }
    
    // Notificación cuando se crea un tour exitosamente
    public void notifyTourCreated(String tourName, String destination) {
        Intent intent = new Intent(context, ToursActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TOURS)
            .setSmallIcon(R.drawable.ic_tour)
            .setContentTitle("✅ Tour creado exitosamente")
            .setContentText(tourName + " • " + destination)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("El tour '" + tourName + "' con destino a " + destination + " ha sido creado exitosamente y está disponible para reservas."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true);
        
        if (hasNotificationPermission()) {
            notificationManager.notify(1001, builder.build());
        }
    }
    
    // Notificación cuando se propone un tour a un guía
    public void notifyTourProposedToGuide(String tourName, String guideName, String destination) {
        Intent intent = new Intent(context, GuidesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GUIDES)
            .setSmallIcon(R.drawable.ic_guide)
            .setContentTitle("📋 Tour propuesto a guía")
            .setContentText("Tour: " + tourName + " • Guía: " + guideName)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("Se ha propuesto el tour '" + tourName + "' en " + destination + " al guía " + guideName + ". Esperando respuesta."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true);
        
        if (hasNotificationPermission()) {
            notificationManager.notify(1002, builder.build());
        }
    }
    
    // Notificación para nuevo mensaje de chat
    public void notifyChatMessage(String clientName, String message, String chatId) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("client_name", clientName);
        intent.putExtra("chat_id", chatId);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );
        
        // Limitar el mensaje para el preview
        String shortMessage = message.length() > 50 ? message.substring(0, 47) + "..." : message;
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_CHATS)
            .setSmallIcon(R.drawable.ic_chat)
            .setContentTitle("💬 " + clientName)
            .setContentText(shortMessage)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message)
                .setBigContentTitle("Nuevo mensaje de " + clientName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE);
        
        if (hasNotificationPermission()) {
            // Usar un ID único basado en el cliente para que cada chat tenga su propia notificación
            int notificationId = Math.abs((clientName + chatId).hashCode());
            notificationManager.notify(notificationId, builder.build());
        }
    }
    
    // Notificación para guía agregado
    public void notifyGuideAdded(String guideName, String specialty) {
        Intent intent = new Intent(context, GuidesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GUIDES)
            .setSmallIcon(R.drawable.ic_guide) // Usaremos un ícono existente
            .setContentTitle("Nuevo guía registrado")
            .setContentText(guideName + " - " + specialty)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("El guía " + guideName + " especializado en " + specialty + " ha sido registrado en el sistema."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        if (hasNotificationPermission()) {
            notificationManager.notify(1003, builder.build());
        }
    }
    
    // Simulación de mensaje entrante (para demostración)
    public void simulateIncomingMessage() {
        String[] clients = {"María García", "Carlos López", "Ana Martínez", "Pedro Silva", "Lucia Ramírez"};
        String[] messages = {
            "Hola, ¿tienen disponibilidad para el tour a Machu Picchu el próximo fin de semana?",
            "¿Podrían enviarme más información sobre los precios?",
            "Tengo una pregunta sobre la cancelación del tour",
            "¿El tour incluye almuerzo?",
            "¿A qué hora es el punto de encuentro?",
            "¿Necesito llevar algo especial para el tour?",
            "Buenos días, quería consultar sobre el tour al Valle Sagrado",
            "¿Cuánto tiempo dura el recorrido?",
            "¿Hay descuentos para grupos?"
        };
        
        String randomClient = clients[(int) (Math.random() * clients.length)];
        String randomMessage = messages[(int) (Math.random() * messages.length)];
        String chatId = "chat_" + System.currentTimeMillis();
        
        notifyChatMessage(randomClient, randomMessage, chatId);
    }
    
    // Simulación para demostrar todas las notificaciones
    public void simulateAllNotifications() {
        // Simular creación de tour
        notifyTourCreated("Tour Machu Picchu Premium", "Cusco");
        
        // Simular propuesta a guía (con delay para que no aparezcan todas juntas)
        new android.os.Handler().postDelayed(() -> {
            notifyTourProposedToGuide("Tour Valle Sagrado", "Carlos Mendoza", "Cusco");
        }, 2000);
        
        // Simular mensaje de chat (con delay)
        new android.os.Handler().postDelayed(() -> {
            simulateIncomingMessage();
        }, 4000);
    }
    
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(context, 
                android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Para versiones anteriores no se necesita permiso explícito
    }
}