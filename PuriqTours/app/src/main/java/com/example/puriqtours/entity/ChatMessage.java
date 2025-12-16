package com.example.puriqtours.entity;

import com.google.firebase.Timestamp;

public class ChatMessage {

    private String senderId;
    private String senderRole; // "Cliente" o "Admin"
    private String text;
    private Timestamp timestamp;

    public ChatMessage() {}

    public ChatMessage(String senderId, String senderRole, String text) {
        this.senderId = senderId;
        this.senderRole = senderRole;
        this.text = text;
        this.timestamp = Timestamp.now();
    }

    public String getSenderId() { return senderId; }
    public String getSenderRole() { return senderRole; }
    public String getText() { return text; }
    public Timestamp getTimestamp() { return timestamp; }
}
