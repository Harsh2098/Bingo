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

public class PeersListRecyclerAdapter extends RecyclerView.Adapter<PeersListRecyclerAdapter.PeerViewHolder> {

    private Context context;
    private List<WifiP2pDevice> peerList = new ArrayList<>();
    private PeerItemClickListener listener;

    public interface PeerItemClickListener {
        void onPeerItemClick(int position);
    }

    public PeersListRecyclerAdapter(Context context, List<WifiP2pDevice> peerList, PeerItemClickListener listener) {
        this.context = context;
        this.peerList = peerList;
        this.listener = listener;
    }

    @Override
    public PeerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(context).inflate(R.layout.peer_item_list, parent, false);
        return new PeerViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(PeerViewHolder holder, int position) {

        holder.deviceNameTextView.setText(peerList.get(position).deviceName);
        holder.deviceAddressTextView.setText(peerList.get(position).deviceAddress);
    }

    @Override
    public int getItemCount() {
        if(peerList == null || peerList.size()==0) return 0;
        return peerList.size();
    }

    public void swapData(List<WifiP2pDevice> list) {
        peerList = list;
        notifyDataSetChanged();
    }

    class PeerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.deviceAddress_textView)
        TextView deviceAddressTextView;

        @BindView(R.id.deviceName_textView)
        TextView deviceNameTextView;

        PeerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onPeerItemClick(getAdapterPosition());
        }
    }
}
