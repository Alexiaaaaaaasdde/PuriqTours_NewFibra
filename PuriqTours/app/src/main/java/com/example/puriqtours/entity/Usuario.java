package com.example.puriqtours.entity;

import com.google.firebase.firestore.DocumentSnapshot;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Usuario implements Serializable {

    private String uid;
    private String username;
    private String name;
    private String last_name;
    private String email;
    private String birthdate;
    private String doc_type;
    private String document;
    private String phone;
    private String address;
    private String language;
    private String rol;
    private String profile_image;
    private List<String> activities;

    // 🔹 Constructor vacío (necesario para Firestore)
    public Usuario() {}

    // 🔹 Constructor completo
    public Usuario(String uid, String username, String name, String last_name, String email,
                   String birthdate, String doc_type, String document, String phone,
                   String address, String language, String rol, String profile_image, List<String> activities) {
        this.uid = uid;
        this.username = username;
        this.name = name;
        this.last_name = last_name;
        this.email = email;
        this.birthdate = birthdate;
        this.doc_type = doc_type;
        this.document = document;
        this.phone = phone;
        this.address = address;
        this.language = language;
        this.rol = rol;
        this.profile_image = profile_image;
        this.activities = activities;
    }

    // 🔹 Getters y setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLast_name() { return last_name; }
    public void setLast_name(String last_name) { this.last_name = last_name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }

    public String getDoc_type() { return doc_type; }
    public void setDoc_type(String doc_type) { this.doc_type = doc_type; }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getProfile_image() { return profile_image; }
    public void setProfile_image(String profile_image) { this.profile_image = profile_image; }

    public List<String> getActivities() { return activities; }
    public void setActivities(List<String> activities) { this.activities = activities; }

    // 🔹 Mapeo a Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("name", name);
        map.put("last_name", last_name);
        map.put("email", email);
        map.put("birthdate", birthdate);
        map.put("doc_type", doc_type);
        map.put("document", document);
        map.put("phone", phone);
        map.put("address", address);
        map.put("language", language);
        map.put("rol", rol);
        map.put("profile_image", profile_image);
        map.put("activities", activities);
        return map;
    }

    // 🔹 Crear instancia desde DocumentSnapshot
    public static Usuario fromSnapshot(DocumentSnapshot doc) {
        Usuario u = doc.toObject(Usuario.class);
        if (u != null) {
            u.setUid(doc.getId());
        }
        return u;
    }
}
