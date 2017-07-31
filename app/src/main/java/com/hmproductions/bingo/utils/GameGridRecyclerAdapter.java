package com.hmproductions.bingo.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hmproductions.bingo.data.GridCell;
import com.hmproductions.bingo.R;

import java.util.ArrayList;

/*
 * Created by harsh on 7/30/17.
 *
 * GameGridRecyclerAdapter is used to load data from ArrayList<GridCell> into the recycler view.
 */

public class GameGridRecyclerAdapter extends RecyclerView.Adapter<GameGridRecyclerAdapter.GridViewHolder> {
    
    private ArrayList<GridCell> mData = new ArrayList<>();
    private Context mContext;
    private int GRID_SIZE;
    private GridCellClickListener mClickListener;
    private RelativeLayout.LayoutParams layoutParams;

    public interface GridCellClickListener {
        void onGridCellClick(int position, View view);
    }

    public GameGridRecyclerAdapter(Context context, int size, ArrayList<GridCell> data, GridCellClickListener listener) {
        GRID_SIZE = size;
        mContext = context;
        mData = data;
        mClickListener = listener;

        /* Setting layoutParams to match the width of the screen */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        layoutParams = new RelativeLayout.LayoutParams(displayMetrics.widthPixels/GRID_SIZE,displayMetrics.widthPixels/GRID_SIZE);
    }

    @Override
    public GameGridRecyclerAdapter.GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View myView = LayoutInflater.from(mContext).inflate(R.layout.grid_item,parent, false);
        myView.setLayoutParams(layoutParams);

        return new GridViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(GameGridRecyclerAdapter.GridViewHolder holder, int position) {

        holder.value_textView.setText(String.valueOf(mData.get(position).getValue()));
    }

    @Override
    public int getItemCount() {
        return GRID_SIZE * GRID_SIZE;
    }

    public void swapData(ArrayList<GridCell> data) {
        mData = data;
        notifyDataSetChanged();
    }

    class GridViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        
        TextView value_textView;
        
        GridViewHolder(View itemView) {
            super(itemView);
            
            value_textView = (TextView)itemView.findViewById(R.id.value_textView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mClickListener.onGridCellClick(getAdapterPosition(), view);
        }
    }
}
