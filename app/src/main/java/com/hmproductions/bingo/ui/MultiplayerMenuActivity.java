package com.hmproductions.bingo.ui;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hmproductions.bingo.R;
import com.hmproductions.bingo.utils.PeersListRecyclerAdapter;
import com.hmproductions.bingo.utils.WifiDirectBroadcastReceiver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MultiplayerMenuActivity extends AppCompatActivity implements PeersListRecyclerAdapter.PeerItemClickListener {

    private static final String LOG_TAG = MultiplayerMenuActivity.class.getSimpleName();
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private PeersListRecyclerAdapter adapter;
    private boolean isWifiP2pEnabled = false;

    @BindView(R.id.peers_recyclerView)
    RecyclerView peersRecyclerView;

    Button findPeersButton;

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

        adapter = new PeersListRecyclerAdapter(this, null, this);

        findPeersButton = (Button)findViewById(R.id.find_peers_button);
        GradientDrawable undoGradientDrawable = (GradientDrawable) findPeersButton.getBackground();
        undoGradientDrawable.setColor(Color.parseColor("#009624"));

        peersRecyclerView.setHasFixedSize(false);
        peersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        peersRecyclerView.setAdapter(adapter);

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

                if (!refreshedPeers.equals(peers)) {
                    peers.clear();
                    peers.addAll(refreshedPeers);
                    adapter.swapData(peers);
                }

                if (peers.size() == 0) {
                    Toast.makeText(MultiplayerMenuActivity.this, "No devices found", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @OnClick(R.id.find_peers_button)
    void onFindPeersButtonClick() {

        if(isWifiP2pEnabled) {
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
            Toast.makeText(MultiplayerMenuActivity.this, "Enable Wifi-Direct" , Toast.LENGTH_SHORT).show();
        }
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
        unregisterReceiver(receiver);
    }

    @Override
    public void onPeerItemClick(int position) {

        WifiP2pDevice device = peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MultiplayerMenuActivity.this, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
