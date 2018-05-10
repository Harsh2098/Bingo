package com.hmproductions.bingo.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hmproductions.bingo.R;
import com.hmproductions.bingo.data.GridCell;

import java.util.ArrayList;

/*
 * Created by harsh on 7/30/17.
 *
 * GameGridRecyclerAdapter is used to load data from ArrayList<GridCell> into the recycler view.
 */

public class GameGridRecyclerAdapter extends RecyclerView.Adapter<GameGridRecyclerAdapter.GridViewHolder> {
    
    private ArrayList<GridCell> gameGridCellList;
    private Context context;
    private int GRID_SIZE;
    private GridCellClickListener mClickListener;
    private RelativeLayout.LayoutParams layoutParams;

    public interface GridCellClickListener {
        void onGridCellClick(int position, View view);
    }

    public GameGridRecyclerAdapter(Context context, int size, ArrayList<GridCell> data, GridCellClickListener listener) {
        GRID_SIZE = size;
        this.context = context;
        gameGridCellList = data;
        mClickListener = listener;

        /* Setting layoutParams to match the width of the screen */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) this.context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        layoutParams = new RelativeLayout.LayoutParams(displayMetrics.widthPixels/GRID_SIZE,displayMetrics.widthPixels/GRID_SIZE);
    }

    @NonNull
    @Override
    public GameGridRecyclerAdapter.GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View myView = LayoutInflater.from(context).inflate(R.layout.grid_item,parent, false);
        myView.setLayoutParams(layoutParams);

        return new GridViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull GameGridRecyclerAdapter.GridViewHolder holder, int position) {

        holder.value_textView.setText(String.valueOf(gameGridCellList.get(position).getValue()));

        holder.value_textView.setTextColor(Color.parseColor("#000000"));
        holder.value_textView.setTypeface(holder.value_textView.getTypeface(), Typeface.NORMAL);

        if(gameGridCellList.get(position).getIsClicked()) {
            holder.value_textView.setTextColor(Color.parseColor("#FF0000"));
            holder.value_textView.setTypeface(holder.value_textView.getTypeface(), Typeface.BOLD);
        }
    }

    @Override
    public int getItemCount() {
        return GRID_SIZE * GRID_SIZE;
    }

    public void swapData(ArrayList<GridCell> data) {
        gameGridCellList = data;
        notifyDataSetChanged();
    }

    class GridViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView value_textView;
        
        GridViewHolder(View itemView) {
            super(itemView);

            value_textView = itemView.findViewById(R.id.value_textView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onGridCellClick(getAdapterPosition(), view);
        }
    }
}
