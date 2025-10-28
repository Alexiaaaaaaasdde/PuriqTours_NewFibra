package com.example.puriqtours.cliente;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puriqtours.BaseActivity;
import com.example.puriqtours.adapter.ChatAdapter;
import com.example.puriqtours.R;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity {

    private EditText etMensaje;
    private ImageButton btnSend, btnVoice, btnBack;
    private RecyclerView recyclerChat;
    private ChatAdapter chatAdapter;
    private List<String> mensajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 🔹 Activar navbar con las 3 rayitas (☰)
        setupDrawer();
        enableDrawerIcon(); // 👈 este método lo añadimos en BaseActivity

        // 🔹 Inicializar botón back (flecha debajo del toolbar)
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 👇 recibimos el estado desde el intent
        String estado = getIntent().getStringExtra("estado");

        // Dependiendo del estado, personalizas la vista
        TextView titulo = findViewById(R.id.tvSaludo);
        if (estado != null) {
            if (estado.equalsIgnoreCase("Reservado")) {
                titulo.setText("Chat - Reservado 🟢");
            } else if (estado.equalsIgnoreCase("En proceso")) {
                titulo.setText("Chat - En Proceso 🟠");
            } else if (estado.equalsIgnoreCase("Finalizado")) {
                titulo.setText("Chat - Finalizado 🔴");
            }
        }

        // 🔹 Inicializamos componentes del chat
        etMensaje = findViewById(R.id.etMensaje);
        btnSend = findViewById(R.id.btnSend);
        btnVoice = findViewById(R.id.btnVoice);
        recyclerChat = findViewById(R.id.recyclerChat);

        mensajes = new ArrayList<>();
        chatAdapter = new ChatAdapter(mensajes);

        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);

        // 🔹 Botón Enviar
        btnSend.setOnClickListener(v -> {
            String msg = etMensaje.getText().toString();
            if (!msg.isEmpty()) {
                mensajes.add("Tú: " + msg);
                chatAdapter.notifyItemInserted(mensajes.size() - 1);
                recyclerChat.scrollToPosition(mensajes.size() - 1);
                etMensaje.setText("");
            }
        });

        // 🔹 Botón Voz
        btnVoice.setOnClickListener(v ->
                Toast.makeText(this, "Funcionalidad de voz pendiente", Toast.LENGTH_SHORT).show()
        );
    }
}
