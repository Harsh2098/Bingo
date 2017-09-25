package com.hmproductions.bingo.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import com.hmproductions.bingo.ui.MainActivity;
import com.hmproductions.bingo.ui.MultiplayerMenuActivity;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private MultiplayerMenuActivity activity;
    private WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    private WifiP2pManager.PeerListListener peerListListener;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MultiplayerMenuActivity activity, WifiP2pManager.PeerListListener listener) {
        this.activity = activity;
        this.manager = manager;
        this.channel = channel;
        this.peerListListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                activity.setIsWifiP2pEnabled(true);
            } else {
                activity.setIsWifiP2pEnabled(false);
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            if(manager != null) {
                manager.requestPeers(channel, peerListListener);
            }
            Toast.makeText(context, "P2P Peers changed", Toast.LENGTH_SHORT).show();

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            Toast.makeText(context, "P2P Connection changed", Toast.LENGTH_SHORT).show();
            activity.updatePlayersList();

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            if (manager == null || intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)==null) {
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // We are connected with the other device, request connection
                // info to find group owner IP

                manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        Log.v("::::", info.toString());
                    }
                });
            }
        }
    }
}
