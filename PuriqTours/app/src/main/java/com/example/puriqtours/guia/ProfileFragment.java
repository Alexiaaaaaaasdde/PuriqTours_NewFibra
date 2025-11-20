package com.example.puriqtours.guia;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.puriqtours.R;
import com.example.puriqtours.entity.Usuario;
import com.example.puriqtours.helper.UserSessionManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileFragment extends Fragment {

    private EditText etNombre, etApellido, etFechaNacimiento, etNumeroDocumento,
            etNumeroTelefonico, etTipoDocumento, etDireccion, etCorreo;
    private ShapeableImageView profileImage;
    private Button btnUpdate, btnSave;
    private UserSessionManager sessionManager;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseAuth auth;
    private String photoUrl;
    private FirebaseFirestore db;
    private String uid;
    private StorageReference storageRef;
    private Uri imageUri;
    private byte[] compressedImageBytes;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflamos el layout del fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // ⭐ VALIDACIÓN CRÍTICA - DEBE SER LO PRIMERO ⭐
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        uid = currentUser.getUid();
        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Referencias a las vistas
        profileImage = view.findViewById(R.id.profileImage);
        etNombre = view.findViewById(R.id.etNombre);
        etApellido = view.findViewById(R.id.etApellido);
        etFechaNacimiento = view.findViewById(R.id.etFechaNacimiento);
        etNumeroDocumento = view.findViewById(R.id.etNumeroDocumento);
        etNumeroTelefonico = view.findViewById(R.id.etNumeroTelefonico);
        etTipoDocumento = view.findViewById(R.id.etTipoDocumento);
        etDireccion = view.findViewById(R.id.etDireccion);
        etCorreo = view.findViewById(R.id.etCorreo);

        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnSave = view.findViewById(R.id.btnSave);


        Context context = requireContext();
        sessionManager = new UserSessionManager(context);

        cargarDatosUsuario();

        // Lógica de botones
        btnUpdate.setOnClickListener(v -> {
            habilitarCampos();
            btnUpdate.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);
        });

        btnSave.setOnClickListener(v -> {
            guardarCambios();
            btnUpdate.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
        });

        return view;
    }

    private void habilitarCampos() {
        etNumeroTelefonico.setEnabled(true);
        profileImage.setClickable(true);

        Toast.makeText(requireContext(), "Puedes cambiar tu número o foto", Toast.LENGTH_SHORT).show();

        profileImage.setOnClickListener(img -> {
            Intent pickPhoto = new Intent(Intent.ACTION_GET_CONTENT);
            pickPhoto.setType("image/*");
            imagePickerLauncher.launch(pickPhoto);  // ← ahora usamos el launcher
        });
    }

    private void guardarCambios() {
        // Campos base
        Map<String, Object> updates = new HashMap<>();
        updates.put("phone", etNumeroTelefonico.getText().toString());
        // Si hay imagen, primero subir → luego guardar datos
        if (compressedImageBytes != null) {
            uploadCompressedImage(url -> {
                photoUrl = url;  // 🔹 Ahora sí la URL ya existe

                updates.put("profile_image", photoUrl);

                // Guardar cambios en Firestore
                db.collection("users").document(uid).update(updates)
                        .addOnSuccessListener(v -> {
                            Log.d("Perfil", "Datos actualizados exitosamente");
                            // Guardar en sesión local
                            Usuario user = sessionManager.getUser();
                            user.setPhone(etNumeroTelefonico.getText().toString());
                            user.setProfile_image(photoUrl);
                            sessionManager.saveUser(user);
                            // Notificar al Activity principal que la foto cambió
                            requireActivity().runOnUiThread(() -> {
                                if (getActivity() instanceof PerfilActualizadoListener) {
                                    ((PerfilActualizadoListener) getActivity()).onPerfilActualizado();
                                }
                            });

                            mostrarDialogo();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Perfil", "Error al guardar: " + e.getMessage());
                            Toast.makeText(requireContext(), "Error al guardar cambios", Toast.LENGTH_SHORT).show();
                        });
            });
        } else {
            // Sin imagen → solo actualizar texto
            db.collection("users").document(uid).update(updates)
                    .addOnSuccessListener(v -> {
                        Usuario user = sessionManager.getUser();
                        user.setPhone(etNumeroTelefonico.getText().toString());
                        sessionManager.saveUser(user);
                        mostrarDialogo();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error al guardar cambios", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void uploadCompressedImage(PhotoUploadCallback callback) {
        ProgressDialog dialog = new ProgressDialog(requireContext());
        dialog.setMessage("Subiendo imagen...");
        dialog.setCancelable(false);
        dialog.show();

        String imageName = uid + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storageRef.child("profile_images/" + imageName);

        fileRef.putBytes(compressedImageBytes)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            dialog.dismiss();
                            callback.onUploaded(uri.toString());
                        }))
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void mostrarDialogo() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.findViewById(R.id.btnClose).setOnClickListener(closeView -> dialog.dismiss());
        dialog.show();

        etNumeroTelefonico.setEnabled(false);
        profileImage.setClickable(false);
    }

    // --- Recibir imagen seleccionada ---
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        Picasso.get().load(imageUri).into(profileImage);
                        compressImage(imageUri);
                    }
                }
        );
    }

    private void compressImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);

            // Escalar si la imagen es muy grande
            int maxWidth = 800;
            int maxHeight = 800;
            float ratio = Math.min((float) maxWidth / bitmap.getWidth(), (float) maxHeight / bitmap.getHeight());
            int newWidth = Math.round(bitmap.getWidth() * ratio);
            int newHeight = Math.round(bitmap.getHeight() * ratio);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 70% calidad
            compressedImageBytes = baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }
    interface PhotoUploadCallback {
        void onUploaded(String url);
    }



    private void cargarDatosUsuario() {
        Usuario user;
        user = sessionManager.getUser();
        etNombre.setText(user.getName());
        etApellido.setText(user.getLast_name());
        etFechaNacimiento.setText(user.getBirthdate());
        etTipoDocumento.setText(user.getDoc_type());
        etNumeroDocumento.setText(user.getDocument());
        etNumeroTelefonico.setText(user.getPhone());
        etDireccion.setText(user.getAddress());
        etCorreo.setText(user.getEmail());
        String imageUrl = user.getProfile_image();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.profile_image_dummy) // imagen temporal
                    .error(R.drawable.profile_image_dummy) // si falla la carga
                    .into(profileImage);
        }

        etNumeroTelefonico.setEnabled(false);
        profileImage.setClickable(false);

        btnSave.setVisibility(View.GONE);
    }


}
