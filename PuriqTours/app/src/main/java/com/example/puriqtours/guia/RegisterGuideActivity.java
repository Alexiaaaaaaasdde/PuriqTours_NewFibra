package com.example.puriqtours.guia;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.puriqtours.R;
import com.example.puriqtours.login.SetPasswordActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class RegisterGuideActivity extends AppCompatActivity {

    // 🔹 Campos base
    private EditText etName, etLastName, etEmail, etBirthDate, etPhone, etAddress, etDocumentNumber;
    private Spinner spnDocumentType;
    private Button btnSubmit;
    private ProgressBar progressBar;

    // 🔹 Idiomas
    private CheckBox cbSpanish, cbEnglish, cbFrench, cbGerman, cbItalian, cbChinese, cbJapanese;

    private FirebaseFirestore db;
    private HashMap<String, Object> userData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_guide);

        // ❌ QUITAR ActionBar (evita botón atrás automático)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();

        initViews();
        setupDatePicker();

        btnSubmit.setOnClickListener(v -> {
            if (!validateForm()) return;
            verifyEmailInFirestore();
        });
    }

    // ================= INIT =================

    private void initViews() {
        etName = findViewById(R.id.etName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etBirthDate = findViewById(R.id.etBirthDate);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etDocumentNumber = findViewById(R.id.etDocumentNumber);

        spnDocumentType = findViewById(R.id.spnDocumentType);
        btnSubmit = findViewById(R.id.btnSubmitGuide);
        progressBar = findViewById(R.id.progressBarGuide);

        cbSpanish = findViewById(R.id.cbSpanish);
        cbEnglish = findViewById(R.id.cbEnglish);
        cbFrench = findViewById(R.id.cbFrench);
        cbGerman = findViewById(R.id.cbGerman);
        cbItalian = findViewById(R.id.cbItalian);
        cbChinese = findViewById(R.id.cbChinese);
        cbJapanese = findViewById(R.id.cbJapanese);
    }

    private void setupDatePicker() {
        etBirthDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(
                    this,
                    (view, y, m, d) -> etBirthDate.setText(d + "/" + (m + 1) + "/" + y),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    // ================= VALIDATION =================

    private boolean validateForm() {
        clearErrors();
        boolean ok = true;

        if (isEmpty(etName)) {
            setError(etName, "Ingresa tu nombre");
            ok = false;
        }

        if (isEmpty(etLastName)) {
            setError(etLastName, "Ingresa tus apellidos");
            ok = false;
        }

        String email = getText(etEmail);
        if (TextUtils.isEmpty(email)) {
            setError(etEmail, "Ingresa tu correo");
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError(etEmail, "Correo inválido");
            ok = false;
        }

        // 📞 TELÉFONO → EXACTAMENTE 9 DÍGITOS
        String phone = getText(etPhone);
        if (!phone.matches("\\d{9}")) {
            setError(etPhone, "El teléfono debe tener 9 dígitos");
            ok = false;
        }

        if (spnDocumentType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecciona tipo de documento", Toast.LENGTH_SHORT).show();
            ok = false;
        }

        // 🪪 DOCUMENTO → EXACTAMENTE 9 DÍGITOS (DNI)
        String doc = getText(etDocumentNumber);
        if (!doc.matches("\\d{9}")) {
            setError(etDocumentNumber, "El DNI debe tener 9 dígitos");
            ok = false;
        }

        if (getSelectedLanguages().isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un idioma", Toast.LENGTH_SHORT).show();
            ok = false;
        }

        return ok;
    }

    // ================= FIRESTORE =================

    private void verifyEmailInFirestore() {
        String email = getText(etEmail);

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(this::handleEmailCheck);
    }

    private void handleEmailCheck(@NonNull Task<QuerySnapshot> task) {
        progressBar.setVisibility(View.GONE);
        btnSubmit.setEnabled(true);

        if (task.isSuccessful() && task.getResult() != null && task.getResult().isEmpty()) {
            proceedToSetPassword();
        } else {
            etEmail.setError("Este correo ya está registrado");
            etEmail.requestFocus();
            Toast.makeText(this, "Correo ya registrado", Toast.LENGTH_LONG).show();
        }
    }

    private void proceedToSetPassword() {
        userData.put("name", getText(etName));
        userData.put("last_name", getText(etLastName));
        userData.put("email", getText(etEmail));
        userData.put("birthdate", getText(etBirthDate));
        userData.put("phone", getText(etPhone));
        userData.put("address", getText(etAddress));
        userData.put("doc_type", spnDocumentType.getSelectedItem().toString());
        userData.put("document", getText(etDocumentNumber));

        // 🔐 CAMPOS CLAVE DEL GUÍA
        userData.put("rol", "Guia");
        userData.put("status", "Inactivo");
        userData.put("guide_status", "No habilitado");
        userData.put("languages", getSelectedLanguages());

        Intent intent = new Intent(this, SetPasswordActivity.class);
        intent.putExtra("userData", userData);
        startActivity(intent);
    }

    // ================= HELPERS =================

    private List<String> getSelectedLanguages() {
        List<String> langs = new ArrayList<>();
        if (cbSpanish.isChecked()) langs.add("Español");
        if (cbEnglish.isChecked()) langs.add("Inglés");
        if (cbFrench.isChecked()) langs.add("Francés");
        if (cbGerman.isChecked()) langs.add("Alemán");
        if (cbItalian.isChecked()) langs.add("Italiano");
        if (cbChinese.isChecked()) langs.add("Chino");
        if (cbJapanese.isChecked()) langs.add("Japonés");
        return langs;
    }

    private boolean isEmpty(EditText et) {
        return TextUtils.isEmpty(getText(et));
    }

    private String getText(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void setError(EditText et, String msg) {
        et.setError(msg);
    }

    private void clearErrors() {
        EditText[] fields = {etName, etLastName, etEmail, etPhone, etDocumentNumber};
        for (EditText et : fields) {
            if (et != null) et.setError(null);
        }
    }
}
