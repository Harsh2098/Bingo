package com.hmproductions.bingo.utils;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.bingo.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConnectedListRecyclerAdapter extends RecyclerView.Adapter<ConnectedListRecyclerAdapter.ConnectedPeerViewHolder> {

    private Context context;
    private List<WifiP2pDevice> peerList = new ArrayList<>();
    private ConnectedPeerItemClickListener listener;
    private boolean isHost = false;

    public interface ConnectedPeerItemClickListener {
        void onConnectedPeerItemClick(int position);
    }

    public ConnectedListRecyclerAdapter(Context context, List<WifiP2pDevice> peerList, ConnectedPeerItemClickListener listener) {
        this.context = context;
        this.peerList = peerList;
        this.listener = listener;
    }

    @Override
    public ConnectedPeerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(context).inflate(R.layout.peer_item_list, parent, false);
        return new ConnectedPeerViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(ConnectedPeerViewHolder holder, int position) {

        holder.deviceNameTextView.setText(peerList.get(position).deviceName);

        if(isHost)
            holder.deviceAddressTextView.setText(context.getString(R.string.host));
        else
            holder.deviceAddressTextView.setText(context.getString(R.string.client));
    }

    @Override
    public int getItemCount() {
        if(peerList == null || peerList.size()==0) return 0;
        return peerList.size();
    }

    public void swapData(List<WifiP2pDevice> list, boolean isHost) {
        peerList = list;
        this.isHost = isHost;
        notifyDataSetChanged();
    }

    class ConnectedPeerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.deviceAddress_textView)
        TextView deviceAddressTextView;

        @BindView(R.id.deviceName_textView)
        TextView deviceNameTextView;

        ConnectedPeerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onConnectedPeerItemClick(getAdapterPosition());
        }
    }
}
