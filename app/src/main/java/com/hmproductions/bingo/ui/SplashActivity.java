package com.hmproductions.bingo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.R;
import com.hmproductions.bingo.actions.GetSessionIdResponse;
import com.hmproductions.bingo.dagger.ContextModule;
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent;
import com.hmproductions.bingo.loaders.SessionIdLoader;
import com.hmproductions.bingo.ui.main.MainActivity;
import com.hmproductions.bingo.utils.Constants;

import javax.inject.Inject;

import io.grpc.ManagedChannel;

import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.ConnectionUtils.isGooglePlayServicesAvailable;
import static com.hmproductions.bingo.utils.Constants.CLASSIC_TAG;
import static com.hmproductions.bingo.utils.Constants.INTERNET_CONNECTION_LOADER_ID;
import static com.hmproductions.bingo.utils.Miscellaneous.convertDpToPixel;

public class SplashActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<GetSessionIdResponse> {

    @Inject
    ManagedChannel channel;

    private TextView loadingTextView;
    private ProgressBar loadingProgressBar;

    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        DaggerBingoApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);

        loadingTextView = findViewById(R.id.loading_textView);
        loadingProgressBar = findViewById(R.id.loading_progressBar);

        /* Setting layoutParams to match the width of the screen */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        LayoutParams layoutParams = new LayoutParams(displayMetrics.widthPixels, displayMetrics.heightPixels / 2);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        layoutParams.setMargins(0, (int) convertDpToPixel(this, 20), 0, 0);
        findViewById(R.id.bingo_imageView).setLayoutParams(layoutParams);

        if (!isGooglePlayServicesAvailable(this)) {
            AlertDialog.Builder playServicesBuilder = new AlertDialog.Builder(this);
            playServicesBuilder
                    .setMessage("Dalal Street requires latest version of google play services.")
                    .setPositiveButton("Close", (dialogInterface, i) -> finish())
                    .setTitle("Update PlayServices")
                    .setCancelable(true)
                    .show();
        }

        actionServiceBlockingStub = BingoActionServiceGrpc.newBlockingStub(channel);

        startMainActivity();
    }

    private void startMainActivity() {

        loadingProgressBar.setVisibility(View.VISIBLE);

        if (!getConnectionInfo(this)) {
            loadingProgressBar.setVisibility(View.GONE);

            loadingTextView.setText(R.string.internet_unavailable);
            new Handler().postDelayed(() -> Snackbar
                    .make(findViewById(android.R.id.content), "Please check internet connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", v -> startMainActivity())
                    .show(), 500);
            return;
        }

        getSupportLoaderManager().restartLoader(INTERNET_CONNECTION_LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<GetSessionIdResponse> onCreateLoader(int i, Bundle bundle) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        return new SessionIdLoader(this, actionServiceBlockingStub);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<GetSessionIdResponse> loader, GetSessionIdResponse data) {
        Handler handler = new Handler();

        if (data != null) {
            Constants.SESSION_ID = data.getSessionId();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            loadingProgressBar.setVisibility(View.GONE);
            loadingTextView.setText(R.string.server_unreachable);
            handler.postDelayed(() -> Snackbar
                    .make(findViewById(android.R.id.content), "Couldn't connect to server", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", v -> startMainActivity())
                    .show(), 500);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<GetSessionIdResponse> loader) {
        // Do nothing
    }
}
