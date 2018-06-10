package com.hmproductions.bingo.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.BingoStreamServiceGrpc;
import com.hmproductions.bingo.R;
import com.hmproductions.bingo.dagger.ContextModule;
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.ui.SettingsActivity;
import com.hmproductions.bingo.ui.SplashActivity;
import com.hmproductions.bingo.utils.ConnectionUtils;
import com.hmproductions.bingo.utils.Miscellaneous;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        ConnectionUtils.OnNetworkDownHandler,
        Miscellaneous.OnFragmentChangeRequest,
        HomeFragment.GetDetails {

    public static final String PLAYER_LEFT_ID = "player-left-id";

    private static final String PLAYER_NAME_KEY = "player-name-key";
    private static final String PLAYER_COLOR_KEY = "player-color-key";

    @Inject
    BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    BingoStreamServiceGrpc.BingoStreamServiceStub streamServiceStub;

    @Inject
    SharedPreferences preferences;

    @BindView(R.id.playerName_editText)
    EditText playerNameEditText;

    @BindView(R.id.color_picker)
    NumberPicker colorPicker;

    static int currentPlayerId = -1, currentRoomId = -1;
    static ArrayList<Player> playersList = new ArrayList<>();

    AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        DaggerBingoApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);

        setupColorPicker();

        View dialogView = LayoutInflater.from(this).inflate(R.layout.loading_dialog, null);
        ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.processing_request);
        loadingDialog = new AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create();

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new HomeFragment()).commit();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void setupColorPicker() {
        colorPicker.setMinValue(0);
        colorPicker.setMaxValue(getResources().getStringArray(R.array.colorsName).length - 1);
        colorPicker.setDisplayedValues(getResources().getStringArray(R.array.colorsName));
        colorPicker.setWrapSelectorWheel(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.settings_action)
            startActivity(new Intent(this, SettingsActivity.class));

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        playerNameEditText.setText(preferences.getString(PLAYER_NAME_KEY, ""));
        colorPicker.setValue(preferences.getInt(PLAYER_COLOR_KEY, 0));
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PLAYER_NAME_KEY, playerNameEditText.getText().toString());
        editor.putInt(PLAYER_COLOR_KEY, colorPicker.getValue());
        editor.apply();
    }

    @Override
    public void changeFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof RoomFragment) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            playerNameEditText.setEnabled(true);
            colorPicker.setEnabled(true);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RoomFragment()).commit();
            playerNameEditText.setEnabled(false);
            colorPicker.setEnabled(false);
        }
    }

    @Override
    public void finishCurrentActivity() {
        finish();
    }

    @Override
    public void onNetworkDownError() {
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    @Nullable
    @Override
    public Player getUserDetails() {

        if (playerNameEditText.getText().toString().isEmpty()) {
            playerNameEditText.setError("A-Z, 0-9");
            Toast.makeText(this, "Please enter the name", Toast.LENGTH_SHORT).show();
            return null;
        } else if (currentRoomId != -1) {
            Toast.makeText(this, "Player already joined", Toast.LENGTH_SHORT).show();
            return null;
        }

        return new Player(playerNameEditText.getText().toString(),
                getResources().getStringArray(R.array.colorsName)[colorPicker.getValue()], -1, false);
    }
}
