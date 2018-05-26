package com.hmproductions.bingo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.bingo.R;
import com.hmproductions.bingo.data.LeaderboardPlayer;

import java.util.ArrayList;
import java.util.Arrays;

public class LeaderboardRecyclerAdapter extends RecyclerView.Adapter<LeaderboardRecyclerAdapter.LeaderboardViewHolder> {

    private Context context;
    private ArrayList<LeaderboardPlayer> leaderboardArrayList;
    private OnLeaderboardPlayerClickListener listener;

    public interface OnLeaderboardPlayerClickListener {
        void onLeaderboardPlayerClick(int position);
    }

    public LeaderboardRecyclerAdapter(@NonNull Context context, @NonNull ArrayList<LeaderboardPlayer> leaderboardArrayList, @Nullable OnLeaderboardPlayerClickListener listener) {
        this.context = context;
        this.leaderboardArrayList = leaderboardArrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View customView = LayoutInflater.from(context).inflate(R.layout.leaderboard_list_item, parent, false);
        return new LeaderboardViewHolder(customView);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {

        LeaderboardPlayer currentPlayer = leaderboardArrayList.get(position);

        holder.nameTextView.setText(currentPlayer.getName());
        holder.winCountTextView.setText(String.valueOf(currentPlayer.getWinCount()));

        int colorPosition = Arrays.asList(context.getResources().getStringArray(R.array.colorsName)).indexOf(currentPlayer.getColor());

        GradientDrawable backgroundDrawable = (GradientDrawable) holder.colorView.getBackground();
        backgroundDrawable.setStroke(2,
                Color.parseColor(context.getResources().getStringArray(R.array.colorsRim)[colorPosition]));
        backgroundDrawable.setColor(Color.parseColor(context.getResources().getStringArray(R.array.colorsHex)[colorPosition]));
    }

    public void swapData(ArrayList<LeaderboardPlayer> newList) {
        leaderboardArrayList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (leaderboardArrayList == null || leaderboardArrayList.size() == 0) return 0;
        return leaderboardArrayList.size();
    }

    class LeaderboardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView winCountTextView, nameTextView;
        View colorView;

        LeaderboardViewHolder(View itemView) {
            super(itemView);
            winCountTextView = itemView.findViewById(R.id.winCount_textView);
            nameTextView = itemView.findViewById(R.id.playerName_textView);

            colorView = itemView.findViewById(R.id.color_view);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onLeaderboardPlayerClick(getAdapterPosition());
        }
    }
}
