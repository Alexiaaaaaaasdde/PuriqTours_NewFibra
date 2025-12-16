package com.example.puriqtours.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.adapters.NotificationAdapter;
import com.example.puriqtours.entity.Notification;
import com.example.puriqtours.utils.NotificationHelper;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerNotifications;
    private NotificationAdapter adapter;
    private NotificationHelper notificationHelper;
    private LinearLayout tvEmptyState;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnBack = findViewById(R.id.btnBack);
        
        notificationHelper = new NotificationHelper(this);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new NotificationAdapter(this, notification -> {
            // Marcar como leída
            notificationHelper.markAsRead(notification.getId());
            
            // Abrir detalle del tour
            Intent intent = new Intent(this, TourDetailActivity.class);
            intent.putExtra("tourId", notification.getTourId());
            startActivity(intent);
        }, notification -> {
            // Eliminar notificación
            notificationHelper.deleteNotification(notification.getId());
            loadNotifications(); // Recargar lista
        });
        
        recyclerNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        List<Notification> notifications = notificationHelper.getAllNotifications();
        
        if (notifications.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerNotifications.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerNotifications.setVisibility(View.VISIBLE);
            adapter.updateData(notifications);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar notificaciones al volver a la actividad
        loadNotifications();
    }
}
