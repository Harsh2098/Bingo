package com.hmproductions.bingo.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.bingo.R;
import com.hmproductions.bingo.data.Player;

import java.util.Arrays;
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

        // Starts animation for ready color change
        int fromColor = holder.readyView.getCardBackgroundColor().getDefaultColor();
        int toColor = currentPlayer.isReady() ? ContextCompat.getColor(context, R.color.player_ready) : ContextCompat.getColor(context, R.color.player_not_ready);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        colorAnimation.setDuration(450);

        colorAnimation.addUpdateListener(animator -> holder.readyView.setCardBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimation.start();

        int colorPosition = Arrays.asList(context.getResources().getStringArray(R.array.colorsName)).indexOf(currentPlayer.getColor());

        GradientDrawable backgroundDrawable = (GradientDrawable) holder.colorView.getBackground();
        backgroundDrawable.setStroke(2,
                Color.parseColor(context.getResources().getStringArray(R.array.colorsRim)[colorPosition]));
        backgroundDrawable.setColor(Color.parseColor(context.getResources().getStringArray(R.array.colorsHex)[colorPosition]));
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

    public void swapDataWithInsertion(List<Player> newList) {
        this.playersList = newList;
        notifyItemRangeInserted(0, newList.size());
    }

    public void swapDataWithDeletion(List<Player> newList) {
        this.playersList = newList;
        notifyItemRangeRemoved(0, newList.size());
    }

    public class PlayerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView playerNameTextView;
        View colorView;
        CardView readyView;

        PlayerViewHolder(View itemView) {
            super(itemView);

            playerNameTextView = itemView.findViewById(R.id.playerName_textView);
            colorView = itemView.findViewById(R.id.color_view);
            readyView = itemView.findViewById(R.id.ready_cardView);

            readyView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onPlayerClick(getAdapterPosition());
        }
    }

    public int getReadyTapTargetPosition(int playerId) {
        for (Player player : playersList) {
            if (player.getId() == playerId)
                return this.playersList.indexOf(player);
        }
        return -1;
    }
}
