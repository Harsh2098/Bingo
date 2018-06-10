package com.hmproductions.bingo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.bingo.R;
import com.hmproductions.bingo.data.Room;

import java.util.ArrayList;
import java.util.List;

public class RoomsRecyclerAdapter extends RecyclerView.Adapter<RoomsRecyclerAdapter.RoomViewHolder> {

    private Context context;
    private final OnRoomItemClickListener listener;
    private ArrayList<Room> roomsList;

    public interface OnRoomItemClickListener {
        void onRoomClick(int position);
    }

    public RoomsRecyclerAdapter(Context context, ArrayList<Room> roomArrayList, OnRoomItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.roomsList = roomArrayList;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RoomViewHolder(LayoutInflater.from(context).inflate(R.layout.room_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {

        Room currentRoom = roomsList.get(position);

        holder.roomNameTextView.setText(currentRoom.getName());
        holder.countTextView.setText(String.valueOf(currentRoom.getCount()));
        holder.maxCountTextView.setText(String.valueOf(currentRoom.getMaxSize()));
    }

    @Override
    public int getItemCount() {
        if (roomsList == null || roomsList.size() == 0) return 0;
        return roomsList.size();
    }

    public void swapData(ArrayList<Room> newList) {
        this.roomsList = newList;
        notifyDataSetChanged();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView roomNameTextView, countTextView, maxCountTextView;

        RoomViewHolder(View itemView) {
            super(itemView);

            roomNameTextView = itemView.findViewById(R.id.roomName_textView);
            countTextView = itemView.findViewById(R.id.count_textView);
            maxCountTextView = itemView.findViewById(R.id.maxCount_textView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onRoomClick(getAdapterPosition());
        }
    }
}
