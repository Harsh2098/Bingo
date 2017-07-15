package com.hmproductions.bingo;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harsh Mahajan on 4/7/2017.
 */

public class GameGridAdapter extends ArrayAdapter<GridCell> {

    private Context mContext;

    public GameGridAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<GridCell> data) {
        super(context, resource, data);
        mContext = context;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null)
            convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);

        TextView value_textView = (TextView)convertView.findViewById(R.id.value_textView);
        value_textView.setText(String.valueOf(getItem(position).getValue()));

        return convertView;
    }
}
