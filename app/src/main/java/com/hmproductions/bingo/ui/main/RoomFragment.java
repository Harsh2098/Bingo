package com.hmproductions.bingo.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.BingoStreamServiceGrpc;
import com.hmproductions.bingo.R;
import com.hmproductions.bingo.actions.RemovePlayerResponse;
import com.hmproductions.bingo.actions.SetPlayerReadyResponse;
import com.hmproductions.bingo.actions.Unsubscribe;
import com.hmproductions.bingo.adapter.ChatRecyclerAdapter;
import com.hmproductions.bingo.adapter.PlayersRecyclerAdapter;
import com.hmproductions.bingo.dagger.ContextModule;
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent;
import com.hmproductions.bingo.data.Message;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.datastreams.RoomEventUpdate;
import com.hmproductions.bingo.loaders.RemovePlayerLoader;
import com.hmproductions.bingo.loaders.SetReadyLoader;
import com.hmproductions.bingo.loaders.UnsubscribeLoader;
import com.hmproductions.bingo.models.RoomEvent;
import com.hmproductions.bingo.models.RoomSubscription;
import com.hmproductions.bingo.ui.GameActivity;
import com.hmproductions.bingo.utils.ConnectionUtils;
import com.hmproductions.bingo.utils.Constants;
import com.hmproductions.bingo.utils.Miscellaneous;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import static com.google.firebase.auth.FirebaseAuth.getInstance;
import static com.hmproductions.bingo.ui.main.MainActivity.currentPlayerId;
import static com.hmproductions.bingo.ui.main.MainActivity.currentRoomId;
import static com.hmproductions.bingo.ui.main.MainActivity.playersList;
import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.Constants.DEFAULT_MSG_LENGTH_LIMIT;
import static com.hmproductions.bingo.utils.Constants.FIRST_TIME_JOINED_KEY;
import static com.hmproductions.bingo.utils.Constants.PLAYER_ID_KEY;
import static com.hmproductions.bingo.utils.Miscellaneous.convertDpToPixel;
import static com.hmproductions.bingo.utils.Miscellaneous.getNameFromId;
import static com.hmproductions.bingo.utils.Miscellaneous.getTimeLimitString;
import static com.hmproductions.bingo.utils.Miscellaneous.hideKeyboardFrom;
import static com.hmproductions.bingo.utils.TimeLimitUtils.getEnumFromValue;

public class RoomFragment extends Fragment implements PlayersRecyclerAdapter.OnPlayerClickListener {

    public static final String PLAYER_READY_BUNDLE_KEY = "player-ready-bundle-key";
    public static final String ROOM_NAME_BUNDLE_KEY = "room-name-bundle-key";
    public static final String TIME_LIMIT_BUNDLE_KEY = "time-limit-bundle-key";

    @Inject
    BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    BingoStreamServiceGrpc.BingoStreamServiceStub streamServiceStub;

    @Inject
    ConnectivityManager connectivityManager;

    @Inject
    NetworkRequest networkRequest;

    @Inject
    SharedPreferences preferences;

    @BindView(R.id.chatRecyclerView)
    RecyclerView chatRecyclerView;

    @BindView(R.id.players_recyclerView)
    RecyclerView playersRecyclerView;

    @BindView(R.id.startConversationTextView)
    TextView startConversationTextView;

    @BindView(R.id.messageEditText)
    EditText messageEditText;

    @BindView(R.id.bottomSheetLayout)
    CardView bottomSheetLayout;

    @BindView(R.id.chatDateTextView)
    TextView chatDateTextView;

    @BindView(R.id.sendButton)
    FloatingActionButton sendButton;

    @BindView(R.id.notificationBubbleTextView)
    TextView messageCountTextView;

    @BindView(R.id.bottom_linearLayout)
    LinearLayout bottomLinearLayout;

    private int maxCount = Constants.MIN_PLAYERS; // Setting minimum maximum count to minimum number of players (2) by default

    Button leaveButton;
    TextView countTextView;
    ProgressBar leaveProgressBar;
    LinearLayoutManager linearLayoutManager;

    ConnectionUtils.OnNetworkDownHandler networkDownHandler;
    Miscellaneous.OnFragmentChangeRequest fragmentChangeRequest;
    ConnectivityManager.NetworkCallback networkCallback;

    private DatabaseReference chatDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private ChildEventListener firebaseChildEventListener;

    PlayersRecyclerAdapter playersRecyclerAdapter;
    ChatRecyclerAdapter chatRecyclerAdapter;

    private Player fakePlayer = new Player("", "", -1, false);
    private boolean wasDisconnected = true;
    private int messageCount = 0;

    private AdView roomBannerAdView;

    private BottomSheetBehavior<CardView> bottomSheetBehavior;

    public RoomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            networkDownHandler = (ConnectionUtils.OnNetworkDownHandler) context;
            fragmentChangeRequest = (Miscellaneous.OnFragmentChangeRequest) context;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " must implement network down handler.");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View customView = inflater.inflate(R.layout.fragment_room, container, false);

        if (getContext() != null)
            DaggerBingoApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        ButterKnife.bind(this, customView);

        countTextView = customView.findViewById(R.id.count_textView);
        leaveButton = customView.findViewById(R.id.leave_button);
        leaveProgressBar = customView.findViewById(R.id.quit_progressBar);

        if (getArguments() != null) {
            ((TextView) customView.findViewById(R.id.roomName_textView)).setText(getArguments().getString(ROOM_NAME_BUNDLE_KEY));
            ((TextView) customView.findViewById(R.id.timeLimitTextView)).setText(getTimeLimitString(getEnumFromValue(getArguments().getInt(TIME_LIMIT_BUNDLE_KEY))));
        }

        setupNetworkCallback();

        playersRecyclerAdapter = new PlayersRecyclerAdapter(playersList, getContext(), this);

        linearLayoutManager = new LinearLayoutManager(getContext());

        chatRecyclerAdapter = new ChatRecyclerAdapter(getContext(), null);

        setupChatBottomSheet();
        setupFirebaseChatEventListener();

        playersRecyclerView.setAdapter(playersRecyclerAdapter);
        playersRecyclerView.setLayoutManager(linearLayoutManager);
        playersRecyclerView.setHasFixedSize(false);

        return customView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupAds();
    }

    // Setting up bottom sheet
    private void setupChatBottomSheet() {

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) {
                    hideKeyboardFrom(getContext(), getView());
                }
                messageCount = 0;
                messageCountTextView.setVisibility(View.GONE);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatRecyclerAdapter);
        chatRecyclerView.setHasFixedSize(false);

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        chatDateTextView.setText(new SimpleDateFormat("dd MMM", Locale.US).format(calendar.getTime()));
    }

    // Setting up auth state listener and child event listener for chat
    private void setupFirebaseChatEventListener() {

        firebaseAuth = getInstance();
        chatDatabaseReference = FirebaseDatabase.getInstance().getReference().child("chats").child(String.valueOf(currentRoomId));

        firebaseChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                showChatRecyclerView(true);
                chatRecyclerAdapter.addMessage(dataSnapshot.getValue(Message.class));
                chatRecyclerView.smoothScrollToPosition(chatRecyclerAdapter.getItemCount() - 1);
                messageCount++;

                if (((bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) ||
                        (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED))) {
                    messageCountTextView.setVisibility(View.VISIBLE);
                    messageCountTextView.setText(messageCount > 9 ? "9+" : String.valueOf(messageCount));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot p0, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot p0) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot p0, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        firebaseAuthStateListener = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null) {
                chatDatabaseReference.addChildEventListener(firebaseChildEventListener);
            } else {
                firebaseAuth.signInAnonymously().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatDatabaseReference.addChildEventListener(firebaseChildEventListener);
                    } else {
                        showChatRecyclerView(false);
                        chatDatabaseReference.removeEventListener(firebaseChildEventListener);
                    }
                });
            }
        };
    }

    boolean getPlayerReady(int playerId) {
        for (Player currentPlayer : playersList) {
            if (currentPlayer.getId() == playerId)
                return currentPlayer.isReady();
        }
        return true;
    }

    private void setupAds() {
        if (getView() != null && getContext() != null) {
            // TODO (Release): Change App ID
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, (int) convertDpToPixel(getContext(), 50));
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

            roomBannerAdView = getView().findViewById(R.id.room_fragment_banner);

            roomBannerAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    roomBannerAdView.setVisibility(View.VISIBLE);
                    bottomLinearLayout.setLayoutParams(layoutParams);
                }
            });

            roomBannerAdView.loadAd(new AdRequest.Builder().build());
        }
    }

    private void setupNetworkCallback() {
        networkCallback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);

                if (wasDisconnected)
                    subscribeToRoomEventsUpdate(currentRoomId);
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                if (getContext() != null && !getConnectionInfo(getContext()))
                    wasDisconnected = true;
            }
        };
    }

    @OnClick(R.id.leave_button)
    void onLeaveButtonClick() {

        showQuitProgressBar(true);

        if (currentPlayerId != -1) {
            getLoaderManager().restartLoader(Constants.UNSUBSCRIBE_LOADER_ID, null, unsubscribeLoader);
            new Handler().postDelayed(() -> getLoaderManager().restartLoader(Constants.REMOVE_PLAYER_LOADER_ID, null, removePlayerLoader), 1000);
        } else
            Toast.makeText(getContext(), "You have not joined", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlayerClick(int position) {
        if (playersList.get(position).getId() == currentPlayerId) {
            this.getLoaderManager().restartLoader(Constants.READY_PLAYER_LOADER_ID, null, setPlayerReadyLoader);
        }
    }

    @OnClick(R.id.chatButton)
    void onChatButtonClick() {
        BottomSheetBehavior<CardView> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @OnClick(R.id.sendButton)
    void onSendMessageClick() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        String name = getNameFromId(playersList, currentPlayerId);
        if (name != null) {
            Message newMessage = new Message(messageEditText.getText().toString(), name,
                    new SimpleDateFormat("hh:mm a", Locale.US).format(calendar.getTime()));

            chatDatabaseReference.push().setValue(newMessage);
        }

        messageEditText.setText("");
    }

    @OnClick(R.id.hideChatButton)
    void onHideChatButtonClick() {
        BottomSheetBehavior<CardView> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void subscribeToRoomEventsUpdate(int roomId) {

        Metadata metadata = new Metadata();

        Metadata.Key<String> metadataKey = Metadata.Key.of(PLAYER_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(metadataKey, String.valueOf(currentPlayerId));

        MetadataUtils.attachHeaders(streamServiceStub, metadata).getRoomEventUpdates(RoomSubscription.newBuilder().setRoomId(roomId).setPlayerId(currentPlayerId).setDestroy(false).build(), new StreamObserver<RoomEventUpdate>() {
            @Override
            public void onNext(RoomEventUpdate value) {

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (value.getRoomEvent().getEventCode() == RoomEvent.EventCode.ROOM_DESTROY) {
                            exitRoom();
                        } else if (value.getRoomEvent().getEventCode() == RoomEvent.EventCode.ADD_PLAYER ||
                                value.getRoomEvent().getEventCode() == RoomEvent.EventCode.PLAYER_READY_CHANGED ||
                                value.getRoomEvent().getEventCode() == RoomEvent.EventCode.PLAYER_STATE_CHANGED ||
                                value.getRoomEvent().getEventCode() == RoomEvent.EventCode.REMOVE_PLAYER) {

                            List<com.hmproductions.bingo.models.Player> responseList = value.getRoomEvent().getPlayersList();
                            maxCount = value.getRoomEvent().getMaxCount();

                            int positionOfRemoval = -1, positionOfInsertion = -1;

                            if (playersList.size() < responseList.size() && playersList.size() > 0) {

                                com.hmproductions.bingo.models.Player currentPlayer = responseList.get(responseList.size() - 1);
                                playersList.add(new Player(currentPlayer.getName(), currentPlayer.getColor(), currentPlayer.getId(),
                                        currentPlayer.getReady()));
                                positionOfInsertion = playersList.size() - 1;

                            } else if (playersList.size() > responseList.size()) {
                                positionOfRemoval = deleteAndGetDeletedPosition(responseList);
                            } else {

                                playersList.clear();
                                for (com.hmproductions.bingo.models.Player currentPlayer : responseList) {
                                    playersList.add(new Player(currentPlayer.getName(), currentPlayer.getColor(), currentPlayer.getId(),
                                            currentPlayer.getReady()));
                                }
                            }

                            // To use variables in lambdas
                            int finalPositionOfInsertion = positionOfInsertion;
                            int finalPositionOfRemoval = positionOfRemoval;


                            if (finalPositionOfInsertion != -1) {
                                playersRecyclerAdapter.swapDataWithInsertion(playersList, finalPositionOfInsertion);
                            } else if (finalPositionOfRemoval != -1) {
                                playersRecyclerAdapter.swapDataWithDeletion(playersList, finalPositionOfRemoval);
                            } else {
                                playersRecyclerAdapter.swapData(playersList);
                            }

                            if (preferences.getBoolean(FIRST_TIME_JOINED_KEY, true)) {
                                int position = playersRecyclerAdapter.getReadyTapTargetPosition(currentPlayerId);
                                new Handler().postDelayed(() -> {
                                    View readyView = (playersRecyclerView.findViewHolderForLayoutPosition(position)).itemView.findViewById(R.id.ready_cardView);
                                    startTapTargetForView(readyView);
                                    preferences.edit().putBoolean(FIRST_TIME_JOINED_KEY, false).apply();
                                }, 750);
                            }

                            String text = playersList.size() + " / " + maxCount;
                            countTextView.setText(text);
                        } else if (value.getRoomEvent().getEventCode() == RoomEvent.EventCode.GAME_START && getView() != null) {

                            getLoaderManager().restartLoader(Constants.UNSUBSCRIBE_LOADER_ID, null, unsubscribeLoader);

                            Intent gameIntent = new Intent(getContext(), GameActivity.class);
                            gameIntent.putExtra(GameActivity.PLAYER_ID, currentPlayerId);
                            gameIntent.putExtra(GameActivity.ROOM_ID, currentRoomId);
                            gameIntent.putExtra(GameActivity.PREVIOUS_MESSAGE_COUNT_KEY, messageCount);
                            gameIntent.putExtra(GameActivity.ROOM_NAME_EXTRA_KEY, ((TextView) getView().findViewById(R.id.roomName_textView)).getText().toString());

                            if (getArguments() != null)
                                gameIntent.putExtra(GameActivity.TIME_LIMIT_ID, getArguments().getInt(TIME_LIMIT_BUNDLE_KEY));

                            gameIntent.putParcelableArrayListExtra(GameActivity.PLAYERS_LIST_ID, playersList);
                            startActivity(gameIntent);

                            fragmentChangeRequest.finishCurrentActivity();

                        }
                    });
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    private void startTapTargetForView(View view) {

        if (getActivity() != null) {
            TapTargetView.showFor(getActivity(), TapTarget.forView(view, "Are you ready ?", "Tap here to mark ready")
                            .targetRadius(100).cancelable(true).icon(getActivity().getDrawable(R.drawable.ready_icon)).drawShadow(true),
                    new TapTargetView.Listener() {
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);
                            getActivity().getSupportLoaderManager().restartLoader(Constants.READY_PLAYER_LOADER_ID, null, setPlayerReadyLoader);
                        }

                        @Override
                        public void onOuterCircleClick(TapTargetView view) {
                            super.onOuterCircleClick(view);
                            view.dismiss(false);
                        }
                    });
        }

    }

    private int deleteAndGetDeletedPosition(List<com.hmproductions.bingo.models.Player> responseList) {

        int returnPosition = -1;
        boolean exists = false;
        Player removalPlayer = null;

        for (Player currentDataPlayer : playersList) {

            exists = false;

            for (com.hmproductions.bingo.models.Player currentModelPlayer : responseList) {
                if (currentDataPlayer.getId() == currentModelPlayer.getId()) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                removalPlayer = currentDataPlayer;
                break;
            }
        }

        if (!exists) {
            returnPosition = playersList.indexOf(removalPlayer);
            playersList.remove(returnPosition);
        }

        return returnPosition;
    }

    private void showQuitProgressBar(boolean show) {
        leaveProgressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        leaveButton.setVisibility(show ? View.INVISIBLE : View.VISIBLE);

    }

    private LoaderManager.LoaderCallbacks<RemovePlayerResponse> removePlayerLoader = new LoaderManager.LoaderCallbacks<RemovePlayerResponse>() {

        @NonNull
        @Override
        public Loader<RemovePlayerResponse> onCreateLoader(int id, @Nullable Bundle args) {

            showQuitProgressBar(true);

            for (Player player : playersList) {
                if (player.getId() == currentPlayerId) {
                    return new RemovePlayerLoader(getContext(), actionServiceBlockingStub, player, currentRoomId);
                }
            }

            // If not found
            return new RemovePlayerLoader(getContext(), actionServiceBlockingStub, fakePlayer, currentRoomId);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<RemovePlayerResponse> loader, RemovePlayerResponse data) {

            showQuitProgressBar(false);

            if (data == null) {
                networkDownHandler.onNetworkDownError();
            } else {

                if (data.getStatusCode() == RemovePlayerResponse.StatusCode.OK) {
                    exitRoom();
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<RemovePlayerResponse> loader) {
            // Do nothing
        }
    };

    private LoaderManager.LoaderCallbacks<SetPlayerReadyResponse> setPlayerReadyLoader = new LoaderManager.LoaderCallbacks<SetPlayerReadyResponse>() {
        @NonNull
        @Override
        public Loader<SetPlayerReadyResponse> onCreateLoader(int id, @Nullable Bundle args) {

            showQuitProgressBar(true);

            if (args != null && !args.getBoolean(PLAYER_READY_BUNDLE_KEY))
                return new SetReadyLoader(getContext(), actionServiceBlockingStub, currentPlayerId, false, currentRoomId);

            return new SetReadyLoader(getContext(), actionServiceBlockingStub, currentPlayerId, !getPlayerReady(currentPlayerId), currentRoomId);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<SetPlayerReadyResponse> loader, SetPlayerReadyResponse data) {

            showQuitProgressBar(false);
            if (data == null) {
                networkDownHandler.onNetworkDownError();
            } else {
                if (data.getStatusCode() == SetPlayerReadyResponse.StatusCode.SERVER_ERROR) {
                    Toast.makeText(getContext(), data.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<SetPlayerReadyResponse> loader) {
            // Do nothing
        }
    };

    private LoaderManager.LoaderCallbacks<Unsubscribe.UnsubscribeResponse> unsubscribeLoader = new LoaderManager.LoaderCallbacks<Unsubscribe.UnsubscribeResponse>() {
        @NonNull
        @Override
        public Loader<Unsubscribe.UnsubscribeResponse> onCreateLoader(int id, @Nullable Bundle args) {

            return new UnsubscribeLoader(getContext(), actionServiceBlockingStub, currentPlayerId, currentRoomId);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Unsubscribe.UnsubscribeResponse> loader, Unsubscribe.UnsubscribeResponse data) {

            if (data == null) {
                networkDownHandler.onNetworkDownError();
            } else {
                if (data.getStatusCode() == Unsubscribe.UnsubscribeResponse.StatusCode.INTERNAL_SERVER_ERROR)
                    Toast.makeText(getContext(), data.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Unsubscribe.UnsubscribeResponse> loader) {
            // Do nothing
        }
    };

    private void exitRoom() {
        currentPlayerId = currentRoomId = -1;
        playersList.clear();
        playersRecyclerAdapter.swapData(null);
        fragmentChangeRequest.changeFragment(null, -1, null);
    }

    private void showChatRecyclerView(boolean show) {
        if (show) {
            startConversationTextView.setVisibility(View.INVISIBLE);
            chatRecyclerView.setVisibility(View.VISIBLE);
        } else {
            startConversationTextView.setVisibility(View.VISIBLE);
            chatRecyclerView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(firebaseAuthStateListener);
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(firebaseAuthStateListener);
        connectivityManager.unregisterNetworkCallback(networkCallback);

        if (currentPlayerId != -1) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(PLAYER_READY_BUNDLE_KEY, false);
            this.getLoaderManager().restartLoader(Constants.READY_PLAYER_LOADER_ID, bundle, setPlayerReadyLoader);
        }
    }
}
