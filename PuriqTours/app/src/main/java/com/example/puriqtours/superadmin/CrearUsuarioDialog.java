package com.example.puriqtours.superadmin;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.content.FileProvider;

import com.example.puriqtours.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CrearUsuarioDialog extends AppCompatDialogFragment {

    private LinearLayout contenedorCampos;
    private TextView tvRolFijo;
    private ImageView imgFotoUsuario;
    private TextView btnAgregarFoto;

    private ArrayList<TextInputLayout> listaCamposTexto = new ArrayList<>();
    private ArrayList<Spinner> listaSpinners = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Uri imagenSeleccionadaUri = null;

    // =====================================================
    // CREAR DIÁLOGO
    // =====================================================
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_crear_usuario, null);

        // Inicializar vistas
        tvRolFijo = view.findViewById(R.id.tvRolFijo);
        contenedorCampos = view.findViewById(R.id.contenedorCampos);
        imgFotoUsuario = view.findViewById(R.id.imgFotoUsuario);
        btnAgregarFoto = view.findViewById(R.id.btnAgregarFoto);
        MaterialButton btnCrear = view.findViewById(R.id.btnCrear);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        // Rol fijo
        tvRolFijo.setText("Rol: Administrador");

        // Dibujar campos
        dibujarCampos();

        // 🔹 Selector de cámara o galería
        btnAgregarFoto.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Seleccionar foto")
                    .setItems(new CharSequence[]{"Tomar foto", "Elegir de galería"}, (dialog, which) -> {
                        if (which == 0) {
                            abrirCamara();
                        } else {
                            seleccionarImagenLauncher.launch("image/*");
                        }
                    })
                    .show();
        });

        // Botón crear
        btnCrear.setOnClickListener(v -> crearUsuario());

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        return dialog;
    }

    // =====================================================
    // DIBUJAR CAMPOS
    // =====================================================
    private void dibujarCampos() {
        contenedorCampos.removeAllViews();
        listaCamposTexto.clear();
        listaSpinners.clear();

        agregarCampoTexto("Nombre");
        agregarCampoTexto("Apellido");
        agregarCampoTexto("Email");

        // 🔥 NUEVOS CAMPOS
        agregarCampoTexto("Teléfono", InputType.TYPE_CLASS_PHONE);
        agregarCampoTexto("Dirección", InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);

        agregarCampoTexto("Password", InputType.TYPE_TEXT_VARIATION_PASSWORD);

        agregarCampoSpinner("Idioma", new String[]{"Español", "Inglés", "Aymara"});
    }


    // =====================================================
    // CAMPOS
    // =====================================================
    private void agregarCampoTexto(String hint) {
        agregarCampoTexto(hint, InputType.TYPE_CLASS_TEXT);
    }

    private void agregarCampoTexto(String hint, int tipo) {
        TextInputLayout til = new TextInputLayout(requireContext());
        til.setHint(hint);

        TextInputEditText et = new TextInputEditText(requireContext());
        et.setInputType(tipo);
        et.setPadding(0, 30, 0, 30);

        til.addView(et);
        contenedorCampos.addView(til);

        listaCamposTexto.add(til);
    }

    private void agregarCampoSpinner(String hint, String[] valores) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 20, 0, 20);

        TextView label = new TextView(requireContext());
        label.setText(hint);
        label.setTextSize(16);
        label.setPadding(0, 10, 0, 10);

        Spinner spinner = new Spinner(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                valores
        );
        spinner.setAdapter(adapter);

        layout.addView(label);
        layout.addView(spinner);
        contenedorCampos.addView(layout);

        listaSpinners.add(spinner);
    }

    // =====================================================
    // CREAR USUARIO
    // =====================================================
    private void crearUsuario() {
        String rol = "Admin";

        // 1️⃣ LEER CAMPOS
        int i = 0;
        String nombre    = getTexto(i++);
        String apellido  = getTexto(i++);
        String email     = getTexto(i++);
        String telefono  = getTexto(i++);
        String direccion = getTexto(i++);
        String password  = getTexto(i++);

        // 2️⃣ VALIDAR CAMPOS DE TEXTO
        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty()
                || telefono.isEmpty() || direccion.isEmpty() || password.isEmpty()) {

            Toast.makeText(getContext(),
                    "❗Completa todos los campos",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // 3️⃣ 🔥 VALIDAR FOTO (AQUÍ VA)
        if (imagenSeleccionadaUri == null) {
            Toast.makeText(getContext(),
                    "📸 Debes adjuntar una foto de perfil",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // 4️⃣ DATA PARA FIRESTORE
        Map<String, Object> data = new HashMap<>();
        data.put("name", nombre);
        data.put("last_name", apellido);
        data.put("email", email);
        data.put("phone", telefono);
        data.put("address", direccion);
        data.put("rol", rol);
        data.put("status", "Activo");

        String idioma = listaSpinners.get(0).getSelectedItem().toString();
        data.put("language", idioma);

        Toast.makeText(getContext(), "Creando usuario...", Toast.LENGTH_SHORT).show();

        // 5️⃣ CREAR USUARIO AUTH
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    // 📸 Ruta de la foto
                    StorageReference fotoRef =
                            storageRef.child(uid + ".jpg");

                    // ⬆️ Subir imagen
                    fotoRef.putFile(imagenSeleccionadaUri)
                            .addOnSuccessListener(taskSnapshot ->
                                    fotoRef.getDownloadUrl()
                                            .addOnSuccessListener(uri -> {

                                                // ✅ Guardar URL
                                                data.put("uid", uid);
                                                data.put("profile_image", uri.toString());

                                                // 💾 Guardar usuario en Firestore
                                                db.collection("users").document(uid)
                                                        .set(data)
                                                        .addOnSuccessListener(a -> {
                                                            Toast.makeText(getContext(),
                                                                    "✅ Usuario creado con foto",
                                                                    Toast.LENGTH_LONG).show();
                                                            dismiss();
                                                        })
                                                        .addOnFailureListener(e ->
                                                                Toast.makeText(getContext(),
                                                                        "❌ Firestore: " + e.getMessage(),
                                                                        Toast.LENGTH_LONG).show());
                                            }))
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(),
                                            "❌ Error al subir foto: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "❌ Error Auth: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());

    }



    // =====================================================
    // HELPERS
    // =====================================================
    private String getTexto(int index) {
        return listaCamposTexto.get(index).getEditText().getText().toString();
    }

    // =====================================================
    // GALERÍA Y CÁMARA
    // =====================================================
    private final ActivityResultLauncher<String> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagenSeleccionadaUri = uri;
                    imgFotoUsuario.setImageURI(uri);
                }
            });

    private final ActivityResultLauncher<Uri> tomarFotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && imagenSeleccionadaUri != null) {
                    imgFotoUsuario.setImageURI(imagenSeleccionadaUri);
                }
            });

    private void abrirCamara() {
        try {
            File tempFile = File.createTempFile("temp_foto_", ".jpg", requireContext().getCacheDir());
            imagenSeleccionadaUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    tempFile
            );
            tomarFotoLauncher.launch(imagenSeleccionadaUri);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al abrir cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
