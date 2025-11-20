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
        
        // Guardar UID original
        guideAdmin.setUid(usuario.getUid());
        
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
        
        // Rating desde el campo rating del usuario guía
        int rating = 4;  // Default
        try {
            if (usuario.getRating() != null) {
                rating = usuario.getRating().intValue();
            }
        } catch (Exception e) {
            rating = 4;
        }
        guideAdmin.setRating(rating);
        
        // Disponibilidad basada en guide_status: "Habilitado" = disponible, "No habilitado" = no disponible
        boolean isAvailable = usuario.getGuide_status() != null && 
                             usuario.getGuide_status().equalsIgnoreCase("Habilitado");
        guideAdmin.setAvailable(isAvailable);
        
        // Profile image URL desde Firebase Storage
        guideAdmin.setProfileImageUrl(usuario.getProfile_image());
        
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
