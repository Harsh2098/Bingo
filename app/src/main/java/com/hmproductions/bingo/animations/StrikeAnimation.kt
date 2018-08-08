package com.hmproductions.bingo.animations

import android.view.animation.Animation
import android.view.animation.Transformation
import com.hmproductions.bingo.views.StrikeView

class StrikeAnimation(private val slash: StrikeView) : Animation() {
    override fun applyTransformation(interpolatedTime: Float, transformation: Transformation) {
        slash.setDimensions(interpolatedTime)
        slash.requestLayout()
    }
}
