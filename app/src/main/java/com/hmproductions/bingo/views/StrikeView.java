package com.hmproductions.bingo.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.hmproductions.bingo.R;

public class StrikeView extends View {

    private Paint paint;
    private float width, height, currentWidth = 0, currentHeight = 0;

    public StrikeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialisePaint();
    }

    private void initialisePaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.player_not_ready));
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        this.width = width;
        this.height = height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, 0, currentWidth, currentHeight, paint);
    }

    public void setDimensions(float time) {
        currentWidth = width * time;
        currentHeight = height * time;
    }

    public boolean finishedAnimation() {
        return currentWidth == width;
    }
}
