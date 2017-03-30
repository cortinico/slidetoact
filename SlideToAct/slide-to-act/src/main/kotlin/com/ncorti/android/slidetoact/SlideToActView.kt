package com.ncorti.android.slidetoact

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.util.Xml
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.util.*


/**
 *  Class representing the custom view, SlideToActView.
 *
 *  SlideToActView is an elegant material designed slider, that enrich your app
 *  with a "Slide-to-unlock" like widget.
 *
 *  @author cortinico
 */
class SlideToActView(context: Context,
                     attrs: AttributeSet?,
                     defStyle: Int) :
        View(context, attrs, defStyle) {

    constructor(context: Context) : this(context, null, R.styleable.SlideToActViewTheme_slideToActViewStyle)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, R.styleable.SlideToActViewTheme_slideToActViewStyle)

    /* -------------------- LAYOUT BOUNDS -------------------- */

    private var desiredSliderHeightDp: Float = 72F
    private var desiredSliderWidthDp: Float = 280F
    private var desiredSliderHeight: Int = 0
    private var desiredSliderWidth: Int = 0

    /* -------------------- MEMBERS -------------------- */

    /** Height of the drawing area */
    private var areaHeight: Int = 0
    /** Width of the drawing area */
    private var areaWidth: Int = 0
    /** Actual Width of the drawing area, used for animations */
    private var actualAreaWidth: Int = 0
    /** Border Radius, default to areaHeight/2, -1 when not initialized */
    private var borderRadius: Int = -1
    /** Margin of the cursor from the outer area */
    private var actualAreaMargin: Int
    private val originAreaMargin: Int

    /** Text message */
    private var textMessage: String = ""
    /** Size for the text message */
    private val textSize: Int

    private var textYPosition = -1f
    private var textXPosition = -1f

    /** Outer color used by the slider (primary) */
    private val outerColor: Int
    /** Inner color used by the slider (secondary, icon and border) */
    private val innerColor: Int

    /** Slider cursor position (between 0 and (`reaWidth - areaHeight)) */
    private var position: Int = 0
        get() = field
        set(value) {
            field = value
            if (areaWidth - areaHeight == 0) {
                // Avoid 0 division
                positionPerc = 0f
                positionPercInv = 1f
                return
            }
            positionPerc = value.toFloat() / (areaWidth - areaHeight).toFloat()
            positionPercInv = 1 - value.toFloat() / (areaWidth - areaHeight).toFloat()
        }
    /** Slider cursor position in percentage (between 0f and 1f) */
    private var positionPerc: Float = 0f
    /** 1/positionPerc */
    private var positionPercInv: Float = 1f


    /* -------------------- ICONS -------------------- */

    private val iconMargin: Int
    /** Margin for Arrow Icon */
    private var arrowMargin: Int
    /** Current angle for Arrow Icon */
    private var arrowAngle: Float = 0f
    /** Margin for Tick Icon */
    private var tickMargin: Int

    /** Color filter used for filling arrow and tick icons */
    private var outerColorFilter: LightingColorFilter
    private var innerColorFilter: LightingColorFilter

    /** Arrow drawable */
    private val mDrawableArrow: VectorDrawableCompat

    /** Tick drawable */
    private val mDrawableTick: AnimatedVectorDrawable
    private var mFlagDrawTick: Boolean = false

    /* -------------------- PAINT & DRAW -------------------- */
    /** Paint used for outer elements */
    private val outerPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** Paint used for inner elements */
    private val innerPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** Paint used for text elements */
    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** Inner rectangle (used for arrow rotation) */
    private var innerRect: RectF
    /** Outer rectangle (used for area drawing) */
    private var outerRect: RectF

    /** Grace value, when positionPerc > graceValue slider will perform the 'complete' operations */
    private val graceValue: Float = 0.8F

    /** Last X coordinate for the touch event */
    private var lastX: Float = 0F
    /** Flag to understand if user is moving the slider cursor */
    private var flagMoving: Boolean = false

    /** Private flag to check if the slide gesture have been completed */
    private var isCompleted = false

    /** Public flag to lock slider operativity */
    var isLocked = false

    private var outlineProviders: List<Any> = ArrayList()


    /** Slide event listeners */
    var onSlideToActAnimationEventListener: OnSlideToActAnimationEventListener? = null
    var onSlideCompleteListener: OnSlideCompleteListener? = null
    var onSlideResetListener: OnSlideResetListener? = null


    init {
        val layoutAttrs: TypedArray = context.theme.obtainStyledAttributes(attrs,
                R.styleable.SlideToActView, defStyle, R.style.SlideToActView)
        try {
            desiredSliderHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, desiredSliderHeightDp, resources.displayMetrics).toInt()
            desiredSliderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, desiredSliderWidthDp, resources.displayMetrics).toInt()
            desiredSliderHeight = layoutAttrs.getDimensionPixelSize(R.styleable.SlideToActView_slider_height, desiredSliderHeight)

            borderRadius = layoutAttrs.getDimensionPixelSize(R.styleable.SlideToActView_border_radius, -1)
            outerColor = layoutAttrs.getColor(R.styleable.SlideToActView_outer_color, R.color.defaultAccent)
            innerColor = layoutAttrs.getColor(R.styleable.SlideToActView_inner_color, R.color.white)
            textMessage = layoutAttrs.getString(R.styleable.SlideToActView_text)

            isLocked = layoutAttrs.getBoolean(R.styleable.SlideToActView_slider_locked, false)

            textSize = layoutAttrs.getDimensionPixelSize(R.styleable.SlideToActView_text_size, R.dimen.default_text_size)
            originAreaMargin = layoutAttrs.getDimensionPixelSize(R.styleable.SlideToActView_area_margin, R.dimen.default_area_margin)
            actualAreaMargin = originAreaMargin
        } finally {
            layoutAttrs.recycle()
        }

        innerRect = RectF((actualAreaMargin + position).toFloat(), actualAreaMargin.toFloat(),
                (areaHeight + position).toFloat() - actualAreaMargin.toFloat(),
                areaHeight.toFloat() - actualAreaMargin.toFloat())

        outerRect = RectF(actualAreaWidth.toFloat(), 0f, areaWidth.toFloat() - actualAreaWidth.toFloat(), areaHeight.toFloat())

        // TODO Remove !!
//        mDrawableArrow = VectorDrawableCompat.create(context.resources, R.drawable.ic_arrow, context.theme)!!
        mDrawableArrow = parseVectorDrawableCompat(context.resources, R.drawable.ic_arrow, context.theme)
//        mDrawableTick = AnimatedVectorDrawableCompat.create(context, R.drawable.animated_ic_check)!!
        mDrawableTick = context.resources.getDrawable(R.drawable.animated_ic_check, context.theme) as AnimatedVectorDrawable
//        mDrawableTick = parseAnimatedVectorDrawableCompat(context, R.drawable.animated_ic_check)

        outerPaint.color = outerColor
        innerPaint.color = innerColor

        // TODO Take typeface from theme
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = textSize.toFloat()
        textPaint.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        textPaint.color = innerColor

        outerColorFilter = LightingColorFilter(outerColor, 1)
        innerColorFilter = LightingColorFilter(innerColor, 1)
        mDrawableArrow.colorFilter = outerColorFilter

        iconMargin = context.resources.getDimensionPixelSize(R.dimen.default_icon_margin)
        arrowMargin = iconMargin
        tickMargin = iconMargin
    }

    private fun parseVectorDrawableCompat(res: Resources, resId: Int, theme: Resources.Theme): VectorDrawableCompat {
        val parser = res.getXml(resId)
        val attrs = Xml.asAttributeSet(parser)
        var type: Int = parser.next()
        while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
            type = parser.next()
        }
        if (type != XmlPullParser.START_TAG) {
            throw XmlPullParserException("No start tag found")
        }
        return VectorDrawableCompat.createFromXmlInner(res, parser, attrs, theme)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val width: Int

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredSliderWidth, widthSize)
        } else {
            width = desiredSliderWidth
        }
        setMeasuredDimension(width, desiredSliderHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        areaWidth = w
        areaHeight = h
        if (borderRadius == -1) // Round if not set up
            borderRadius = h / 2

        // Text horizontal/vertical positioning (both centered)
        textXPosition = areaWidth.toFloat() / 2
        textYPosition = (areaHeight.toFloat() / 2) - (textPaint.descent() + textPaint.ascent()) / 2

        // This outline provider force removal of shadow
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            outlineProviders = generateOutlineProviders((areaWidth - areaHeight) / 2)
            val outline = outlineProviders[0]
            if (outline is ViewOutlineProvider) {
                outlineProvider = outline
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        // Outer area
        outerRect.set(actualAreaWidth.toFloat(), 0f, areaWidth.toFloat() - actualAreaWidth.toFloat(), areaHeight.toFloat())
        canvas.drawRoundRect(outerRect, borderRadius.toFloat(), borderRadius.toFloat(), outerPaint)

        // Inner Cursor
        innerRect.set((actualAreaMargin + position).toFloat(),
                actualAreaMargin.toFloat(),
                (areaHeight + position).toFloat() - actualAreaMargin.toFloat(),
                areaHeight.toFloat() - actualAreaMargin.toFloat())
        canvas.drawRoundRect(innerRect, borderRadius.toFloat(), borderRadius.toFloat(), innerPaint)

        // Text alpha
        textPaint.alpha = (255 * positionPercInv).toInt()

        // Vertical + Horizontal centering
        canvas.drawText(textMessage, textXPosition, textYPosition, textPaint)

        // Arrow angle
        arrowAngle = -180 * positionPerc
        canvas.rotate(arrowAngle, innerRect.centerX(), innerRect.centerY())
        mDrawableArrow.setBounds(innerRect.left.toInt() + arrowMargin,
                innerRect.top.toInt() + arrowMargin,
                innerRect.right.toInt() - arrowMargin,
                innerRect.bottom.toInt() - arrowMargin)
        if (mDrawableArrow.bounds.left <= mDrawableArrow.bounds.right &&
                mDrawableArrow.bounds.top <= mDrawableArrow.bounds.bottom) {
            mDrawableArrow.draw(canvas)
        }
        canvas.rotate(-1 * arrowAngle, innerRect.centerX(), innerRect.centerY())

        // Tick drawing
        mDrawableTick.setBounds(
                actualAreaWidth + tickMargin,
                tickMargin,
                areaWidth - tickMargin - actualAreaWidth,
                areaHeight - tickMargin)

        mDrawableTick.colorFilter = innerColorFilter
        if (mFlagDrawTick) {
            mDrawableTick.draw(canvas)
        }
    }

    override fun isInEditMode(): Boolean {
        return true
    }

    /**
     * On Touch event handler
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null && isEnabled) {
            when (MotionEventCompat.getActionMasked(event)) {
                MotionEvent.ACTION_DOWN -> {
                    if (checkInsideButton(event.x, event.y)) {
                        flagMoving = true
                        lastX = event.x
                    }
                }
                MotionEvent.ACTION_UP -> {
                    flagMoving = false
                    if ((position > 0 && isLocked) || (position > 0 && positionPerc < graceValue)) {
                        // Check for grace value

                        val positionAnimator = ValueAnimator.ofInt(position, 0)
                        positionAnimator.duration = 300
                        positionAnimator.addUpdateListener({
                            position = it.animatedValue as Int
                            invalidateArea()
                        })
                        positionAnimator.start()
                    } else if (position > 0 && positionPerc >= graceValue) {
                        isEnabled = false // Fully disable touch events
                        startAnimationComplete()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (flagMoving) {
                        val diffX = event.x - lastX
                        lastX = event.x
                        increasePosition(diffX.toInt())
                        invalidateArea()
                    }
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    fun invalidateArea() {
        invalidate(outerRect.left.toInt(), outerRect.top.toInt(), outerRect.right.toInt(), outerRect.bottom.toInt())
    }

    /**
     * Private method to check if user has touched the slider cursor
     * @param x The x coordinate of the touch event
     * @param y The y coordinate of the touch event
     * @return A boolean that informs if user has pressed or not
     */
    private fun checkInsideButton(x: Float, y: Float): Boolean {
        return (0 < y && y < areaHeight && position < x && x < (areaHeight + position))
    }

    /**
     * Private method for increasing/decreasing the position
     * Ensure that position never exits from its range [0, (areaWidth - areaHeight)]
     *
     * @param inc Increment to be performed (negative if it's a decrement)
     */
    private fun increasePosition(inc: Int) {
        position += inc
        if (position < 0)
            position = 0
        if (position > (areaWidth - areaHeight))
            position = areaWidth - areaHeight
    }

    /**
     * Private method that is performed when user completes the slide
     */
    private fun startAnimationComplete() {
        val animSet = AnimatorSet()

        // Animator that moves the cursor
        val finalPositionAnimator = ValueAnimator.ofInt(position, areaWidth - areaHeight)
        finalPositionAnimator.addUpdateListener({
            position = it.animatedValue as Int
            invalidateArea()
        })

        // Animator that bounce away the cursors
        val marginAnimator = ValueAnimator.ofInt(actualAreaMargin, (innerRect.width() / 2).toInt() + actualAreaMargin)
        marginAnimator.addUpdateListener({
            actualAreaMargin = it.animatedValue as Int
            invalidateArea()
        })
        marginAnimator.interpolator = AnticipateOvershootInterpolator(2f)

        // Animator that reduces the outer area (to right)
        val areaAnimator = ValueAnimator.ofInt(0, (areaWidth - areaHeight) / 2)
        areaAnimator.addUpdateListener {
            actualAreaWidth = it.animatedValue as Int
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                val outline = outlineProviders[actualAreaWidth]
                if (outline is ViewOutlineProvider) {
                    outlineProvider = outline
                }
            }
            invalidateArea()
        }

        var tickAnimator: ValueAnimator
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            // Fallback not using AVD.
            tickAnimator = ValueAnimator.ofInt(0, 255)
            tickAnimator.addUpdateListener {
                tickMargin = iconMargin
                mFlagDrawTick = true
                mDrawableTick.alpha = it.animatedValue as Int
                invalidateArea()
            }
        } else {
            // Used AVD Animation.
            tickAnimator = ValueAnimator.ofInt(0)
            tickAnimator.addUpdateListener {
                if (!mFlagDrawTick) {
                    tickMargin = iconMargin
                    mFlagDrawTick = true
                    mDrawableTick.start()
                    invalidateArea()
                }
            }
        }

        if (position >= areaWidth - areaHeight) {
            animSet.playSequentially(marginAnimator, areaAnimator, tickAnimator)
        } else {
            animSet.playSequentially(finalPositionAnimator, marginAnimator, areaAnimator, tickAnimator)
        }

        animSet.duration = 300

        animSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                onSlideToActAnimationEventListener?.onSlideCompleteAnimationStarted(this@SlideToActView, positionPerc)
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                isCompleted = true
                onSlideToActAnimationEventListener?.onSlideCompleteAnimationEnded(this@SlideToActView)
                onSlideCompleteListener?.onSlideComplete(this@SlideToActView)
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

        })
        animSet.start()
    }

    /**
     * Method that reset the slider
     */
    fun resetSlider() {
        startAnimationReset()
    }

    /**
     * Method that returns the 'isCompleted' flag
     * @return True if slider is in the Complete state
     */
    fun isCompleted(): Boolean {
        return this.isCompleted
    }

    /**
     * Private method that is performed when you want to reset the cursor
     */
    private fun startAnimationReset() {
        isCompleted = false
        val animSet = AnimatorSet()

        // Animator that enlarges the outer area
        val tickAnimator = ValueAnimator.ofInt(tickMargin, areaWidth / 2)
        tickAnimator.addUpdateListener({
            tickMargin = it.animatedValue as Int
            invalidateArea()
        })

        // Animator that enlarges the outer area
        val areaAnimator = ValueAnimator.ofInt(actualAreaWidth, 0)
        areaAnimator.addUpdateListener({
            // Now we can hide the tick till the next complete
            mFlagDrawTick = false
            actualAreaWidth = it.animatedValue as Int
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                val outline = outlineProviders[actualAreaWidth]
                if (outline is ViewOutlineProvider) {
                    outlineProvider = outline
                }
            }
            invalidateArea()
        })

        val positionAnimator = ValueAnimator.ofInt(position, 0)
        positionAnimator.addUpdateListener({
            position = it.animatedValue as Int
            invalidateArea()
        })

        // Animator that re-draw the cursors
        val marginAnimator = ValueAnimator.ofInt(actualAreaMargin, originAreaMargin)
        marginAnimator.addUpdateListener({
            actualAreaMargin = it.animatedValue as Int
            invalidateArea()
        })
        marginAnimator.interpolator = AnticipateOvershootInterpolator(2f)

        // Animator that makes the arrow appear
        val arrowAnimator = ValueAnimator.ofInt(arrowMargin, iconMargin)
        arrowAnimator.addUpdateListener({
            arrowMargin = it.animatedValue as Int
            invalidateArea()
        })


        marginAnimator.interpolator = OvershootInterpolator(2f)
        animSet.playSequentially(tickAnimator, areaAnimator, positionAnimator, marginAnimator, arrowAnimator)
        animSet.duration = 300

        animSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                onSlideToActAnimationEventListener?.onSlideResetAnimationStarted(this@SlideToActView)
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                isEnabled = true
                mDrawableTick.stop()
                onSlideToActAnimationEventListener?.onSlideResetAnimationEnded(this@SlideToActView)
                onSlideResetListener?.onSlideReset(this@SlideToActView)
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }
        })
        animSet.start()
    }

    /**
     * Private method for generating outline providers (the shadow)
     */
    private fun generateOutlineProviders(end: Int): List<SlideToActOutlineProvider> {
        val outlines = ArrayList<SlideToActOutlineProvider>()
        for (j in 0..end) {
            outlines.add(SlideToActOutlineProvider(borderRadius, j, areaWidth - j, areaHeight))
        }
        return outlines
    }


    /**
     * Event handler for the SlideToActView animation events.
     * This event handler can be used to react to animation events from the Slide,
     * the event will be fired whenever an animation start/end.
     */
    interface OnSlideToActAnimationEventListener {

        /**
         * Called when the slide complete animation start. You can perform actions during the complete
         * animations.
         *
         * @param view The SlideToActView who created the event
         * @param threshold The position (in percentage [0f,1f]) where the user has left the cursor
         */
        fun onSlideCompleteAnimationStarted(view: SlideToActView, threshold: Float)

        /**
         * Called when the slide complete animation finish. At this point the slider is stuck in the
         * center of the slider.
         *
         * @param view The SlideToActView who created the event
         */
        fun onSlideCompleteAnimationEnded(view: SlideToActView)

        /**
         * Called when the slide reset animation start. You can perform actions during the reset
         * animations.
         *
         * @param view The SlideToActView who created the event
         */
        fun onSlideResetAnimationStarted(view: SlideToActView)

        /**
         * Called when the slide reset animation finish. At this point the slider will be in the
         * ready on the left of the screen and user can interact with it.
         *
         * @param view The SlideToActView who created the event
         */
        fun onSlideResetAnimationEnded(view: SlideToActView)
    }


    /**
     * Event handler for the slide complete event.
     * Use this handler to react to slide event
     */
    interface OnSlideCompleteListener {
        /**
         * Called when user performed the slide
         * @param view The SlideToActView who created the event
         */
        fun onSlideComplete(view: SlideToActView)
    }

    /**
     * Event handler for the slide react event.
     * Use this handler to inform the user that she can slide again.
     */
    interface OnSlideResetListener {
        /**
         * Called when slides is again available
         * @param view The SlideToActView who created the event
         */
        fun onSlideReset(view: SlideToActView)
    }

    /**
     * Outline provider for the SlideToActView.
     * This outline will suppress the shadow (till the moment when Android will support
     * updatable Outlines).
     */
    private class SlideToActOutlineProvider(borderRadius: Int, actualAreaWidth: Int, areaWidth: Int, areaHeight: Int) : ViewOutlineProvider() {
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
}



