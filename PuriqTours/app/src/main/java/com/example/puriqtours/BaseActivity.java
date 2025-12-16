package com.example.puriqtours;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.puriqtours.cliente.HistorialActivity;
import com.example.puriqtours.cliente.ProfileActivity;
import com.example.puriqtours.cliente.ToursActivity;
import com.example.puriqtours.helper.UserSessionManager;
import com.example.puriqtours.login.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected UserSessionManager sessionManager;
    protected NavigationView navigationView;
    protected MaterialToolbar toolbar;
    protected ShapeableImageView profileIconToolbar;

    protected FirebaseAuth auth;
    protected FirebaseFirestore db;
    protected String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel(); // 🔥 Canal de notificaciones

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new UserSessionManager(this);

        if (auth.getCurrentUser() != null) {
            uid = auth.getCurrentUser().getUid();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 3000);
            }
        }

        // 🔥 ESCUCHAR AUTOMÁTICAMENTE LAS NOTIFICACIONES DESDE FIRESTORE
        if (uid != null) {

            FirebaseFirestore.getInstance()
                    .collection("notificaciones")
                    .document(uid)
                    .addSnapshotListener((doc, err) -> {

                        if (doc != null && doc.exists()) {
                            String titulo = doc.getString("titulo");
                            String mensaje = doc.getString("mensaje");

                            if (titulo != null && mensaje != null) {
                                mostrarNotificacion(titulo, mensaje);
                            }
                        }
                    });
        }
    }

    /** Toolbar unificado */
    protected void setupSharedToolbar() {

        toolbar = findViewById(R.id.topAppBar);
        profileIconToolbar = findViewById(R.id.profileIcon);

        if (toolbar == null || profileIconToolbar == null) return;

        setSupportActionBar(toolbar);

        toolbar.getMenu().clear();

        // Cargar imagen del usuario
        if (uid != null) {
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        String url = doc.getString("profile_image");
                        if (url != null && !url.isEmpty()) {
                            Picasso.get().load(url).into(profileIconToolbar);
                        }
                    });
        }

        profileIconToolbar.setOnClickListener(v -> showLogoutMenu());
    }


    protected void setProfileIcon(String url) {
        if (profileIconToolbar != null && url != null && !url.isEmpty()) {
            Picasso.get().load(url).into(profileIconToolbar);
        }
    }

    private void showLogoutMenu() {
        PopupMenu popup = new PopupMenu(this, profileIconToolbar);
        popup.getMenu().add(1, 1, 1, "  Cerrar Sesión")
                .setIcon(R.drawable.ic_logout);

        try {
            Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object helper = field.get(popup);
            Method m = helper.getClass().getDeclaredMethod("setForceShowIcon", boolean.class);
            m.invoke(helper, true);
        } catch (Exception ignored) {}

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) cerrarSesion();
            return true;
        });

        popup.show();
    }

    private void cerrarSesion() {
        auth.signOut();
        sessionManager.clearSession();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }


    protected void enableDrawerIcon() {
        if (toolbar != null && drawerLayout != null) {
            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }
    }

    /** 🔥 CANAL DE NOTIFICACIONES DEL SISTEMA */
    protected void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "puriq",
                    "Puriq Tours",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones sobre reservas y pagos de tours");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    protected void mostrarNotificacion(String titulo, String mensaje) {

        // 1️⃣ Verificar permiso explícitamente para evitar warning
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Si no hay permiso, NO lanzar notificación
                return;
            }
        }

        // 2️⃣ Crear builder de notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "puriq")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 3️⃣ Envolver notify() en try/catch para eliminar warning del IDE
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(
                    (int) System.currentTimeMillis(),
                    builder.build()
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

}
