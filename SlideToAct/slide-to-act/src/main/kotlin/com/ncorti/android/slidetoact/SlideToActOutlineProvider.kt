package com.ncorti.android.slidetoact

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

/**
 * Outline provider for the SlideToActView.
 * This outline will suppress the shadow (till the moment when Android will support
 * updatable Outlines).
 */
class SlideToActOutlineProvider(borderRadius: Int, actualAreaWidth: Int, areaWidth: Int, areaHeight: Int) : ViewOutlineProvider() {
    val left = actualAreaWidth
    val top = 0
    val right = areaWidth
    val bottom = areaHeight
    val radius = borderRadius

    override fun getOutline(view: View?, outline: Outline?) {
        if (view == null || outline == null)
            return
        outline.setRoundRect(left, top, right, bottom, radius.toFloat())
    }
}