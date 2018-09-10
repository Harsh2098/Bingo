package com.hmproductions.bingo.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.BingoStreamServiceGrpc;
import com.hmproductions.bingo.R;
import com.hmproductions.bingo.adapter.ColorRecyclerAdapter;
import com.hmproductions.bingo.dagger.ContextModule;
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.ui.GameActivity;
import com.hmproductions.bingo.ui.SplashActivity;
import com.hmproductions.bingo.ui.settings.SettingsActivity;
import com.hmproductions.bingo.utils.ConnectionUtils;
import com.hmproductions.bingo.utils.Constants;
import com.hmproductions.bingo.utils.Miscellaneous;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hmproductions.bingo.ui.main.RoomFragment.ROOM_NAME_BUNDLE_KEY;
import static com.hmproductions.bingo.ui.main.RoomFragment.TIME_LIMIT_BUNDLE_KEY;
import static com.hmproductions.bingo.utils.Constants.FIRST_TIME_OPENED_KEY;

public class MainActivity extends AppCompatActivity implements
        ConnectionUtils.OnNetworkDownHandler,
        Miscellaneous.OnFragmentChangeRequest,
        HomeFragment.GetDetails,
        Miscellaneous.OnSnackBarRequest,
        ColorRecyclerAdapter.OnColorSelected {

    public static final String PLAYER_LEFT_KEY = "player-left-key";
    private static final String PLAYER_NAME_KEY = "player-name-key";
    private static final String PLAYER_COLOR_KEY = "player-color-key";

    private static final int COLOR_ROW_SPAN = 4;

    @Inject
    BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    BingoStreamServiceGrpc.BingoStreamServiceStub streamServiceStub;

    @Inject
    SharedPreferences preferences;

    @BindView(R.id.playerName_editText)
    EditText playerNameEditText;

    @BindView(R.id.color_recyclerView)
    RecyclerView colorRecyclerView;

    @BindView(R.id.main_toolbar)
    Toolbar mainToolbar;

    static int currentPlayerId = -1, currentRoomId = -1;
    static ArrayList<Player> playersList = new ArrayList<>();

    ColorRecyclerAdapter colorRecyclerAdapter;
    AlertDialog helpDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        DaggerBingoApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);

        mainToolbar.setTitle("");
        setSupportActionBar(mainToolbar);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (getIntent().getAction() != null &&
                (getIntent().getAction().equals(Constants.QUIT_GAME_ACTION) || getIntent().getAction().equals(Constants.RECONNECT_ACTION))) {
            if (!getIntent().getBooleanExtra(PLAYER_LEFT_KEY, true)) {
                currentPlayerId = getIntent().getIntExtra(GameActivity.PLAYER_ID, currentPlayerId);
                currentRoomId = getIntent().getIntExtra(GameActivity.ROOM_ID, currentRoomId);

                RoomFragment roomFragment = new RoomFragment();

                Bundle bundle = new Bundle();
                bundle.putInt(TIME_LIMIT_BUNDLE_KEY, getIntent().getIntExtra(TIME_LIMIT_BUNDLE_KEY, 2));
                bundle.putString(ROOM_NAME_BUNDLE_KEY, getIntent().getStringExtra(GameActivity.ROOM_NAME_EXTRA_KEY));
                roomFragment.setArguments(bundle);//TODO

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.addRule(RelativeLayout.BELOW, R.id.nameTextInputLayout);
                findViewById(R.id.fragment_container).setLayoutParams(params);
                colorRecyclerView.setVisibility(View.GONE);

                playerNameEditText.setEnabled(false);

                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, roomFragment).commit();
                return;
            }
        }

        currentPlayerId = currentRoomId = -1;

        startTapTargetSequence();
        preferences.edit().putBoolean(FIRST_TIME_OPENED_KEY, false).apply();
    }

    private void setupColorRecyclerView() {
        colorRecyclerAdapter = new ColorRecyclerAdapter(this, getResources().getStringArray(R.array.colorsHex), this);

        colorRecyclerView.setLayoutManager(new GridLayoutManager(this, COLOR_ROW_SPAN));
        colorRecyclerView.setAdapter(colorRecyclerAdapter);
        colorRecyclerView.setHasFixedSize(true);
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
        else if (item.getItemId() == R.id.help_action) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setCustomTitle(getLayoutInflater().inflate(R.layout.help_title, null))
                    .setView(getLayoutInflater().inflate(R.layout.help_box, null))
                    .setCancelable(true)
                    .show();
            helpDialog = builder.create();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupColorRecyclerView();

        playerNameEditText.setText(preferences.getString(PLAYER_NAME_KEY, ""));
        colorRecyclerAdapter.setSelected(preferences.getInt(PLAYER_COLOR_KEY, 0));
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PLAYER_NAME_KEY, playerNameEditText.getText().toString());
        editor.putInt(PLAYER_COLOR_KEY, colorRecyclerAdapter.getSelectedPosition());
        editor.apply();
    }

    @Override
    public void changeFragment(String roomName, int timeLimit, View view) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (currentFragment instanceof RoomFragment) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, getHomeFragment(false)).commit();
            playerNameEditText.setEnabled(true);

            params.addRule(RelativeLayout.BELOW, R.id.color_recyclerView);
            findViewById(R.id.fragment_container).setLayoutParams(params);
            colorRecyclerView.setVisibility(View.VISIBLE);

        } else {
            RoomFragment roomFragment = new RoomFragment();

            if (roomName != null) {
                Bundle bundle = new Bundle();
                bundle.putString(RoomFragment.ROOM_NAME_BUNDLE_KEY, roomName);
                bundle.putInt(TIME_LIMIT_BUNDLE_KEY, timeLimit);
                roomFragment.setArguments(bundle);
            }

            if (view != null) {
                getSupportFragmentManager().beginTransaction().addSharedElement(view, getString(R.string.room_name_transition))
                        .replace(R.id.fragment_container, roomFragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, roomFragment).commit();
            }

            params.addRule(RelativeLayout.BELOW, R.id.nameTextInputLayout);
            findViewById(R.id.fragment_container).setLayoutParams(params);
            colorRecyclerView.setVisibility(View.GONE);

            playerNameEditText.setEnabled(false);

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    @Override
    public void finishCurrentActivity() {
        finish();
    }

    @Override
    public void onNetworkDownError() {
        TextView bingoTextView = mainToolbar.findViewById(R.id.toolbarName_textView);

        startActivity(new Intent(this, SplashActivity.class), ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, bingoTextView, bingoTextView.getTransitionName()).toBundle());
        finish();
    }

    @Nullable
    @Override
    public Player getUserDetails() {

        if (playerNameEditText.getText().toString().isEmpty() || playerNameEditText.getText().toString().equals("")) {
            playerNameEditText.setError("A-Z, 0-9");
            Toast.makeText(this, "Please enter the name", Toast.LENGTH_SHORT).show();
            return null;
        } else if (currentRoomId != -1) {
            Toast.makeText(this, "Player already joined", Toast.LENGTH_SHORT).show();
            return null;
        }

        return new Player(playerNameEditText.getText().toString(),
                getResources().getStringArray(R.array.colorsName)[colorRecyclerAdapter.getSelectedPosition()], -1, false);
    }

    private HomeFragment getHomeFragment(boolean showSnackbar) {

        Bundle bundle = new Bundle();
        bundle.putBoolean(SplashActivity.SHOW_SNACKBAR_KEY, showSnackbar && getIntent().getBooleanExtra(SplashActivity.SHOW_SNACKBAR_KEY, false));

        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);

        return homeFragment;
    }

    private void startTapTargetSequence() {

        if (preferences.getBoolean(FIRST_TIME_OPENED_KEY, true)) {

            HomeFragment homeFragment = getHomeFragment(false);

            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment).commit();

            new Handler().postDelayed(() -> {
                        if (homeFragment.getView() != null) {
                            new TapTargetSequence(this).targets(
                                    TapTarget
                                            .forToolbarMenuItem(mainToolbar, R.id.settings_action, "Settings", "Toggle settings to call numbers instead of tapping on them")
                                            .targetRadius(50)
                                            .drawShadow(true),
                                    TapTarget
                                            .forView(homeFragment.getView().findViewById(R.id.noRoomsFound_textView), "Refresh Rooms", "Swipe downwards to refresh rooms list anytime")
                                            .targetRadius(120).cancelable(true).icon(getDrawable(R.drawable.swipe_icon)).drawShadow(true)
                            ).listener(new TapTargetSequence.Listener() {
                                @Override
                                public void onSequenceFinish() {
                                    homeFragment.onRefresh();
                                }

                                @Override
                                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                                }

                                @Override
                                public void onSequenceCanceled(TapTarget lastTarget) {

                                }
                            })
                                    .continueOnCancel(true)
                                    .start();
                        }
                    }
                    , 450);


            preferences.edit().putBoolean(FIRST_TIME_OPENED_KEY, false).apply();

        } else
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, getHomeFragment(true)).commit();

    }

    @Override
    public void showSnackBar(String message, int duration) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, duration);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (message.equals("No rooms available"))
            snackbar.setAction("Host", view -> ((HomeFragment) currentFragment).onHostButtonClick());
        else if (message.equals("Room does not exist"))
            snackbar.setAction("Refresh", view -> ((HomeFragment) currentFragment).onRefresh());

        if (currentFragment.getView() != null) {
            FloatingActionButton hostFab = currentFragment.getView().findViewById(R.id.host_fab);

            hostFab.hide();
            new Handler().postDelayed(hostFab::show, duration == Snackbar.LENGTH_LONG ? 3500 : 2000);
        }

        snackbar.show();
    }

    @Override
    public void onColorClick(int position) {
        colorRecyclerAdapter.setSelected(position);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /* Copyright 2016 Keepsafe Software Inc.

     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     * http://www.apache.org/licenses/LICENSE-2.0
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.

     */
}
