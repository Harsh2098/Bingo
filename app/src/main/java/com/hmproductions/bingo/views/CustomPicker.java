package com.hmproductions.bingo.views;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.hmproductions.bingo.R;

public class CustomPicker extends NumberPicker {

    public CustomPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    private void updateView(View view) {
        if(view instanceof EditText){
            ((EditText) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            ((EditText) view).setTypeface(ResourcesCompat.getFont(getContext(), R.font.default_bingo_font));
            ((EditText) view).setTextColor(getResources().getColor(R.color.primaryTextColor));
        }
    }

    // Prevents user from search in number picker for color. Prevents custom picker editable.
    @Override
    public void setDescendantFocusability(int focusability) {
        super.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }
}
