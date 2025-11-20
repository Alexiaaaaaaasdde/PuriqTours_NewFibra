package com.example.puriqtours.helper;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class StorageManager {
    
    private static final String TAG = "StorageManager";
    private final FirebaseStorage storage;
    private final StorageReference storageRef;
    
    public StorageManager() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }
    
    /**
     * Sube una imagen a Firebase Storage en la carpeta de servicios extra
     * @param imageUri URI de la imagen seleccionada
     * @param listener Listener para recibir la URL de descarga o error
     */
    public void uploadExtraServiceImage(Uri imageUri, OnImageUploadListener listener) {
        if (imageUri == null) {
            listener.onError("No se seleccionó ninguna imagen");
            return;
        }
        
        // Generar nombre único para la imagen
        String fileName = "extra_service_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child("extra_services/" + fileName);
        
        // Subir imagen
        UploadTask uploadTask = imageRef.putFile(imageUri);
        
        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            listener.onProgress((int) progress);
            Log.d(TAG, "Upload is " + progress + "% done");
        }).addOnSuccessListener(taskSnapshot -> {
            // Obtener URL de descarga
            imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                String downloadUrl = downloadUri.toString();
                Log.d(TAG, "Image uploaded successfully: " + downloadUrl);
                listener.onSuccess(downloadUrl);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error getting download URL", e);
                listener.onError("Error al obtener URL de la imagen: " + e.getMessage());
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error uploading image", e);
            listener.onError("Error al subir la imagen: " + e.getMessage());
        });
    }
    
    /**
     * Sube una imagen a Firebase Storage en la carpeta de tours
     * @param imageUri URI de la imagen seleccionada
     * @param listener Listener para recibir la URL de descarga o error
     */
    public void uploadTourImage(Uri imageUri, OnImageUploadListener listener) {
        if (imageUri == null) {
            listener.onError("No se seleccionó ninguna imagen");
            return;
        }
        
        String fileName = "tour_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child("tours/" + fileName);
        
        UploadTask uploadTask = imageRef.putFile(imageUri);
        
        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            listener.onProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                listener.onSuccess(downloadUri.toString());
            }).addOnFailureListener(e -> {
                listener.onError("Error al obtener URL: " + e.getMessage());
            });
        }).addOnFailureListener(e -> {
            listener.onError("Error al subir imagen: " + e.getMessage());
        });
    }
    
    /**
     * Interface para callbacks de subida de imagen
     */
    public interface OnImageUploadListener {
        void onSuccess(String downloadUrl);
        void onError(String errorMessage);
        void onProgress(int progress);
    }
}
