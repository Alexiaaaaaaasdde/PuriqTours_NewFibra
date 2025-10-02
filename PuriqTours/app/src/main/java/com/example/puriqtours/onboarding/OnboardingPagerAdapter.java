package com.example.puriqtours.onboarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OnboardingPagerAdapter extends FragmentStateAdapter {
    public final LanguagesFragment fLang = new LanguagesFragment();

    public final PreferencesFragment fPref = new PreferencesFragment();

    public OnboardingPagerAdapter(@NonNull FragmentActivity fa) { super(fa); }

    @NonNull @Override public Fragment createFragment(int position) {
        switch (position) {
            case 0: return fLang;
            default: return fPref;
        }
    }
    @Override public int getItemCount() { return 3; }
}

