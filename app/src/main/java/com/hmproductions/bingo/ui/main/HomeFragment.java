package com.hmproductions.bingo.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
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
import com.hmproductions.bingo.utils.ConnectionUtils;
import com.hmproductions.bingo.utils.Miscellaneous;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.OnClick;

import static com.hmproductions.bingo.ui.main.MainActivity.currentPlayerId;
import static com.hmproductions.bingo.ui.main.MainActivity.currentRoomId;
import static com.hmproductions.bingo.utils.Constants.ADD_PLAYER_LOADER_ID;
import static com.hmproductions.bingo.utils.Constants.GET_ROOMS_LOADER_ID;
import static com.hmproductions.bingo.utils.Constants.HOST_ROOM_LOADER_ID;
import static com.hmproductions.bingo.utils.Miscellaneous.nameToIdHash;

public class HomeFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<GetRoomsResponse>,
        RoomsRecyclerAdapter.OnRoomItemClickListener {

    private static final String ROOM_ID_KEY = "roomId-key";
    private static final String ROOM_NAME_KEY = "room-name-key";
    private static final String ROOM_SIZE_KEY = "room-size-key";

    @Inject
    BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    RecyclerView roomsRecyclerView;
    TextView noRoomsTextView;
    AlertDialog loadingDialog;

    private ArrayList<Room> roomsArrayList = new ArrayList<>();

    RoomsRecyclerAdapter roomsRecyclerAdapter;
    GetDetails userDetails;

    ConnectionUtils.OnNetworkDownHandler networkDownHandler;
    Miscellaneous.OnFragmentChangeRequest fragmentChangeRequest;

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

            Player player = userDetails.getUserDetails();

            if (player != null && args != null && getContext() != null) {
                currentPlayerId = nameToIdHash(player.getName());
                player.setId(currentPlayerId);

                Room room = new Room(-1, -1, args.getInt(ROOM_SIZE_KEY), args.getString(ROOM_NAME_KEY));

                return new HostRoomLoader(getContext(), actionServiceBlockingStub, room, player);
            }

            return null;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<HostRoomResponse> loader, HostRoomResponse data) {
            if (data == null) {
                networkDownHandler.onNetworkDownError();
            } else {
                if (data.getStatusCode() == HostRoomResponse.StatusCode.INTERNAL_SERVER_ERROR) {
                    currentPlayerId = -1;
                } else {
                    currentRoomId = data.getRoomId();
                    fragmentChangeRequest.changeFragment();
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

            if (rawPlayer != null && args != null) {


                loadingDialog.show();

                currentPlayerId = nameToIdHash(rawPlayer.getName());

                return new AddPlayerLoader(getContext(), actionServiceBlockingStub, args.getInt(ROOM_ID_KEY),
                        new Player(rawPlayer.getName(), rawPlayer.getColor(), currentPlayerId, false));
            }

            return null;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<AddPlayerResponse> loader, AddPlayerResponse data) {

            loadingDialog.dismiss();

            if (data == null) {
                networkDownHandler.onNetworkDownError();
            } else {

                switch (data.getStatusCodeValue()) {

                    case AddPlayerResponse.StatusCode.OK_VALUE:
                        currentRoomId = data.getRoomId();
                        fragmentChangeRequest.changeFragment();
                        break;

                    case AddPlayerResponse.StatusCode.ROOM_FULL_VALUE:
                    default:
                        currentPlayerId = -1;
                        Toast.makeText(getContext(), data.getStatusMessage(), Toast.LENGTH_SHORT).show();
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
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " must implement network down handler.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View customView = inflater.inflate(R.layout.fragment_home, container, false);

        DaggerBingoApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        roomsRecyclerView = customView.findViewById(R.id.rooms_recyclerView);
        noRoomsTextView = customView.findViewById(R.id.noRoomsFound_textView);

        if (getContext() != null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.loading_dialog, null);
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.processing_request);
            loadingDialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();
        }

        roomsRecyclerAdapter = new RoomsRecyclerAdapter(getContext(), null, this);

        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        roomsRecyclerView.setAdapter(roomsRecyclerAdapter);
        roomsRecyclerView.setHasFixedSize(false);

        getLoaderManager().restartLoader(GET_ROOMS_LOADER_ID, null, this);

        return customView;
    }

    @OnClick(R.id.host_button)
    void onHostButtonClick() {
        View hostRoomView = LayoutInflater.from(getContext()).inflate(R.layout.host_room_view, null);

        NumberPicker countPicker = hostRoomView.findViewById(R.id.count_picker);
        countPicker.setMinValue(2);
        countPicker.setMaxValue(4);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(hostRoomView)
                .setNegativeButton("Cancel", (dI, i) -> dI.dismiss())
                .setCancelable(true)
                .setPositiveButton("Host", ((dialogInterface, i) -> {
                    Bundle bundle = new Bundle();
                    bundle.putString(ROOM_NAME_KEY, ((EditText)hostRoomView.findViewById(R.id.roomName_editText)).getText().toString());
                    bundle.putInt(ROOM_SIZE_KEY, countPicker.getValue());

                    getLoaderManager().restartLoader(HOST_ROOM_LOADER_ID, bundle, hostRoomLoader);
                }));

        builder.show();
    }

    @OnClick(R.id.refreshRooms_button)
    void onRefreshButtonClick() {
        getLoaderManager().restartLoader(GET_ROOMS_LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<GetRoomsResponse> onCreateLoader(int id, @Nullable Bundle args) {
        loadingDialog.show();

        if (getContext() != null)
            return new GetRoomsLoader(getContext(), actionServiceBlockingStub);
        else
            return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<GetRoomsResponse> loader, GetRoomsResponse data) {

        loadingDialog.dismiss();

        if (data == null)
            networkDownHandler.onNetworkDownError();
        else {
            switch (data.getStatusCode()) {
                case OK:
                    noRoomsTextView.setVisibility(View.GONE);
                    roomsRecyclerView.setVisibility(View.VISIBLE);

                    for (com.hmproductions.bingo.models.Room currentRoom : data.getRoomsList()) {
                        roomsArrayList.add(new Room(currentRoom.getRoomId(), currentRoom.getCount(), currentRoom.getMaxSize(), currentRoom.getRoomName()));
                    }
                    roomsRecyclerAdapter.swapData(roomsArrayList);
                    break;

                case NO_ROOMS:
                    noRoomsTextView.setVisibility(View.VISIBLE);
                    roomsRecyclerView.setVisibility(View.GONE);
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
    public void onRoomClick(int position) {

        Bundle bundle = new Bundle();
        bundle.putInt(ROOM_ID_KEY, roomsArrayList.get(position).getRoomId());

        getLoaderManager().restartLoader(ADD_PLAYER_LOADER_ID, bundle, addPlayerLoader);
    }
}
