package com.example.puriqtours.entity;

import com.google.firebase.Timestamp;

public class ChatThread {

    private String chatId;
    private String idCliente;
    private String idEmpresa;
    private String clientName;
    private String tourName;
    private String lastMessage;
    private Timestamp lastTimestamp;

    // 🔹 Constructor vacío (Firestore)
    public ChatThread() {}

    public ChatThread(String chatId, String idCliente, String idEmpresa,
                      String clientName, String tourName,
                      String lastMessage, Timestamp lastTimestamp) {
        this.chatId = chatId;
        this.idCliente = idCliente;
        this.idEmpresa = idEmpresa;
        this.clientName = clientName;
        this.tourName = tourName;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
    }

    public String getChatId() { return chatId; }
    public String getIdCliente() { return idCliente; }
    public String getIdEmpresa() { return idEmpresa; }
    public String getClientName() { return clientName; }
    public String getTourName() { return tourName; }
    public String getLastMessage() { return lastMessage; }
    public Timestamp getLastTimestamp() { return lastTimestamp; }
}
