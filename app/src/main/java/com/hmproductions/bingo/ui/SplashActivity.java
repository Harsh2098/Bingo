package com.hmproductions.bingo.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hmproductions.bingo.R;

import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.ConnectionUtils.isGooglePlayServicesAvailable;
import static com.hmproductions.bingo.utils.ConnectionUtils.isReachableByTcp;
import static com.hmproductions.bingo.utils.Constants.SERVER_ADDRESS;
import static com.hmproductions.bingo.utils.Constants.SERVER_PORT;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (!isGooglePlayServicesAvailable(this)) {
            AlertDialog.Builder playServicesBuilder = new AlertDialog.Builder(this);
            playServicesBuilder
                    .setMessage("Dalal Street requires latest version of google play services.")
                    .setPositiveButton("Close", (dialogInterface, i) -> finish())
                    .setTitle("Update PlayServices")
                    .setCancelable(true)
                    .show();
        }

        startMainActivity();
    }

    private void startMainActivity() {

        Handler handler = new Handler();

        if (!getConnectionInfo(this)) {
            handler.postDelayed(() -> Snackbar
                    .make(findViewById(android.R.id.content), "Please check internet connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", v -> startMainActivity())
                    .show(), 500);
            return;
        }

        if (isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {
            
            handler.postDelayed(() -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }, 1500);

        } else {
            handler.postDelayed(() -> Snackbar
                    .make(findViewById(android.R.id.content), "Server Unreachable", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", v -> startMainActivity())
                    .show(), 500);
        }
    }
}
