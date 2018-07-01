package com.hmproductions.bingo.ui.settings;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.hmproductions.bingo.R;
import com.hmproductions.bingo.utils.Constants;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int RECORD_AUDIO_RC = 17;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Preference preference = findPreference(key);

        if (preference != null && preference instanceof SwitchPreference && ((SwitchPreference) preference).isChecked()) {

            if (key.equals(getString(R.string.tts_preference_key))) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_RC);
            }
            else if (key.equals(getString(R.string.tutorial_preference_key))) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.FIRST_TIME_PLAYED_KEY, true);
                editor.putBoolean(Constants.FIRST_TIME_OPENED_KEY, true);
                editor.putBoolean(Constants.FIRST_TIME_JOINED_KEY, true);
                editor.putBoolean(Constants.FIRST_TIME_WON_KEY, true);
                editor.apply();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ((SwitchPreference)findPreference(getString(R.string.tts_preference_key)))
                .setChecked(requestCode == RECORD_AUDIO_RC && grantResults[0] == PERMISSION_GRANTED);

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
