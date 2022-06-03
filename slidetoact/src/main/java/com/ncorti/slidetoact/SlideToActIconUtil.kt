package com.ncorti.slidetoact

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Build.VERSION_CODES.N
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

internal object SlideToActIconUtil {

    @SuppressLint("UseCompatLoadingForDrawables")
    internal fun loadIconCompat(context: Context, value: Int): Drawable {
        // Due to bug in the AVD implementation in the support library, we use it only for API < 21
        return if (SDK_INT >= LOLLIPOP) {
            context.resources.getDrawable(value, context.theme)
        } else {
            return AnimatedVectorDrawableCompat.create(context, value)
                ?: ContextCompat.getDrawable(context, value)!!
        }
    }

    internal fun tintIconCompat(icon: Drawable, color: Int) {
        // Tinting the tick with the proper implementation method
        when {
            SDK_INT >= LOLLIPOP -> icon.setTint(color)
            icon is AnimatedVectorDrawableCompat -> icon.setTint(color)
            else -> DrawableCompat.setTint(icon, color)
        }
    }

    /**
     * Internal method to start the Icon AVD animation, with the proper library based on API level.
     */
    internal fun startIconAnimation(icon: Drawable) {
        when {
            SDK_INT >= LOLLIPOP && icon is AnimatedVectorDrawable -> icon.start()
            icon is AnimatedVectorDrawableCompat -> icon.start()
            else -> {
                // Do nothing as the icon can't be animated
            }
        }
    }

    /**
     * Internal method to stop the Icon AVD animation, with the proper library based on API level.
     */
    internal fun stopIconAnimation(icon: Drawable) {
        when {
            SDK_INT >= LOLLIPOP && icon is AnimatedVectorDrawable -> icon.stop()
            icon is AnimatedVectorDrawableCompat -> icon.stop()
            else -> {
                // Do nothing as the icon can't be animated
            }
        }
    }

    /**
     * Creates a [ValueAnimator] to animate the complete icon. Uses the [fallbackToFadeAnimation]
     * to decide if the icon should be animated with a Fade or with using [AnimatedVectorDrawable].
     */
    fun createIconAnimator(
        view: SlideToActView,
        icon: Drawable,
        listener: ValueAnimator.AnimatorUpdateListener
    ): ValueAnimator {
        if (fallbackToFadeAnimation(icon)) {
            // Fallback not using AVD.
            val tickAnimator = ValueAnimator.ofInt(0, 255)
            tickAnimator.addUpdateListener(listener)
            tickAnimator.addUpdateListener {
                icon.alpha = it.animatedValue as Int
                view.invalidate()
            }
            return tickAnimator
        } else {
            // Used AVD Animation.
            val tickAnimator = ValueAnimator.ofInt(0)
            var startedOnce = false
            tickAnimator.addUpdateListener(listener)
            tickAnimator.addUpdateListener {
                if (!startedOnce) {
                    startIconAnimation(icon)
                    view.invalidate()
                    startedOnce = true
                }
            }
            return tickAnimator
        }
    }

    /**
     * Logic to decide if we should do a Fade or use the [AnimatedVectorDrawable] animation.
     */
    private fun fallbackToFadeAnimation(icon: Drawable) = when {
        // We don't use AVD at all for <= N.
        SDK_INT <= N -> true
        SDK_INT >= LOLLIPOP && icon !is AnimatedVectorDrawable -> true
        SDK_INT < LOLLIPOP && icon !is AnimatedVectorDrawableCompat -> true
        else -> false
    }
}
