package com.hmproductions.bingo.ui.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hmproductions.bingo.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.PreferenceTheme)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
