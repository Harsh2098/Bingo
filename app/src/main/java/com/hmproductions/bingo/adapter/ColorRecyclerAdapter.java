package com.hmproductions.bingo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hmproductions.bingo.R;

public class ColorRecyclerAdapter extends RecyclerView.Adapter<ColorRecyclerAdapter.ColorViewHolder> {

    private String[] colorsHex;
    private Context context;
    private int selectedPosition;
    private OnColorSelected listener;

    public interface OnColorSelected {
        void onColorClick(int position);
    }

    public ColorRecyclerAdapter(Context context, String[] colorsHex, OnColorSelected listener) {
        this.colorsHex = colorsHex;
        this.context = context;
        this.selectedPosition = 0;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ColorViewHolder(LayoutInflater.from(context).inflate(R.layout.color_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        holder.colorSelectedImageView.setVisibility(selectedPosition == position ? View.VISIBLE : View.GONE);
        holder.colorImageView.setColorFilter(Color.parseColor(colorsHex[position]), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public int getItemCount() {
        if (colorsHex == null || colorsHex.length == 0) return 0;
        return colorsHex.length;
    }

    public void setSelected(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    class ColorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView colorImageView, colorSelectedImageView;

        ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorImageView = itemView.findViewById(R.id.color_imageView);
            colorSelectedImageView = itemView.findViewById(R.id.selected_imageView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onColorClick(getAdapterPosition());
        }
    }
}
