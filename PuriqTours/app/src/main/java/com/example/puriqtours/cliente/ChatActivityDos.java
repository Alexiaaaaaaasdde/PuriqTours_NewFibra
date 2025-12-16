package com.example.puriqtours.cliente;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.puriqtours.R;
import com.example.puriqtours.adapter.ChatAdapterDos;
import com.example.puriqtours.entity.ChatMessage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivityDos extends AppCompatActivity {

    // 🔹 UI
    private RecyclerView recyclerChat;
    private EditText etMensaje;
    private ImageButton btnSend, btnVoice;
    private ShapeableImageView profileIcon;
    private MaterialToolbar topAppBar;

    // 🔹 Chat
    private ChatAdapterDos adapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    // 🔹 Firebase
    private FirebaseFirestore db;
    private String currentUserId;

    // 🔹 Datos del chat
    private String chatId;
    private String idCliente;
    private String idEmpresa;
    private String clientName;
    private String tourName;
    private String senderRole; // 👈 NUEVO

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // ================= Firebase =================
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        // ================= Obtener rol del usuario =================
        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        senderRole = doc.getString("rol");
                    }
                });

        // ================= Views =================
        recyclerChat = findViewById(R.id.recyclerChat);
        etMensaje = findViewById(R.id.etMensaje);
        btnSend = findViewById(R.id.btnSend);
        btnVoice = findViewById(R.id.btnVoice);
        profileIcon = findViewById(R.id.profileIcon);
        topAppBar = findViewById(R.id.topAppBar);

        // ================= Intent =================
        chatId = getIntent().getStringExtra("chatId");
        idCliente = getIntent().getStringExtra("idCliente");
        idEmpresa = getIntent().getStringExtra("idEmpresa");
        clientName = getIntent().getStringExtra("clientName");
        tourName = getIntent().getStringExtra("tourName");

        // ================= Recycler =================
        adapter = new ChatAdapterDos(messageList, currentUserId);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(adapter);

        // ================= Cargar datos =================
        cargarEmpresa();
        escucharMensajes();

        btnSend.setOnClickListener(v -> enviarMensaje());
    }






    // =========================================================================
    // 🔥 CARGAR EMPRESA (NOMBRE + FOTO)
    // =========================================================================
    private void cargarEmpresa() {
        db.collection("users")
                .document(idEmpresa)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String nombreEmpresa = doc.getString("name");
                    String fotoUrl = doc.getString("profile_image");

                    // 🔹 Nombre en toolbar
                    if (nombreEmpresa != null) {
                        topAppBar.setTitle(nombreEmpresa);
                    }

                    // 🔹 Foto empresa
                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        Glide.with(this)
                                .load(fotoUrl)
                                .placeholder(R.drawable.imagen_perfil)
                                .error(R.drawable.imagen_perfil)
                                .into(profileIcon);
                    }
                });
    }

    // =========================================================================
    // 🔥 ESCUCHAR MENSAJES
    // =========================================================================
    private void escucharMensajes() {
        db.collection("chatThreads")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null || snapshots == null) return;

                    messageList.clear();
                    messageList.addAll(snapshots.toObjects(ChatMessage.class));
                    adapter.notifyDataSetChanged();

                    recyclerChat.scrollToPosition(messageList.size() - 1);
                });
    }

    // =========================================================================
    // ✉️ ENVIAR MENSAJE
    // =========================================================================
    private void enviarMensaje() {

        String text = etMensaje.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        if (senderRole == null) return; // seguridad

        ChatMessage message = new ChatMessage(
                currentUserId,
                senderRole,   // ✅ DINÁMICO
                text
        );

        DocumentReference chatRef =
                db.collection("chatThreads").document(chatId);

        chatRef.collection("messages")
                .add(message)
                .addOnSuccessListener(doc -> {
                    etMensaje.setText("");
                    actualizarThread(text);
                });
    }

    // =========================================================================
    // 🔄 ACTUALIZAR THREAD
    // =========================================================================
    private void actualizarThread(String lastMessage) {

        Map<String, Object> update = new HashMap<>();
        update.put("lastMessage", lastMessage);
        update.put("lastTimestamp", Timestamp.now());

        db.collection("chatThreads")
                .document(chatId)
                .update(update);
    }
}
