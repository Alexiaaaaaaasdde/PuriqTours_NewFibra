package com.example.puriqtours.model;

public class Chat {
    private int id;
    private String clientName;
    private String lastMessage;
    private String time;
    private boolean hasUnreadMessages;
    private int unreadCount;
    private String tourName;

    public Chat() {}

    public Chat(int id, String clientName, String lastMessage, String time, boolean hasUnreadMessages, int unreadCount, String tourName) {
        this.id = id;
        this.clientName = clientName;
        this.lastMessage = lastMessage;
        this.time = time;
        this.hasUnreadMessages = hasUnreadMessages;
        this.unreadCount = unreadCount;
        this.tourName = tourName;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public boolean hasUnreadMessages() { return hasUnreadMessages; }
    public void setHasUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public String getTourName() { return tourName; }
    public void setTourName(String tourName) { this.tourName = tourName; }

    // Método para obtener texto de mensajes no leídos
    public String getUnreadText() {
        if (hasUnreadMessages && unreadCount > 0) {
            return unreadCount + " nuevo" + (unreadCount > 1 ? "s" : "");
        }
        return "";
    }
}