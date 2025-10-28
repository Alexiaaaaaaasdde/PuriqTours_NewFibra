package com.example.puriqtours;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.puriqtours.model.Tour;
import com.example.puriqtours.model.Guide;
import com.example.puriqtours.model.Chat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StorageHelper {
    
    private static final String PREF_NAME = "AdminPuriqToursPrefs";
    private static final String TOURS_KEY = "saved_tours";
    private static final String GUIDES_KEY = "saved_guides";
    private static final String CHATS_KEY = "saved_chats";
    private static final String CHAT_MESSAGES_KEY = "chat_messages_";
    private static final String PROFILE_KEY = "admin_profile";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    
    public StorageHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }
    
    // ==================== TOURS ====================
    
    public void saveTours(List<Tour> tours) {
        String toursJson = gson.toJson(tours);
        editor.putString(TOURS_KEY, toursJson);
        editor.apply();
    }
    
    public List<Tour> loadTours() {
        String toursJson = sharedPreferences.getString(TOURS_KEY, null);
        if (toursJson != null) {
            Type listType = new TypeToken<List<Tour>>(){}.getType();
            return gson.fromJson(toursJson, listType);
        }
        return createDefaultTours(); // Si no hay tours guardados, crear algunos por defecto
    }
    
    public void addTour(Tour tour) {
        List<Tour> tours = loadTours();
        tours.add(tour);
        saveTours(tours);
    }
    
    public void updateTour(Tour updatedTour) {
        List<Tour> tours = loadTours();
        for (int i = 0; i < tours.size(); i++) {
            if (tours.get(i).getId() == updatedTour.getId()) {
                tours.set(i, updatedTour);
                break;
            }
        }
        saveTours(tours);
    }
    
    public void deleteTour(int tourId) {
        List<Tour> tours = loadTours();
        tours.removeIf(tour -> tour.getId() == tourId);
        saveTours(tours);
    }
    
    private List<Tour> createDefaultTours() {
        List<Tour> defaultTours = new ArrayList<>();
        // Usar un ID de drawable genérico o 0 si no hay imagen específica
        int defaultImage = android.R.drawable.ic_menu_gallery; // Drawable del sistema
        defaultTours.add(new Tour(1, "Tour Machu Picchu", "Cusco", "Explora la ciudadela inca más famosa del mundo", "Hoy • 3 h", defaultImage, 299.0, 1, "Juan Pérez"));
        defaultTours.add(new Tour(2, "Tour Valle Sagrado", "Cusco", "Descubre los pueblos y sitios arqueológicos del Valle Sagrado", "Mañana • 3 h", defaultImage, 199.0, 1, ""));
        defaultTours.add(new Tour(3, "City Tour Lima", "Lima", "Recorre el centro histórico de Lima", "23/08/2026 • 3 h", defaultImage, 89.0, 1, ""));
        return defaultTours;
    }
    
    // ==================== GUIDES ====================
    
    public void saveGuides(List<Guide> guides) {
        String guidesJson = gson.toJson(guides);
        editor.putString(GUIDES_KEY, guidesJson);
        editor.apply();
    }
    
    public List<Guide> loadGuides() {
        String guidesJson = sharedPreferences.getString(GUIDES_KEY, null);
        if (guidesJson != null) {
            Type listType = new TypeToken<List<Guide>>(){}.getType();
            return gson.fromJson(guidesJson, listType);
        }
        return createDefaultGuides(); // Si no hay guías guardados, crear algunos por defecto
    }
    
    public void addGuide(Guide guide) {
        List<Guide> guides = loadGuides();
        guides.add(guide);
        saveGuides(guides);
    }
    
    public void updateGuide(Guide updatedGuide) {
        List<Guide> guides = loadGuides();
        for (int i = 0; i < guides.size(); i++) {
            if (guides.get(i).getId() == updatedGuide.getId()) {
                guides.set(i, updatedGuide);
                break;
            }
        }
        saveGuides(guides);
    }
    
    public void deleteGuide(int guideId) {
        List<Guide> guides = loadGuides();
        guides.removeIf(guide -> guide.getId() == guideId);
        saveGuides(guides);
    }
    
    private List<Guide> createDefaultGuides() {
        List<Guide> defaultGuides = new ArrayList<>();
        // Usar un ID de drawable genérico o 0 si no hay imagen específica  
        int defaultAvatar = android.R.drawable.ic_menu_myplaces; // Drawable del sistema
        defaultGuides.add(new Guide(1, "Carlos Mendoza", "Cusco", 5, true, defaultAvatar));
        defaultGuides.add(new Guide(2, "María Quispe", "Cusco", 4, false, defaultAvatar));
        defaultGuides.add(new Guide(3, "José Huamán", "Cusco", 5, true, defaultAvatar));
        return defaultGuides;
    }
    
    // ==================== CHATS ====================
    
    public void saveChats(List<Chat> chats) {
        String chatsJson = gson.toJson(chats);
        editor.putString(CHATS_KEY, chatsJson);
        editor.apply();
    }
    
    public List<Chat> loadChats() {
        String chatsJson = sharedPreferences.getString(CHATS_KEY, null);
        if (chatsJson != null) {
            Type listType = new TypeToken<List<Chat>>(){}.getType();
            return gson.fromJson(chatsJson, listType);
        }
        return createDefaultChats(); // Si no hay chats guardados, crear algunos por defecto
    }
    
    public void addChat(Chat chat) {
        List<Chat> chats = loadChats();
        chats.add(chat);
        saveChats(chats);
    }
    
    public void updateChat(Chat updatedChat) {
        List<Chat> chats = loadChats();
        for (int i = 0; i < chats.size(); i++) {
            if (chats.get(i).getId() == updatedChat.getId()) {
                chats.set(i, updatedChat);
                break;
            }
        }
        saveChats(chats);
    }
    
    public void deleteChat(int chatId) {
        List<Chat> chats = loadChats();
        chats.removeIf(chat -> chat.getId() == chatId);
        saveChats(chats);
        
        // También eliminar los mensajes de este chat
        deleteChatMessages(String.valueOf(chatId));
    }
    
    private List<Chat> createDefaultChats() {
        List<Chat> defaultChats = new ArrayList<>();
        defaultChats.add(new Chat(1, "María García", 
            "¿Tienen disponibilidad para Machu Picchu?", "10:30 AM", true, 2, "Tour Machu Picchu"));
        defaultChats.add(new Chat(2, "Carlos López", 
            "Gracias por la información", "9:15 AM", false, 0, "City Tour Lima"));
        defaultChats.add(new Chat(3, "Ana Martínez", 
            "¿El tour incluye almuerzo?", "Yesterday", true, 1, "Valle Sagrado"));
        return defaultChats;
    }
    
    // ==================== CHAT MESSAGES ====================
    
    public void saveChatMessages(String chatId, List<ChatAdminActivity.ChatMessage> messages) {
        String messagesJson = gson.toJson(messages);
        editor.putString(CHAT_MESSAGES_KEY + chatId, messagesJson);
        editor.apply();
    }
    
    public List<ChatAdminActivity.ChatMessage> loadChatMessages(String chatId) {
        String messagesJson = sharedPreferences.getString(CHAT_MESSAGES_KEY + chatId, null);
        if (messagesJson != null) {
            Type listType = new TypeToken<List<ChatAdminActivity.ChatMessage>>(){}.getType();
            return gson.fromJson(messagesJson, listType);
        }
        return createDefaultMessages(chatId); // Crear mensajes por defecto para este chat
    }
    
    public void addChatMessage(String chatId, ChatAdminActivity.ChatMessage message) {
        List<ChatAdminActivity.ChatMessage> messages = loadChatMessages(chatId);
        messages.add(message);
        saveChatMessages(chatId, messages);
    }
    
    public void deleteChatMessages(String chatId) {
        editor.remove(CHAT_MESSAGES_KEY + chatId);
        editor.apply();
    }
    
    private List<ChatAdminActivity.ChatMessage> createDefaultMessages(String chatId) {
        List<ChatAdminActivity.ChatMessage> defaultMessages = new ArrayList<>();
        
        // Crear algunos mensajes por defecto basados en el ID del chat
        switch (chatId) {
            case "1": // María García
                defaultMessages.add(new ChatAdminActivity.ChatMessage("Hola, buenos días", false, "10:25 AM"));
                defaultMessages.add(new ChatAdminActivity.ChatMessage("¿Tienen disponibilidad para el tour a Machu Picchu este fin de semana?", false, "10:26 AM"));
                defaultMessages.add(new ChatAdminActivity.ChatMessage("Buenos días María, sí tenemos disponibilidad", true, "10:28 AM"));
                defaultMessages.add(new ChatAdminActivity.ChatMessage("¿Para cuántas personas sería?", true, "10:28 AM"));
                defaultMessages.add(new ChatAdminActivity.ChatMessage("Serían 4 personas adultas", false, "10:30 AM"));
                break;
            case "2": // Carlos López
                defaultMessages.add(new ChatAdminActivity.ChatMessage("Hola, me interesa el City Tour por Lima", false, "9:10 AM"));
                defaultMessages.add(new ChatAdminActivity.ChatMessage("Perfecto Carlos, te envío la información", true, "9:12 AM"));
                defaultMessages.add(new ChatAdminActivity.ChatMessage("El tour incluye transporte y guía", true, "9:13 AM"));
                defaultMessages.add(new ChatAdminActivity.ChatMessage("Gracias por la información", false, "9:15 AM"));
                break;
            case "3": // Ana Martínez
                defaultMessages.add(new ChatAdminActivity.ChatMessage("Consulta sobre el Valle Sagrado", false, "Yesterday"));
                defaultMessages.add(new ChatAdminActivity.ChatMessage("¿El tour incluye almuerzo?", false, "Yesterday"));
                break;
            default:
                defaultMessages.add(new ChatAdminActivity.ChatMessage("¡Hola! ¿En qué puedo ayudarte?", true, "Ahora"));
                break;
        }
        
        return defaultMessages;
    }
    
    // ==================== ADMIN PROFILE ====================
    
    public void saveAdminProfile(String companyName, String phone, String email, String address) {
        editor.putString(PROFILE_KEY + "_company", companyName);
        editor.putString(PROFILE_KEY + "_phone", phone);
        editor.putString(PROFILE_KEY + "_email", email);
        editor.putString(PROFILE_KEY + "_address", address);
        editor.apply();
    }
    
    public String getAdminCompanyName() {
        return sharedPreferences.getString(PROFILE_KEY + "_company", "Puriq Tours");
    }
    
    public String getAdminPhone() {
        return sharedPreferences.getString(PROFILE_KEY + "_phone", "+51 999 888 777");
    }
    
    public String getAdminEmail() {
        return sharedPreferences.getString(PROFILE_KEY + "_email", "admin@puriqtours.com");
    }
    
    public String getAdminAddress() {
        return sharedPreferences.getString(PROFILE_KEY + "_address", "Av. El Sol 123, Cusco");
    }
    
    // ==================== UTILITY METHODS ====================
    
    public void clearAllData() {
        editor.clear();
        editor.apply();
    }
    
    public boolean hasStoredData() {
        return sharedPreferences.contains(TOURS_KEY) || 
               sharedPreferences.contains(GUIDES_KEY) || 
               sharedPreferences.contains(CHATS_KEY);
    }
}