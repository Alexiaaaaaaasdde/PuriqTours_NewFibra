package com.example.puriqtours;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.adapter.ChatAdapter;
import com.example.puriqtours.model.Chat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChats;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;
    private TextInputEditText searchBar;

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
        
        // Configurar bottom navigation
        setupBottomNavigation();
    }

    private void initViews() {
        recyclerViewChats = findViewById(R.id.recyclerViewChats);
        searchBar = findViewById(R.id.searchBar);
    }

    private void createSampleData() {
        chatList = new ArrayList<>();
        chatList.add(new Chat(1, "María González", "Hola, tengo una consulta sobre el tour", "10:30", true, 2, "Tour Machu Picchu"));
        chatList.add(new Chat(2, "Carlos Mendoza", "¿A qué hora es el punto de encuentro?", "09:15", true, 1, "Tour Kuelap"));
        chatList.add(new Chat(3, "Ana Torres", "Gracias por la información", "Ayer", false, 0, "Tour Lima Colonial"));
        chatList.add(new Chat(4, "Pedro Silva", "¿Puedo reprogramar mi tour?", "08:45", true, 3, "Tour Arequipa"));
        chatList.add(new Chat(5, "Lucía Ramírez", "Perfecto, nos vemos mañana", "Lunes", false, 0, "Tour Cusco"));
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(this, chatList);
        chatAdapter.setOnChatClickListener(new ChatAdapter.OnChatClickListener() {
            @Override
            public void onChatClick(Chat chat, int position) {
                // Abrir ChatActivity
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra("chat_id", chat.getId());
                intent.putExtra("client_name", chat.getClientName());
                intent.putExtra("tour_name", chat.getTourName());
                startActivity(intent);
            }

            @Override
            public void onDeleteChat(Chat chat, int position) {
                // Eliminar chat de la lista
                chatAdapter.removeChat(position);
                Toast.makeText(ChatListActivity.this, 
                    "Chat con " + chat.getClientName() + " eliminado", 
                    Toast.LENGTH_SHORT).show();
            }
        });
        
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChats.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        // Icono de notificaciones en toolbar
        ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show();
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
                    if (chatAdapter != null) {
                        chatAdapter.getFilter().filter(s);
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
                    startActivity(new Intent(this, MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_reports) {
                    startActivity(new Intent(this, ReportsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_chat) {
                    return true; // Ya estás en chat
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }
}