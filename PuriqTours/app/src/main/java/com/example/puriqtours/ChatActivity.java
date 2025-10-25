package com.example.puriqtours;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

<<<<<<< HEAD
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity {
=======
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
>>>>>>> Mathi

    private EditText etMensaje;
    private ImageButton btnSend, btnVoice, btnBack;
    private RecyclerView recyclerChat;
<<<<<<< HEAD
    private ChatAdapter chatAdapter;
    private List<String> mensajes;
=======
    private ChatMessageAdapter chatMessageAdapter;
    private List<ChatMessage> mensajes;
    private TextView tvClientName, tvTourName;
    private StorageHelper storageHelper;
    private String chatId;
>>>>>>> Mathi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

<<<<<<< HEAD
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
=======
        // Recibir datos del intent
        String clientName = getIntent().getStringExtra("client_name");
        String tourName = getIntent().getStringExtra("tour_name");
        chatId = getIntent().getStringExtra("chat_id");
        if (chatId == null) {
            chatId = String.valueOf(getIntent().getIntExtra("chat_id", 0));
        }

        // Inicializar storage
        storageHelper = new StorageHelper(this);

        // Inicializar vistas
        initViews();
        
        // Configurar información del chat
        setupChatInfo(clientName, tourName);
        
        // Cargar mensajes desde storage
        loadChatMessages();
        
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

    private void loadChatMessages() {
        mensajes = storageHelper.loadChatMessages(chatId);
    }

    private void createSampleMessages(String clientName) {
        mensajes = new ArrayList<>();
        
        // Mensajes de simulación
        String nombreCliente = clientName != null ? clientName : "Cliente";
        
        mensajes.add(new ChatMessage(nombreCliente + ": Hola, tengo una consulta sobre el tour", false, "10:20"));
        mensajes.add(new ChatMessage("Tú: ¡Hola! Estoy aquí para ayudarte. ¿Cuál es tu consulta?", true, "10:21"));
        mensajes.add(new ChatMessage(nombreCliente + ": ¿A qué hora debemos llegar al punto de encuentro?", false, "10:22"));
        mensajes.add(new ChatMessage("Tú: El punto de encuentro es a las 7:00 AM en la Plaza de Armas", true, "10:23"));
        mensajes.add(new ChatMessage(nombreCliente + ": Perfecto, ¿necesito llevar algo especial?", false, "10:24"));
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
                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                ChatMessage newMessage = new ChatMessage(msg, true, currentTime);
                
                mensajes.add(newMessage);
                storageHelper.addChatMessage(chatId, newMessage);
                
                chatMessageAdapter.notifyItemInserted(mensajes.size() - 1);
>>>>>>> Mathi
                recyclerChat.scrollToPosition(mensajes.size() - 1);
                etMensaje.setText("");
            }
        });

<<<<<<< HEAD
        // 🔹 Botón Voz
=======
        // Botón voz
>>>>>>> Mathi
        btnVoice.setOnClickListener(v ->
                Toast.makeText(this, "Funcionalidad de voz pendiente", Toast.LENGTH_SHORT).show()
        );
    }
<<<<<<< HEAD
}
=======

    // Clase interna para mensajes
    public static class ChatMessage {
        private String message;
        private boolean isFromAdmin;
        private String time;

        public ChatMessage(String message, boolean isFromAdmin, String time) {
            this.message = message;
            this.isFromAdmin = isFromAdmin;
            this.time = time;
        }

        public String getMessage() { return message; }
        public boolean isFromAdmin() { return isFromAdmin; }
        public String getTime() { return time; }
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
>>>>>>> Mathi
