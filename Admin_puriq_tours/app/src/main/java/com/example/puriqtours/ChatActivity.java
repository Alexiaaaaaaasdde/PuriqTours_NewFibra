package com.example.puriqtours;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private EditText etMensaje;
    private ImageButton btnSend, btnVoice, btnBack;
    private RecyclerView recyclerChat;
    private ChatMessageAdapter chatMessageAdapter;
    private List<ChatMessage> mensajes;
    private TextView tvClientName, tvTourName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Recibir datos del intent
        String clientName = getIntent().getStringExtra("client_name");
        String tourName = getIntent().getStringExtra("tour_name");
        int chatId = getIntent().getIntExtra("chat_id", 0);

        // Inicializar vistas
        initViews();
        
        // Configurar información del chat
        setupChatInfo(clientName, tourName);
        
        // Crear mensajes de ejemplo
        createSampleMessages(clientName);
        
        // Configurar RecyclerView
        setupRecyclerView();
        
        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        etMensaje = findViewById(R.id.etMensaje);
        btnSend = findViewById(R.id.btnSend);
        btnVoice = findViewById(R.id.btnVoice);
        btnBack = findViewById(R.id.btnBack);
        recyclerChat = findViewById(R.id.recyclerChat);
        tvClientName = findViewById(R.id.tvClientName);
        tvTourName = findViewById(R.id.tvTourName);
    }

    private void setupChatInfo(String clientName, String tourName) {
        if (tvClientName != null && clientName != null) {
            tvClientName.setText("Chat con " + clientName);
        }
        if (tvTourName != null && tourName != null) {
            tvTourName.setText("Tour: " + tourName);
        }
    }

    private void createSampleMessages(String clientName) {
        mensajes = new ArrayList<>();
        
        // Mensajes de simulación
        String nombreCliente = clientName != null ? clientName : "Cliente";
        
        mensajes.add(new ChatMessage(nombreCliente + ": Hola, tengo una consulta sobre el tour", false));
        mensajes.add(new ChatMessage("Tú: ¡Hola! Estoy aquí para ayudarte. ¿Cuál es tu consulta?", true));
        mensajes.add(new ChatMessage(nombreCliente + ": ¿A qué hora debemos llegar al punto de encuentro?", false));
        mensajes.add(new ChatMessage("Tú: El punto de encuentro es a las 7:00 AM en la Plaza de Armas", true));
        mensajes.add(new ChatMessage(nombreCliente + ": Perfecto, ¿necesito llevar algo especial?", false));
    }

    private void setupRecyclerView() {
        chatMessageAdapter = new ChatMessageAdapter(mensajes);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatMessageAdapter);
        
        // Scroll al último mensaje
        if (mensajes.size() > 0) {
            recyclerChat.scrollToPosition(mensajes.size() - 1);
        }
    }

    private void setupListeners() {
        // Botón back
        btnBack.setOnClickListener(v -> finish());

        // Botón enviar
        btnSend.setOnClickListener(v -> {
            String msg = etMensaje.getText().toString().trim();
            if (!msg.isEmpty()) {
                mensajes.add(new ChatMessage("Tú: " + msg, true));
                chatMessageAdapter.notifyItemInserted(mensajes.size() - 1);
                recyclerChat.scrollToPosition(mensajes.size() - 1);
                etMensaje.setText("");
            }
        });

        // Botón voz
        btnVoice.setOnClickListener(v ->
                Toast.makeText(this, "Funcionalidad de voz pendiente", Toast.LENGTH_SHORT).show()
        );
    }

    // Clase interna para mensajes
    public static class ChatMessage {
        private String message;
        private boolean isFromAdmin;

        public ChatMessage(String message, boolean isFromAdmin) {
            this.message = message;
            this.isFromAdmin = isFromAdmin;
        }

        public String getMessage() { return message; }
        public boolean isFromAdmin() { return isFromAdmin; }
    }

    // Adapter simple para mensajes
    private static class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {
        private List<ChatMessage> messages;

        public ChatMessageAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @Override
        public MessageViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            holder.textView.setText(message.getMessage());
            
            // Cambiar estilo según quién envió el mensaje
            if (message.isFromAdmin()) {
                holder.textView.setTextColor(0xFF009688); // Verde para admin
                holder.textView.setBackgroundColor(0xFFE0F2F1); // Fondo verde claro
            } else {
                holder.textView.setTextColor(0xFF333333); // Gris oscuro para cliente
                holder.textView.setBackgroundColor(0xFFF5F5F5); // Fondo gris claro
            }
            holder.textView.setPadding(16, 12, 16, 12);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        static class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            MessageViewHolder(android.view.View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}