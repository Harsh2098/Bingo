package com.hmproductions.bingo.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hmproductions.bingo.R;
import com.hmproductions.bingo.animations.GridCircleAnimation;
import com.hmproductions.bingo.data.GridCell;
import com.hmproductions.bingo.views.CircleView;

import java.util.ArrayList;
import java.util.Arrays;

import static com.hmproductions.bingo.utils.Constants.CLASSIC_TAG;
import static com.hmproductions.bingo.utils.Constants.GRID_SCALING_FACTOR;

public class GameGridRecyclerAdapter extends RecyclerView.Adapter<GameGridRecyclerAdapter.GridViewHolder> {

    private static final String GRID_LOG_TAG = GameGridRecyclerAdapter.class.getSimpleName() + CLASSIC_TAG;

    private ArrayList<GridCell> gameGridCellList;
    private Context context;
    private int gridSize;
    private GridCellClickListener mClickListener;
    private RelativeLayout.LayoutParams layoutParams;

    public interface GridCellClickListener {
        void onGridCellClick(int position);
    }

    public GameGridRecyclerAdapter(Context context, int size, ArrayList<GridCell> data, GridCellClickListener listener) {
        gridSize = size;
        this.context = context;
        gameGridCellList = data;
        mClickListener = listener;

        /* Setting layoutParams to match the width of the screen */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) this.context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        layoutParams = new RelativeLayout.LayoutParams(
                (int) (displayMetrics.widthPixels / gridSize * GRID_SCALING_FACTOR),
                (int) (displayMetrics.widthPixels / gridSize * GRID_SCALING_FACTOR));

        Log.d(GRID_LOG_TAG, "Setting cell size to " + displayMetrics.widthPixels / gridSize);
    }

    @NonNull
    @Override
    public GameGridRecyclerAdapter.GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View myView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        myView.setLayoutParams(layoutParams);

        return new GridViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull GameGridRecyclerAdapter.GridViewHolder holder, int position) {

        GridCell currentGridCell = gameGridCellList.get(position);

        holder.value_textView.setText(String.valueOf(currentGridCell.getValue()));

        if (gameGridCellList.get(position).isClicked()) {

            int colorPosition = Arrays.asList(context.getResources().getStringArray(R.array.colorsName)).indexOf(currentGridCell.getColor());

            holder.value_textView.setTypeface(holder.value_textView.getTypeface(), Typeface.BOLD);
            holder.value_textView.setTextColor(
                    Color.parseColor(context.getResources().getStringArray(R.array.colorsRim)[colorPosition])
            );

            holder.circleView.setPaintColorId(Color.parseColor(context.getResources().getStringArray(R.array.colorsHex)[colorPosition]));

            GridCircleAnimation animation = new GridCircleAnimation(holder.circleView, 360);
            animation.setDuration(450);
            holder.circleView.startAnimation(animation);

        } else {

            holder.value_textView.setTypeface(holder.value_textView.getTypeface(), Typeface.NORMAL);
            holder.value_textView.setTextColor(context.getResources().getColor(R.color.primaryTextColor));

            holder.value_textView.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return gridSize * gridSize;
    }

    public void swapData(ArrayList<GridCell> data, int position) {
        gameGridCellList = data;
        notifyItemChanged(position);
    }

    class GridViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView value_textView;
        CircleView circleView;

        GridViewHolder(View itemView) {
            super(itemView);

            value_textView = itemView.findViewById(R.id.value_textView);
            circleView = itemView.findViewById(R.id.circleView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onGridCellClick(Integer.parseInt(value_textView.getText().toString()));
        }
    }
}
