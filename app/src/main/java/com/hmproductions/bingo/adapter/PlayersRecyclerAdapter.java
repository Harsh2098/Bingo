package com.hmproductions.bingo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.bingo.R;
import com.hmproductions.bingo.data.Player;

import java.util.List;

public class PlayersRecyclerAdapter extends RecyclerView.Adapter<PlayersRecyclerAdapter.PlayerViewHolder> {

    private List<Player> playersList;
    private Context context;
    private OnPlayerClickListener listener;

    public interface OnPlayerClickListener {
        void onPlayerClick(int position);
    }

    public PlayersRecyclerAdapter(List<Player> playersList, Context context, OnPlayerClickListener listener) {
        this.playersList = playersList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(context).inflate(R.layout.player_list_item, parent, false);
        return new PlayerViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {

        Player currentPlayer = playersList.get(position);

        holder.playerNameTextView.setText(currentPlayer.getName());
        holder.playerColorTextView.setText(currentPlayer.getColor());
        holder.readyView.setBackgroundColor(Color.parseColor(currentPlayer.isReady()?"#00FF00":"#FF0000"));
    }

    @Override
    public int getItemCount() {
        if (playersList == null || playersList.size() == 0) return 0;
        return playersList.size();
    }

    public void swapData(List<Player> newList) {
        this.playersList = newList;
        notifyDataSetChanged();
    }

    class PlayerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView playerNameTextView, playerColorTextView;
        View readyView;

        PlayerViewHolder(View itemView) {
            super(itemView);

            playerNameTextView = itemView.findViewById(R.id.playerName_textView);
            playerColorTextView = itemView.findViewById(R.id.playerColor_textView);
            readyView = itemView.findViewById(R.id.ready_view);

            readyView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onPlayerClick(getAdapterPosition());
        }
    }
}
