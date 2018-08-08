package com.hmproductions.bingo.animations

import android.view.animation.Animation
import android.view.animation.Transformation
import com.hmproductions.bingo.views.CircleView

class GridCircleAnimation(private val circle: CircleView, newAngle: Int) : Animation() {

    private val newAngle: Float = newAngle.toFloat()

    override fun applyTransformation(interpolatedTime: Float, transformation: Transformation) {
        val angle = newAngle * interpolatedTime
        circle.setAngle(angle)
        circle.requestLayout()
    }
}
