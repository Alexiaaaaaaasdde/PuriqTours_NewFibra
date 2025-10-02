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


}
