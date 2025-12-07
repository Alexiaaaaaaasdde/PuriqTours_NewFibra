package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.puriqtours.cliente.HistorialActivity;
import com.example.puriqtours.cliente.ProfileActivity;
import com.example.puriqtours.cliente.ToursActivity;
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
    protected NavigationView navigationView;
    protected MaterialToolbar toolbar;
    protected ShapeableImageView profileIconToolbar;

    protected FirebaseAuth auth;
    protected FirebaseFirestore db;
    protected String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            uid = auth.getCurrentUser().getUid();
        }
    }

    /** Toolbar unificado */
    protected void setupSharedToolbar() {

        toolbar = findViewById(R.id.topAppBar);
        profileIconToolbar = findViewById(R.id.profileIcon);

        if (toolbar == null || profileIconToolbar == null) return;

        setSupportActionBar(toolbar);

        toolbar.getMenu().clear();

        // Cargar imagen
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
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }


    protected void enableDrawerIcon() {
        if (toolbar != null && drawerLayout != null) {
            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }
    }

    protected void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    "puriqtours_channel",
                    "Reservas PuriqTours",
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notificaciones sobre reservas y pagos de tours");

            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
