package com.hmproductions.bingo.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.hmproductions.bingo.R;

import static com.hmproductions.bingo.utils.Constants.CELL_SCALING_FACTOR;

public class CircleView extends View {

    private static final int START_ANGLE_POINT = 270;

    private Paint paint;
    private RectF rectF;

    private float sweepAngle;

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialisePaint();
        sweepAngle = 0;
    }

    private void initialisePaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);

        // Setting relative positions of left, right, top, bottom for rectangle; relative to the entire view
        rectF = new RectF(width * (1 - CELL_SCALING_FACTOR), height * (1 - CELL_SCALING_FACTOR), width * CELL_SCALING_FACTOR, height * CELL_SCALING_FACTOR);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(rectF, START_ANGLE_POINT, sweepAngle, false, paint);
    }

    public void setPaintColorId(int colorId) {
        this.paint.setColor(colorId);
    }

    public void setAngle(float angle) {
        this.sweepAngle = angle;
    }
}
