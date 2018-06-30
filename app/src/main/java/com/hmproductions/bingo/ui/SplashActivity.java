package com.hmproductions.bingo.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AnimationUtils;
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
import static com.hmproductions.bingo.utils.Constants.INTERNET_CONNECTION_LOADER_ID;

public class SplashActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<GetSessionIdResponse> {

    public static final String SHOW_SNACKBAR_KEY = "show-snackbar-key";

    @Inject
    ManagedChannel channel;

    private TextView loadingTextView;
    private MediaPlayer flybySound;

    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        DaggerBingoApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);

        loadingTextView = findViewById(R.id.loading_textView);
        TextView bingoAppNameTextView = findViewById(R.id.toolbarName_textView);

        flybySound = MediaPlayer.create(this, R.raw.flyby);
        flybySound.setVolume(0.8f, 0.8f);

        bingoAppNameTextView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_out_animation));
        flybySound.start();

        if (!isGooglePlayServicesAvailable(this)) {
            AlertDialog.Builder playServicesBuilder = new AlertDialog.Builder(this);
            playServicesBuilder
                    .setMessage("Bingo requires latest version of google play services.")
                    .setPositiveButton("Close", (dialogInterface, i) -> finish())
                    .setTitle("Update PlayServices")
                    .setCancelable(true)
                    .show();
        }

        actionServiceBlockingStub = BingoActionServiceGrpc.newBlockingStub(channel);

        bingoAppNameTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_out_animation));

        new Handler().postDelayed(this::startMainActivity, 1000);
    }

    private void startMainActivity() {


        if (!getConnectionInfo(this)) {
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
        return new SessionIdLoader(this, actionServiceBlockingStub);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<GetSessionIdResponse> loader, GetSessionIdResponse data) {
        Handler handler = new Handler();

        if (data != null) {
            Constants.SESSION_ID = data.getSessionId();

            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            mainActivityIntent.putExtra(SHOW_SNACKBAR_KEY, true);

            TextView bingoTextView = findViewById(R.id.toolbarName_textView);

            startActivity(mainActivityIntent, ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, bingoTextView, bingoTextView.getTransitionName()).toBundle());
            finish();

        } else {
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

    @Override
    protected void onPause() {
        super.onPause();
        flybySound.stop();
    }
}
