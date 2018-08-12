package com.hmproductions.bingo.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.R;
import com.hmproductions.bingo.actions.AddPlayerResponse;
import com.hmproductions.bingo.actions.GetRoomsResponse;
import com.hmproductions.bingo.actions.HostRoomResponse;
import com.hmproductions.bingo.adapter.RoomsRecyclerAdapter;
import com.hmproductions.bingo.dagger.ContextModule;
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.data.Room;
import com.hmproductions.bingo.loaders.AddPlayerLoader;
import com.hmproductions.bingo.loaders.GetRoomsLoader;
import com.hmproductions.bingo.loaders.HostRoomLoader;
import com.hmproductions.bingo.ui.SplashActivity;
import com.hmproductions.bingo.utils.ConnectionUtils;
import com.hmproductions.bingo.utils.Miscellaneous;
import com.hmproductions.bingo.views.CustomPicker;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.hmproductions.bingo.ui.main.MainActivity.currentPlayerId;
import static com.hmproductions.bingo.ui.main.MainActivity.currentRoomId;
import static com.hmproductions.bingo.utils.Constants.ADD_PLAYER_LOADER_ID;
import static com.hmproductions.bingo.utils.Constants.GET_ROOMS_LOADER_ID;
import static com.hmproductions.bingo.utils.Constants.HOST_ROOM_LOADER_ID;
import static com.hmproductions.bingo.utils.Constants.MAX_PLAYERS;
import static com.hmproductions.bingo.utils.Constants.MIN_PLAYERS;
import static com.hmproductions.bingo.utils.Miscellaneous.hideKeyboardFrom;
import static com.hmproductions.bingo.utils.Miscellaneous.nameToIdHash;
import static com.hmproductions.bingo.utils.TimeLimitUtils.getEnumFromValue;
import static com.hmproductions.bingo.utils.TimeLimitUtils.getValueFromEnum;

public class HomeFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<GetRoomsResponse>,
        RoomsRecyclerAdapter.OnRoomItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String ROOM_ID_KEY = "roomId-key";
    private static final String ROOM_NAME_KEY = "room-name-key";
    private static final String ROOM_SIZE_KEY = "room-size-key";
    private static final String ROOM_TIME_LIMIT_KEY = "room-time-limit-key";
    private static final String ROOM_PASSWORD_KEY = "room-password-key";

    @Inject
    BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    SharedPreferences preferences;

    FloatingActionButton hostFab;
    ProgressBar homeProgressBar;
    AlertDialog hostRoomDialog;

    SwipeRefreshLayout roomSwipeRefreshLayout;
    RecyclerView roomsRecyclerView;

    TextView noRoomsTextView;
    View lastItemView = null;

    private ArrayList<Room> roomsArrayList = new ArrayList<>();

    GetDetails userDetails;

    ConnectionUtils.OnNetworkDownHandler networkDownHandler;
    Miscellaneous.OnFragmentChangeRequest fragmentChangeRequest;
    Miscellaneous.OnSnackBarRequest snackBarRequest;

    interface GetDetails {
        @Nullable
        Player getUserDetails();
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    private LoaderManager.LoaderCallbacks<HostRoomResponse> hostRoomLoader = new LoaderManager.LoaderCallbacks<HostRoomResponse>() {
        @NonNull
        @Override
        public Loader<HostRoomResponse> onCreateLoader(int id, @Nullable Bundle args) {

            showHomeProgressBar(true);
            Player player = userDetails.getUserDetails();

            if (player != null && args != null && getContext() != null && args.getString(ROOM_NAME_KEY) != null && args.getString(ROOM_PASSWORD_KEY) != null) {

                currentPlayerId = nameToIdHash(player.getName());
                player.setId(currentPlayerId);

                Room room = new Room(-1, -1, args.getInt(ROOM_SIZE_KEY), args.getString(ROOM_NAME_KEY),
                        getEnumFromValue(args.getInt(ROOM_TIME_LIMIT_KEY)), !args.getString(ROOM_PASSWORD_KEY).equals("-1"));

                return new HostRoomLoader(getContext(), actionServiceBlockingStub, room, player, args.getString(ROOM_PASSWORD_KEY));
            }

            return null;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<HostRoomResponse> loader, HostRoomResponse data) {

            showHomeProgressBar(false);
            if (data == null) {
                networkDownHandler.onNetworkDownError();
            } else {
                if (data.getStatusCode() == HostRoomResponse.StatusCode.INTERNAL_SERVER_ERROR) {
                    currentPlayerId = -1;
                    snackBarRequest.showSnackBar(data.getStatusMessage(), Snackbar.LENGTH_SHORT);

                } else if (data.getStatusCode() == HostRoomResponse.StatusCode.NAME_TAKEN) {
                    Toast.makeText(getActivity(), data.getStatusMessage(), Toast.LENGTH_SHORT).show();

                    hostFab.callOnClick();
                } else {
                    snackBarRequest.showSnackBar(data.getStatusMessage(), Snackbar.LENGTH_SHORT);
                    currentRoomId = data.getRoomId();

                    fragmentChangeRequest.changeFragment(preferences.getString(ROOM_NAME_KEY, "New Room"),
                            preferences.getInt(ROOM_TIME_LIMIT_KEY, 1), null);
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<HostRoomResponse> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<AddPlayerResponse> addPlayerLoader = new LoaderManager.LoaderCallbacks<AddPlayerResponse>() {
        @NonNull
        @Override
        public Loader<AddPlayerResponse> onCreateLoader(int id, @Nullable Bundle args) {

            Player rawPlayer = userDetails.getUserDetails();
            showHomeProgressBar(true);

            if (rawPlayer != null && args != null) {

                currentPlayerId = nameToIdHash(rawPlayer.getName());

                return new AddPlayerLoader(getContext(), actionServiceBlockingStub, args.getInt(ROOM_ID_KEY),
                        new Player(rawPlayer.getName(), rawPlayer.getColor(), currentPlayerId, false), args.getString(ROOM_PASSWORD_KEY));
            }

            return new AddPlayerLoader(getContext(), actionServiceBlockingStub, -1,
                    new Player("", "", -1, false), "-1");
        }

        @Override
        public void onLoadFinished(@NonNull Loader<AddPlayerResponse> loader, AddPlayerResponse data) {

            showHomeProgressBar(false);
            if (data == null) {
                networkDownHandler.onNetworkDownError();
            } else {
                switch (data.getStatusCodeValue()) {

                    case AddPlayerResponse.StatusCode.OK_VALUE:
                        snackBarRequest.showSnackBar(data.getStatusMessage(), Snackbar.LENGTH_LONG);
                        currentRoomId = data.getRoomId();
                        fragmentChangeRequest.changeFragment(getRoomNameFromRoomId(currentRoomId),
                                getRoomTimeLimitValueFromRoomId(currentRoomId),
                                lastItemView.findViewById(R.id.roomName_textView));
                        break;

                    case AddPlayerResponse.StatusCode.COLOR_TAKEN_VALUE:
                        Toast.makeText(getActivity(), data.getStatusMessage(), Toast.LENGTH_SHORT).show();
                        break;

                    case AddPlayerResponse.StatusCode.SERVER_ERROR_VALUE:
                        break;

                    case AddPlayerResponse.StatusCode.ROOM_FULL_VALUE:
                    default:
                        snackBarRequest.showSnackBar(data.getStatusMessage(), Snackbar.LENGTH_SHORT);
                        currentPlayerId = -1;
                        break;
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<AddPlayerResponse> loader) {
            // Do nothing
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            networkDownHandler = (ConnectionUtils.OnNetworkDownHandler) context;
            fragmentChangeRequest = (Miscellaneous.OnFragmentChangeRequest) context;
            userDetails = (GetDetails) context;
            snackBarRequest = (Miscellaneous.OnSnackBarRequest) context;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " must implement network down handler.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View customView = inflater.inflate(R.layout.fragment_home, container, false);

        if (getContext() != null)
            DaggerBingoApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        ButterKnife.bind(this, customView);

        hostFab = customView.findViewById(R.id.host_fab);
        homeProgressBar = customView.findViewById(R.id.home_progressBar);
        roomSwipeRefreshLayout = customView.findViewById(R.id.roomRecyclerView_swipeRefreshLayout);
        roomSwipeRefreshLayout.setOnRefreshListener(this);

        roomsRecyclerView = customView.findViewById(R.id.rooms_recyclerView);
        noRoomsTextView = customView.findViewById(R.id.noRoomsFound_textView);

        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        roomsRecyclerView.setHasFixedSize(false);

        getLoaderManager().restartLoader(GET_ROOMS_LOADER_ID, null, this);

        return customView;
    }

    @OnClick(R.id.host_fab)
    void onHostButtonClick() {
        View hostRoomView = LayoutInflater.from(getContext()).inflate(R.layout.host_room_view, null);

        EditText roomNameEditText = hostRoomView.findViewById(R.id.roomName_editText);
        EditText passwordEditText = hostRoomView.findViewById(R.id.password_editText);

        Player hostPlayer = userDetails.getUserDetails();

        if (hostPlayer == null)
            return;
        else {
            String sampleName = userDetails.getUserDetails().getName() + "'s room";
            roomNameEditText.setText(preferences.getString(ROOM_NAME_KEY, sampleName));
        }

        CustomPicker countPicker = hostRoomView.findViewById(R.id.count_picker);
        countPicker.setMinValue(MIN_PLAYERS);
        countPicker.setMaxValue(MAX_PLAYERS);
        countPicker.setValue(preferences.getInt(ROOM_SIZE_KEY, MIN_PLAYERS + 1));
        countPicker.setWrapSelectorWheel(false);

        CustomPicker timeLimitPicker = hostRoomView.findViewById(R.id.timeLimit_picker);
        timeLimitPicker.setMinValue(0);
        timeLimitPicker.setMaxValue(getResources().getStringArray(R.array.timeLimitOptions).length - 1);
        timeLimitPicker.setValue(preferences.getInt(ROOM_TIME_LIMIT_KEY, 1));
        timeLimitPicker.setDisplayedValues(getResources().getStringArray(R.array.timeLimitOptions));
        timeLimitPicker.setWrapSelectorWheel(false);

        if (getContext() != null) {
            hostRoomDialog = new AlertDialog.Builder(getContext())
                    .setView(hostRoomView)
                    .setNegativeButton("Cancel", (dI, i) -> dI.dismiss())
                    .setCancelable(true)
                    .setPositiveButton("Host", null)
                    .create();

            hostRoomDialog.setOnShowListener(dialogInterface -> {
                Button positiveButton = hostRoomDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener((view) -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt(ROOM_SIZE_KEY, countPicker.getValue());
                    bundle.putInt(ROOM_TIME_LIMIT_KEY, timeLimitPicker.getValue());

                    if (passwordEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().equals("")) {
                        bundle.putString(ROOM_PASSWORD_KEY, "-1");
                    } else {
                        bundle.putString(ROOM_PASSWORD_KEY, passwordEditText.getText().toString());
                    }

                    if (roomNameEditText.getText().toString().isEmpty() || roomNameEditText.getText().toString().equals("")) {
                        roomNameEditText.setError("Room name empty");
                    } else {
                        bundle.putString(ROOM_NAME_KEY, roomNameEditText.getText().toString());
                        preferences.edit().putString(ROOM_NAME_KEY, roomNameEditText.getText().toString()).apply();
                        preferences.edit().putInt(ROOM_SIZE_KEY, countPicker.getValue()).apply();
                        preferences.edit().putInt(ROOM_TIME_LIMIT_KEY, timeLimitPicker.getValue()).apply();

                        getLoaderManager().restartLoader(HOST_ROOM_LOADER_ID, bundle, hostRoomLoader);

                        dialogInterface.dismiss();

                        hideKeyboardFrom(getContext(), view);
                    }
                });
            });

            hostRoomDialog.show();
        }
    }

    @NonNull
    @Override
    public Loader<GetRoomsResponse> onCreateLoader(int id, @Nullable Bundle args) {

        roomSwipeRefreshLayout.setRefreshing(true);
        return getContext() != null ? new GetRoomsLoader(getContext(), actionServiceBlockingStub) : null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<GetRoomsResponse> loader, GetRoomsResponse data) {

        roomSwipeRefreshLayout.setRefreshing(false);

        if (data == null)
            networkDownHandler.onNetworkDownError();
        else {
            switch (data.getStatusCode()) {
                case OK:
                    noRoomsTextView.setVisibility(View.INVISIBLE);
                    roomsRecyclerView.setVisibility(View.VISIBLE);

                    roomsArrayList.clear();
                    for (com.hmproductions.bingo.models.Room currentRoom : data.getRoomsList()) {
                        roomsArrayList.add(new Room(currentRoom.getRoomId(), currentRoom.getCount(), currentRoom.getMaxSize(),
                                currentRoom.getRoomName(), getEnumFromValue(currentRoom.getTimeLimitValue()), currentRoom.getPasswordExists()));
                    }

                    if (getContext() != null)
                        roomsRecyclerView.setAdapter(new RoomsRecyclerAdapter(getContext(), roomsArrayList, this));
                    break;

                case NO_ROOMS:
                    noRoomsTextView.setVisibility(View.VISIBLE);
                    roomsRecyclerView.setVisibility(View.INVISIBLE);

                    if (getActivity() != null && getView() != null && getArguments() != null && getArguments().getBoolean(SplashActivity.SHOW_SNACKBAR_KEY)) {
                        snackBarRequest.showSnackBar(data.getStatusMessage(), Snackbar.LENGTH_SHORT);
                    }
                    break;

                default:
                    Toast.makeText(getContext(), data.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<GetRoomsResponse> loader) {
        // Do nothing
    }

    @Override
    public void onRoomClick(@NonNull View view, int position) {

        Room currentRoom = roomsArrayList.get(position);

        if (currentRoom.getMaxSize() == currentRoom.getCount()) {
            snackBarRequest.showSnackBar("Room is full", Snackbar.LENGTH_SHORT);
            currentPlayerId = -1;
        } else {
            Bundle bundle = new Bundle();
            bundle.putInt(ROOM_ID_KEY, currentRoom.getRoomId());

            lastItemView = view;

            if (currentRoom.getPasswordExists()) {
                addFromDialog(bundle);
                return;
            }
            else
                bundle.putString(ROOM_PASSWORD_KEY, "-1");

            getLoaderManager().restartLoader(ADD_PLAYER_LOADER_ID, bundle, addPlayerLoader);
        }
    }

    @Nullable
    private String getRoomNameFromRoomId(int id) {
        for (Room room : roomsArrayList) {
            if (room.getRoomId() == id)
                return room.getName();
        }
        return null;
    }

    private int getRoomTimeLimitValueFromRoomId(int id) {
        for (Room room : roomsArrayList) {
            if (room.getRoomId() == id)
                return getValueFromEnum(room.getTimeLimit());
        }
        return -1;
    }

    @Override
    public void onRefresh() {
        roomSwipeRefreshLayout.setRefreshing(true);
        getLoaderManager().restartLoader(GET_ROOMS_LOADER_ID, null, this);
    }

    private void showHomeProgressBar(boolean show) {
        homeProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) hostFab.hide();
        else hostFab.show();
    }

    private void addFromDialog(Bundle bundle) {
        if (getContext() != null) {

            View passwordDialogView = LayoutInflater.from(getContext()).inflate(R.layout.password_dialog_view, null);

            EditText passwordEditText = passwordDialogView.findViewById(R.id.password_editText);

            final AlertDialog passwordDialog = new AlertDialog.Builder(getContext())
                    .setView(passwordDialogView)
                    .setNegativeButton("Cancel", (dI, i) -> dI.dismiss())
                    .setCancelable(true)
                    .setPositiveButton("Join", null)
                    .create();

            passwordDialog.setOnShowListener(dI -> {
                Button positiveButton = passwordDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener((view) -> {
                    if (passwordEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().equals("")) {
                        passwordEditText.setError("Enter the password");
                    } else {
                        bundle.putString(ROOM_PASSWORD_KEY, passwordEditText.getText().toString());
                        getLoaderManager().restartLoader(ADD_PLAYER_LOADER_ID, bundle, addPlayerLoader);
                        hideKeyboardFrom(getContext(), view);
                        dI.dismiss();
                    }
                });
            });

            passwordDialog.show();
        }
    }
}
