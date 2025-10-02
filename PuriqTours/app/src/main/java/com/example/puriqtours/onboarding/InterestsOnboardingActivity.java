package com.example.puriqtours.onboarding;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.puriqtours.R;

import java.util.ArrayList;

public class InterestsOnboardingActivity extends AppCompatActivity {

    public String selectedLanguageCode = null; // "es-PE", "en", "qu", "ay", "es-419"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests_onboarding);

        // Botón atrás (asegúrate que exista en el XML con este id)
        View back = findViewById(R.id.btnBackOnboarding);
        if (back != null) {
            back.setOnClickListener(v -> {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            });
        }

        // Primer paso: Idiomas
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.onboarding_container, new LanguagesFragment())
                    .commit();
        }
    }

    /** Llamado por LanguagesFragment: va a REGIONES */
    public void onLanguageChosen(String code) {
        // Debug visual para confirmar que llegaste aquí:
        android.widget.Toast.makeText(this, "Idioma elegido: " + code, android.widget.Toast.LENGTH_SHORT).show();

        getSharedPreferences("onboarding", MODE_PRIVATE)
                .edit().putString("language", code).apply();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.onboarding_container, new RegionsFragment())
                .addToBackStack(null)
                .commit();
    }


    /** Llamado por RegionsFragment: va a INTERESES */
    public void onRegionsChosen(ArrayList<String> regions) {
        getSharedPreferences("onboarding", MODE_PRIVATE)
                .edit().putString("regions", android.text.TextUtils.join(",", regions))
                .apply();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.onboarding_container, new PreferencesFragment())
                .addToBackStack(null)
                .commit();
    }

    /** Llamado por PreferencesFragment: termina (o navega adonde prefieras) */
    public void onPreferencesChosen(ArrayList<String> prefs) {
        getSharedPreferences("onboarding", MODE_PRIVATE)
                .edit().putString("preferences", android.text.TextUtils.join(",", prefs))
                .apply();

        // TODO: si quieres, envía todo a Firestore aquí

        finish(); // o empieza HomeActivity si quieres
    }
}
