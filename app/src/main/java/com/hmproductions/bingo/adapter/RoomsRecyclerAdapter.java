package com.hmproductions.bingo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmproductions.bingo.R;
import com.hmproductions.bingo.data.Room;

import java.util.ArrayList;
import java.util.Collections;

import static com.hmproductions.bingo.utils.Miscellaneous.generateColor;

public class RoomsRecyclerAdapter extends RecyclerView.Adapter<RoomsRecyclerAdapter.RoomViewHolder> {

    private static final int HEADER_TYPE = -435;
    private static final int NORMAL_TYPE = -107;

    private Context context;
    private final OnRoomItemClickListener listener;
    private ArrayList<Room> roomArrayList;

    public interface OnRoomItemClickListener {
        void onRoomClick(RoomViewHolder viewHolder, int position);
    }

    public RoomsRecyclerAdapter(Context context, ArrayList<Room> roomArrayList, OnRoomItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.roomArrayList = roomArrayList;

        createHeadersAndRefactorRooms();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RoomViewHolder(LayoutInflater.from(context).inflate(
                viewType == NORMAL_TYPE ? R.layout.room_list_item : R.layout.room_heading_item, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {

        Room currentRoom = roomArrayList.get(position);

        holder.roomNameTextView.setText(currentRoom.getName());

        if (holder.getItemViewType() == NORMAL_TYPE) {
            holder.countTextView.setText(String.valueOf(currentRoom.getCount()));
            holder.maxCountTextView.setText(String.valueOf(currentRoom.getMaxSize()));

            holder.roomIconTextView.setText(String.valueOf(currentRoom.getName().charAt(0)).toUpperCase());
            holder.roomIconTextView.setBackgroundResource(R.drawable.cell_circle_foreground);
            GradientDrawable backgroundDrawable = (GradientDrawable) holder.roomIconTextView.getBackground();

            String randomColor = "#" + generateColor();
            backgroundDrawable.setColor(Color.parseColor(randomColor));
        }
    }

    @Override
    public int getItemCount() {
        if (roomArrayList == null || roomArrayList.size() == 0) return 0;
        return roomArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return roomArrayList.get(position).getRoomId() == HEADER_TYPE ? HEADER_TYPE : NORMAL_TYPE;
    }

    public void swapData(ArrayList<Room> newList) {
        this.roomArrayList = newList;
        createHeadersAndRefactorRooms();
        notifyDataSetChanged();
    }

    private void createHeadersAndRefactorRooms() {
        if (roomArrayList != null && roomArrayList.size() > 0) {
            Collections.sort(roomArrayList, (roomA, roomB) -> Integer.compare(
                    roomB.getMaxSize() - roomB.getCount(), roomA.getMaxSize() - roomA.getCount()));

            if (roomArrayList.get(0).getMaxSize() != roomArrayList.get(0).getCount())
                roomArrayList.add(0, new Room(HEADER_TYPE, 0, 0, "Available Rooms"));

            int roomsFullPosition = -1;
            for (Room room : roomArrayList)
                if (room.getMaxSize() == room.getCount() && room.getRoomId() != HEADER_TYPE) {
                    roomsFullPosition = roomArrayList.indexOf(room);
                    break;
                }

            if (roomsFullPosition != -1) {
                roomArrayList.add(roomsFullPosition, new Room(HEADER_TYPE, 0, 0, "Full Rooms"));
            }
        }
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView roomIconTextView, roomNameTextView, countTextView, maxCountTextView;

        RoomViewHolder(View itemView, int viewType) {
            super(itemView);

            roomNameTextView = itemView.findViewById(R.id.roomName_textView);

            if (viewType == NORMAL_TYPE) {
                roomIconTextView = itemView.findViewById(R.id.roomIcon_textView);
                countTextView = itemView.findViewById(R.id.count_textView);
                maxCountTextView = itemView.findViewById(R.id.maxCount_textView);
                itemView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            listener.onRoomClick(this, getAdapterPosition());
        }
    }

    /* private void test() {
     *   //Mock rooms list for testing
     *   ArrayList<Room> mockList = new ArrayList<>();
     *   mockList.add(new Room(1, 3, 3, "hello1"));
     *   mockList.add(new Room(1, 2, 3, "hello2"));
     *   mockList.add(new Room(1, 0, 3, "hello4"));
     *   mockList.add(new Room(1, 4, 4, "hello5"));
     *   mockList.add(new Room(1, 1, 1, "hello7"));
     *   mockList.add(new Room(1, 4, 6, "hello8"));
     *   mockList.add(new Room(1, 8, 8, "hello9"));
     *   mockList = createHeadersAndRefactorRooms(mockList);
     *   for (Room room : mockList)
     *       Log.v(CLASSIC_TAG, room.getName() + " == " + room.getCount() + "/" + room.getMaxSize());
     }
        */
}