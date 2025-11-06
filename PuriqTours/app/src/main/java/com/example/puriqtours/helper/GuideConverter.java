package com.example.puriqtours.helper;

import com.example.puriqtours.entity.GuideAdmin;
import com.example.puriqtours.entity.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper para convertir entre Usuario (Guía en Firestore) y GuideAdmin (UI/Adapter)
 */
public class GuideConverter {
    
    /**
     * Convertir Usuario (Guía de Firestore) a GuideAdmin (para adapters/UI)
     */
    public static GuideAdmin usuarioToGuideAdmin(Usuario usuario) {
        if (usuario == null) return null;
        
        GuideAdmin guideAdmin = new GuideAdmin();
        
        // Intentar parsear el UID a int, o usar hashCode como fallback
        try {
            guideAdmin.setId(Integer.parseInt(usuario.getUid()));
        } catch (NumberFormatException e) {
            guideAdmin.setId(usuario.getUid() != null ? usuario.getUid().hashCode() : 0);
        }
        
        // Nombre completo o username
        String nombreCompleto = usuario.getName() != null ? usuario.getName() : "";
        if (usuario.getLast_name() != null && !usuario.getLast_name().isEmpty()) {
            nombreCompleto += " " + usuario.getLast_name();
        }
        if (nombreCompleto.trim().isEmpty()) {
            nombreCompleto = usuario.getUsername() != null ? usuario.getUsername() : "Guía sin nombre";
        }
        guideAdmin.setName(nombreCompleto);
        
        // Ubicación desde address
        guideAdmin.setLocation(usuario.getAddress() != null ? usuario.getAddress() : "Sin ubicación");
        
        // Rating (si existe campo de valoración)
        int rating = 0;
        try {
            if (usuario.getActivities() != null && !usuario.getActivities().isEmpty()) {
                // Por ahora usar 4 como rating por defecto
                rating = 4;
            }
        } catch (Exception e) {
            rating = 4;
        }
        guideAdmin.setRating(rating);
        
        // Disponibilidad basada en state
        boolean isAvailable = usuario.getStatus() != null && 
                             usuario.getStatus().equalsIgnoreCase("habilitado");
        guideAdmin.setAvailable(isAvailable);
        
        // Imagen por defecto
        guideAdmin.setImageResource(android.R.drawable.ic_menu_myplaces);
        
        return guideAdmin;
    }
    
    /**
     * Convertir lista de Usuarios (Guías) a lista de GuideAdmins
     */
    public static List<GuideAdmin> usuariosToGuideAdmins(List<Usuario> usuarios) {
        List<GuideAdmin> guideAdmins = new ArrayList<>();
        if (usuarios != null) {
            for (Usuario usuario : usuarios) {
                // Solo convertir usuarios con rol "Guia"
                if (usuario.getRol() != null && usuario.getRol().equalsIgnoreCase("Guia")) {
                    GuideAdmin guideAdmin = usuarioToGuideAdmin(usuario);
                    if (guideAdmin != null) {
                        guideAdmins.add(guideAdmin);
                    }
                }
            }
        }
        return guideAdmins;
    }
}
