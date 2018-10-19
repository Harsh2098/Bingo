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
import com.hmproductions.bingo.data.Message;

import java.util.ArrayList;
import java.util.Arrays;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.MessageViewHolder> {

    private ArrayList<Message> messageArrayList;
    private Context context;

    public ChatRecyclerAdapter(Context context, ArrayList<Message> messageArrayList) {
        this.messageArrayList = messageArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message currentMessage = messageArrayList.get(position);

        holder.authorTextView.setText(currentMessage.getAuthor());
        holder.timeStampTextView.setText(currentMessage.getTimeStamp());
        holder.contentTextView.setText(currentMessage.getContent());

        int colorPosition = Arrays.asList(context.getResources().getStringArray(R.array.colorsName)).indexOf(currentMessage.getColor());
        holder.authorTextView.setTextColor(Color.parseColor(context.getResources().getStringArray(R.array.colorsHex)[colorPosition]));
    }

    public void addMessage(Message message) {
        if (messageArrayList == null) messageArrayList = new ArrayList<>();

        messageArrayList.add(message);
        notifyItemInserted(messageArrayList.size()-1);
    }

    @Override
    public int getItemCount() {
        if (messageArrayList == null || messageArrayList.size() == 0) return 0;
        return messageArrayList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView authorTextView, contentTextView, timeStampTextView;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            timeStampTextView = itemView.findViewById(R.id.timeStampTextView);
        }
    }
}
