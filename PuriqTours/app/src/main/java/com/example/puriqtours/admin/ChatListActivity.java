package com.example.puriqtours.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.R;
import com.example.puriqtours.adapter.ChatListAdapter;
import com.example.puriqtours.cliente.ChatActivityDos;
import com.example.puriqtours.entity.ChatThread;
import com.example.puriqtours.helper.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    // 🔹 UI
    private RecyclerView recyclerViewChats;
    private TextInputEditText searchBar;

    // 🔹 Data
    private final List<ChatThread> chatList = new ArrayList<>();
    private ChatListAdapter adapter;

    // 🔹 Firebase
    private FirebaseFirestore db;

    // 🔹 Helpers
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        db = FirebaseFirestore.getInstance();
        notificationHelper = new NotificationHelper(this);

        initViews();
        setupRecycler();
        setupListeners();
        cargarChats();
        setupBottomNavigation();
    }

    // =========================================================================
    private void initViews() {
        recyclerViewChats = findViewById(R.id.recyclerViewChats);
        searchBar = findViewById(R.id.searchBar);

        ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v ->
                    Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show()
            );
        }
    }

    // =========================================================================
    private void setupRecycler() {
        adapter = new ChatListAdapter(chatList, chat -> {
            Intent intent = new Intent(this, ChatActivityDos.class);
            intent.putExtra("chatId", chat.getChatId());
            intent.putExtra("idCliente", chat.getIdCliente());
            intent.putExtra("idEmpresa", chat.getIdEmpresa());
            intent.putExtra("clientName", chat.getClientName());
            intent.putExtra("tourName", chat.getTourName());
            startActivity(intent);
        });

        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChats.setAdapter(adapter);
    }

    // =========================================================================
    private void cargarChats() {
        db.collection("chatThreads")
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    chatList.clear();
                    chatList.addAll(snapshots.toObjects(ChatThread.class));
                    adapter.notifyDataSetChanged();
                });
    }

    // =========================================================================
    private void setupListeners() {
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.filtrar(s.toString());
                }
            });
        }
    }

    // =========================================================================
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation == null) return;

        bottomNavigation.setSelectedItemId(R.id.nav_chat);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, MainAdminActivity.class));
                return true;
            }
            if (id == R.id.nav_reports) {
                startActivity(new Intent(this, ReportsActivity.class));
                return true;
            }
            if (id == R.id.nav_chat) {
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileAdminActivity.class));
                return true;
            }
            return false;
        });
    }
}
