package com.hmproductions.bingo.animations;

import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.hmproductions.bingo.views.CircleView;

public class GridCircleAnimation extends Animation {

    private CircleView circle;

    private float newAngle;

    public GridCircleAnimation(CircleView circle, int newAngle) {
        this.newAngle = newAngle;
        this.circle = circle;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        float angle = newAngle * interpolatedTime;
        circle.setAngle(angle);
        circle.requestLayout();
    }
}
