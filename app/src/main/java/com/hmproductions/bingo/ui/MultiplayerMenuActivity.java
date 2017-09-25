package com.hmproductions.bingo.ui;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.Toast;

import com.hmproductions.bingo.R;
import com.hmproductions.bingo.utils.ConnectedListRecyclerAdapter;
import com.hmproductions.bingo.utils.PeersListRecyclerAdapter;
import com.hmproductions.bingo.utils.WifiDirectBroadcastReceiver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MultiplayerMenuActivity extends AppCompatActivity implements
        PeersListRecyclerAdapter.PeerItemClickListener,
        ConnectedListRecyclerAdapter.ConnectedPeerItemClickListener {

    private static final String LOG_TAG = ":::";

    private List<WifiP2pDevice> availablePeers = new ArrayList<>();
    private List<WifiP2pDevice> connectedPeers = new ArrayList<>();

    private PeersListRecyclerAdapter peerListAdapter;
    private ConnectedListRecyclerAdapter connectedListAdapter;

    private boolean isWifiP2pEnabled = false;

    @BindView(R.id.peers_recyclerView)
    RecyclerView peersRecyclerView;

    @BindView(R.id.game_room_recyclerView)
    RecyclerView gameRoomRecyclerView;

    Button findPeersButton, playButton;

    WifiP2pManager manager;
    Channel channel;
    WifiDirectBroadcastReceiver receiver = null;
    WifiP2pManager.PeerListListener peerListener;

    IntentFilter intentFilter = new IntentFilter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_menu);
        ButterKnife.bind(this);

        peerListAdapter = new PeersListRecyclerAdapter(this, null, this);
        connectedListAdapter = new ConnectedListRecyclerAdapter(this, null, this);

        findPeersButton = (Button) findViewById(R.id.find_peers_button);
        GradientDrawable findPeersGradientDrawable = (GradientDrawable) findPeersButton.getBackground();
        findPeersGradientDrawable.setColor(Color.parseColor("#00E5B5"));

        playButton = (Button) findViewById(R.id.play_button);
        GradientDrawable playGradientDrawable = (GradientDrawable) playButton.getBackground();
        playGradientDrawable.setColor(Color.parseColor("#009624"));
        playButton.setEnabled(false);

        peersRecyclerView.setHasFixedSize(false);
        peersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        peersRecyclerView.setAdapter(peerListAdapter);

        gameRoomRecyclerView.setHasFixedSize(false);
        gameRoomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        gameRoomRecyclerView.setAdapter(connectedListAdapter);

        AddFilterToIntentFilter();
        InitialisePeerListListener();

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    private void AddFilterToIntentFilter() {

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void InitialisePeerListListener() {

        peerListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {

                Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();

                if (!refreshedPeers.equals(availablePeers)) {
                    availablePeers.clear();
                    availablePeers.addAll(refreshedPeers);
                    peerListAdapter.swapData(availablePeers);
                }

                if (availablePeers.size() == 0) {
                    Toast.makeText(MultiplayerMenuActivity.this, "No devices found", Toast.LENGTH_SHORT).show();
                    connectedPeers.clear();
                    connectedListAdapter.swapData(null, false);
                }
            }
        };
    }

    @OnClick(R.id.find_peers_button)
    void onFindPeersButtonClick() {

        if (isWifiP2pEnabled) {
            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MultiplayerMenuActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reasonCode) {
                    Toast.makeText(MultiplayerMenuActivity.this, "Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(MultiplayerMenuActivity.this, "Enable Wifi-Direct", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPeerItemClick(int position) {

        WifiP2pDevice device = availablePeers.get(position);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MultiplayerMenuActivity.this, "Connect failed. Retry." + String.valueOf(reason), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectedPeerItemClick(final int position) {

        Toast.makeText(MultiplayerMenuActivity.this, String.valueOf(connectedPeers.get(position).isGroupOwner()), Toast.LENGTH_SHORT).show();
    }

    public void updatePlayersList() {

        manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {

                if (group != null) {

                    if (group.isGroupOwner()) {
                        Collection<WifiP2pDevice> list = group.getClientList();
                        connectedPeers.clear();
                        for (WifiP2pDevice currentDevice : list) {
                            connectedPeers.add(currentDevice);
                        }
                        connectedListAdapter.swapData(connectedPeers, false);

                        if (list.size() > 0)
                            playButton.setEnabled(true);
                    } else {
                        connectedPeers.clear();
                        connectedPeers.add(group.getOwner());
                        connectedListAdapter.swapData(connectedPeers, true);
                        playButton.setEnabled(false);
                    }
                }
            }
        });
    }

    @OnClick(R.id.play_button)
    void onPlayButtonClick() {
        Toast.makeText(this, "Starting the game", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WifiDirectBroadcastReceiver(manager, channel, this, peerListener);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        connectedPeers.clear();
        unregisterReceiver(receiver);
    }
}
