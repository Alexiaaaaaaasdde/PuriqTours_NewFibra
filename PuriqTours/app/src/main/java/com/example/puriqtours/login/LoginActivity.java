package com.example.puriqtours.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.puriqtours.R;
import com.example.puriqtours.SplashActivity;
import com.example.puriqtours.cliente.ProfileActivity;
import com.example.puriqtours.helper.UserSessionManager;
import com.example.puriqtours.entity.Usuario;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private MaterialButton btnGoogle;
    private TextView tvRegister, tvForgotPassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final int RC_GOOGLE = 1001;
    private GoogleSignInClient googleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // ⭐ Verificar si ya hay sesión activa
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "Usuario ya autenticado, redirigiendo...");
            irAProfile();
            return;
        }

        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogle = findViewById(R.id.btn_google);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        ImageView btnTogglePassword = findViewById(R.id.btnTogglePassword);

        btnTogglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() ==
                    (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

                // Mostrar contraseña
                etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_eye_open);

            } else {

                // Ocultar contraseña
                etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            }

            // Mover el cursor al final para que no salte al inicio
            etPassword.setSelection(etPassword.getText().length());
        });


        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class))
        );


        btnGoogle.setOnClickListener(v -> iniciarGoogle());
    }

    // -------------------- LOGIN EMAIL -----------------------
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Campo requerido");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Campo requerido");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Verificando...");

        Log.d(TAG, "Intentando login con email: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Iniciar Sesión");

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Login exitoso");

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Usuario autenticado con UID: " + user.getUid());
                            verifyUserData(user.getUid());
                        } else {
                            Log.e(TAG, "Usuario es null después del login exitoso");
                            Toast.makeText(this, "Error inesperado", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Login fallido: " + task.getException());
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void verifyUserData(String uid) {
        Log.d(TAG, "Verificando datos del usuario en Firestore...");

        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        Log.w(TAG, "Usuario no tiene documento en Firestore");
                        mAuth.signOut();
                        Toast.makeText(this, "Tu cuenta no está registrada correctamente.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Log.d(TAG, "Datos de usuario encontrados");
                    Usuario user = Usuario.fromSnapshot(snapshot);

                    // Guardar en UserSession
                    new UserSessionManager(this).saveUser(user);

                    Log.d(TAG, "Sesión guardada, redirigiendo a ProfileActivity");

                    // ⭐ IR DIRECTO A PROFILE (sin pasar por Splash)
                    irAProfile();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos de Firestore: " + e.getMessage());
                    Toast.makeText(this, "Error cargando datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // -------------------- RESET PASSWORD -----------------------
    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Ingresa tu correo", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Correo de recuperación enviado");
                    Toast.makeText(this, "Correo enviado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al enviar correo: " + e.getMessage());
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // -------------------- LOGIN CON GOOGLE -----------------------
    private void iniciarGoogle() {
        Log.d(TAG, "Iniciando login con Google");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);

        startActivityForResult(googleClient.getSignInIntent(), RC_GOOGLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Google sign in exitoso: " + account.getEmail());
                firebaseAuthWithGoogle(account);

            } catch (Exception e) {
                Log.e(TAG, "Error en Google sign in: " + e.getMessage());
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "Autenticando con Firebase usando Google...");

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error en Firebase auth con Google: " + task.getException());
                Toast.makeText(this, "Error con FirebaseAuth Google", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Log.e(TAG, "Usuario null después de auth con Google");
                return;
            }

            String uid = user.getUid();
            Log.d(TAG, "Autenticado con Google, UID: " + uid);

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(snapshot -> {

                        if (!snapshot.exists()) {
                            Log.d(TAG, "Creando nuevo documento para usuario de Google");

                            Map<String, Object> data = new HashMap<>();
                            data.put("email", account.getEmail());
                            data.put("name", account.getGivenName() != null ? account.getGivenName() : "");
                            data.put("last_name", account.getFamilyName() != null ? account.getFamilyName() : "");
                            data.put("profile_image",
                                    account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "");
                            data.put("rol", "cliente"); // Asegurar que tenga rol
                            data.put("status", "activo"); //Asegurar que tenga status

                            db.collection("users").document(uid).set(data)
                                    .addOnSuccessListener(v -> {
                                        Log.d(TAG, "Documento creado para nuevo usuario");
                                        cargarYGuardarSesion(uid);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error al crear documento: " + e.getMessage());
                                        Toast.makeText(this, "Error al guardar datos", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Log.d(TAG, "Usuario existente en Firestore");
                            cargarYGuardarSesion(uid);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al verificar usuario: " + e.getMessage());
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void cargarYGuardarSesion(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    Usuario user = Usuario.fromSnapshot(snapshot);
                    new UserSessionManager(this).saveUser(user);

                    Log.d(TAG, "Sesión guardada correctamente");
                    irAProfile();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos finales: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                });
    }

    private void irAProfile() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra("FROM_LOGIN", true); // ⭐ CLAVE
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        finish();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            irAProfile();
        }
    }

}
