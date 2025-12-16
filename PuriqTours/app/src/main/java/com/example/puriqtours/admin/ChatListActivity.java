package com.example.puriqtours.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.helper.NotificationHelper;
import com.example.puriqtours.R;
import com.example.puriqtours.helper.StorageHelper;
import com.example.puriqtours.adapter.ChatAdminAdapter;
import com.example.puriqtours.entity.ChatAdmin;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChats;
    private ChatAdminAdapter chatAdminAdapter;
    private List<ChatAdmin> chatAdminList;
    private TextInputEditText searchBar;
    private StorageHelper storageHelper;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // Inicializar vistas
        initViews();
        
        // Crear datos de ejemplo
        createSampleData();
        
        // Configurar RecyclerView
        setupRecyclerView();
        
        // Configurar listeners
        setupListeners();
        
        // Configurar toolbar
        setupToolbar();
        
        // Configurar bottom navigation
        setupBottomNavigation();
    }

    private void initViews() {
        recyclerViewChats = findViewById(R.id.recyclerViewChats);
        searchBar = findViewById(R.id.searchBar);
    }

    private void createSampleData() {
        storageHelper = new StorageHelper(this);
        notificationHelper = new NotificationHelper(this);
        
        // Cargar chats desde SharedPreferences
        chatAdminList = storageHelper.loadChats();
    }

    private void setupRecyclerView() {
        chatAdminAdapter = new ChatAdminAdapter(this, chatAdminList);
        chatAdminAdapter.setOnChatClickListener(new ChatAdminAdapter.OnChatClickListener() {
            @Override
            public void onChatClick(ChatAdmin chatAdmin, int position) {
                // Abrir ChatActivity
                Intent intent = new Intent(ChatListActivity.this, ChatAdminActivity.class);
                intent.putExtra("chat_id", chatAdmin.getId());
                intent.putExtra("client_name", chatAdmin.getClientName());
                intent.putExtra("tour_name", chatAdmin.getTourName());
                startActivity(intent);
            }

            @Override
            public void onDeleteChat(ChatAdmin chatAdmin, int position) {
                // Eliminar chat del storage
                storageHelper.deleteChat(chatAdmin.getId());
                
                // Eliminar chat de la lista visual
                chatAdminAdapter.removeChat(position);
                Toast.makeText(ChatListActivity.this, 
                    "Chat con " + chatAdmin.getClientName() + " eliminado",
                    Toast.LENGTH_SHORT).show();
            }
        });
        
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChats.setAdapter(chatAdminAdapter);
    }

    private void setupListeners() {
        // Icono de notificaciones en toolbar
        ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                Intent intent = new Intent(ChatListActivity.this, NotificationsActivity.class);
                startActivity(intent);
            });
        }

        // Configurar toolbar navigation (botón de logout)
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                Toast.makeText(this, "Cerrar sesión", Toast.LENGTH_SHORT).show();
            });
        }

        // Configurar búsqueda en tiempo real
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (chatAdminAdapter != null) {
                        chatAdminAdapter.getFilter().filter(s);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_chat);
            
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_dashboard) {
                    startActivity(new Intent(this, MainAdminActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_reports) {
                    startActivity(new Intent(this, ReportsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_chat) {
                    return true; // Ya estás en chat
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileAdminActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }
    
    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> cerrarSesion());
        }
    }
    
    private void cerrarSesion() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                    android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(ChatListActivity.this, com.example.puriqtours.login.LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}