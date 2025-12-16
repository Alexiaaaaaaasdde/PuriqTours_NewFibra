package com.example.puriqtours.superadmin;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogItem {

    private String type;        // 👈 igual que BD
    private String desc;        // 👈 igual que BD
    private Timestamp timestamp; // 👈 igual que BD

    // 🔹 Constructor vacío (OBLIGATORIO para Firestore)
    public LogItem() {}

    public LogItem(String type, String desc, Timestamp timestamp) {
        this.type = type;
        this.desc = desc;
        this.timestamp = timestamp;
    }

    // ===== GETTERS =====
    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    // ===== UTILIDAD PARA UI =====
    public String getFechaFormateada() {
        if (timestamp == null) return "";
        Date date = timestamp.toDate();
        SimpleDateFormat sdf =
                new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}
