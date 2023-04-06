package cx.aphex.perplexity

import BlinkingCaretSpan
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat

object Animations {
    fun Context.createCaretAnimator(blinkingCaretSpan: BlinkingCaretSpan): ObjectAnimator {
        return ObjectAnimator.ofArgb(
            blinkingCaretSpan,
            "color",
            ContextCompat.getColor(this, R.color.white),
            ContextCompat.getColor(this, android.R.color.transparent)
        ).apply {
            duration = 500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }

    fun createPulsingAnimation(target: ImageView): AnimatorSet {
        val scaleXAnimator = ObjectAnimator.ofFloat(target, "scaleX", 1f, 1.2f, 1f).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }
        val scaleYAnimator = ObjectAnimator.ofFloat(target, "scaleY", 1f, 1.2f, 1f).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }
        val alphaAnimator = ObjectAnimator.ofFloat(target, "alpha", 1f, 0.6f, 1f).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }

        return AnimatorSet().apply {
            playTogether(scaleXAnimator, scaleYAnimator, alphaAnimator)
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

}