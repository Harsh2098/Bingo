package com.hmproductions.bingo.ui.settings

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.PreferenceFragmentCompat
import com.hmproductions.bingo.R
import com.hmproductions.bingo.utils.Constants

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = addPreferencesFromResource(R.xml.settings)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

        val preference = findPreference(key)

        if (preference != null && preference is SwitchPreference && preference.isChecked) {

            if (key == getString(R.string.tts_preference_key)) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_RC)
            } else if (key == getString(R.string.tutorial_preference_key)) {
                with(sharedPreferences.edit()) {
                    putBoolean(Constants.FIRST_TIME_PLAYED_KEY, true)
                    putBoolean(Constants.FIRST_TIME_OPENED_KEY, true)
                    putBoolean(Constants.FIRST_TIME_JOINED_KEY, true)
                    putBoolean(Constants.FIRST_TIME_WON_KEY, true)
                    apply()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        (findPreference(getString(R.string.tts_preference_key)) as SwitchPreference).isChecked = requestCode == RECORD_AUDIO_RC && grantResults[0] == PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    companion object {
        private const val RECORD_AUDIO_RC = 17
    }
}
