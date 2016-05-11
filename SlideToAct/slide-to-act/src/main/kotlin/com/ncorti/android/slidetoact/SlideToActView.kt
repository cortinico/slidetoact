package com.ncorti.android.slidetoact

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import java.util.*

/**
 *  Class representing the custom view, SlideToActView.
 *
 *  SlideToActView is an elegant material designed slider, that enrich your app
 *  with a "Slide-to-unlock" like widget.
 *
 *  @author cortinico
 */
class SlideToActView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    /* -------------------- DEFAULTS -------------------- */

    final private val DEFAULT_BORDER_RADIUS = -1
    final private val DEFAULT_AREA_MARGIN = 20
    final private val DEFAULT_TEXT_SIZE = 20F
    final private val DEFAULT_OUTER_COLOR = Color.CYAN
    final private val DEFAULT_INNER_COLOR = Color.WHITE
    final private val DEFAULT_ICON_MARGIN = 24
    final private val DEFAULT_GRACE_VALUE = 0.8f

    /* -------------------- LAYOUT BOUNDS -------------------- */

    private var desiredSliderHeightDp: Float = 80F
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
    /** Border Radius, default to areaHeight/2, -1 when not initialized*/
    private var borderRadius: Int = DEFAULT_BORDER_RADIUS
    /** Margin of the cursor from the outer area */
    private var actualAreaMargin: Int = DEFAULT_AREA_MARGIN
    private var originAreaMargin: Int = DEFAULT_AREA_MARGIN

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

    /** Slider cursor position (between 0 and (areaWidth - areaHeight)) */
    private var position: Int = 0
        get() = field
        set(value) {
            field = value
            if (areaWidth - areaHeight == 0) {
                // Avoid 0 division
                positionPerc = 0f;
                positionPercInv = 1f;
                return
            }
            positionPerc = value.toFloat() / (areaWidth - areaHeight).toFloat();
            positionPercInv = 1 - value.toFloat() / (areaWidth - areaHeight).toFloat();
        }
    /** Slider cursor position in percentage (between 0f and 1f) */
    private var positionPerc: Float = 0f
    /** 1/positionPerc */
    private var positionPercInv: Float = 1f


    /* -------------------- ICONS -------------------- */

    /** Margin for Arrow Icon */
    private var arrowMargin: Int = DEFAULT_ICON_MARGIN
    /** Current angle for Arrow Icon */
    private var arrowAngle: Float = 0f
    /** Margin for Tick Icon */
    private var tickMargin: Int = DEFAULT_ICON_MARGIN

    /** Color filter used for filling arrow and tick icons */
    private var outerColorFilter = LightingColorFilter(DEFAULT_OUTER_COLOR, 1)
    private var innerColorFilter = LightingColorFilter(DEFAULT_INNER_COLOR, 1)


    /** Flag used to draw check if tick must be drawn */
    private var drawTick: Boolean = false

    /** Frame of the tick that must be drawn */
    private var tickFrame = 0

    /** Arrow drawable */
    private val mDrawableArrow: Drawable

    /** Tick drawable (frame list) */
    private val mDrawableTick: Drawable
    private var mDrawableTickFrame: Drawable? = null

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
    private val graceValue: Float = DEFAULT_GRACE_VALUE

    /** Last X coordinate for the touch event */
    private var lastX: Float = 0F
    /** Flag to understand if user is moving the slider cursor */
    private var flagMoving: Boolean = false

    /** Private flag to check if the slide gesture have been completed */
    private var isCompleted = false;

    /** Public flag to lock slider operativity */
    var isLocked = false;

    private var outlineProviders: List<Any> = ArrayList()


    /** Slide event listeners */
    var onSlideToActAnimationEventListener: OnSlideToActAnimationEventListener? = null
    var onSlideCompleteListener: OnSlideCompleteListener? = null
    var onSlideResetListener: OnSlideResetListener? = null


    init {
        val layoutAttrs: TypedArray = context.theme.obtainStyledAttributes(attrs,
                R.styleable.SlideToActView, 0, 0)
        try {
            // Load accent color from the theme
            var tValue = TypedValue()
            var desiredColor: Int
            if (context.theme.resolveAttribute(R.attr.colorAccent, tValue, true)) {
                desiredColor = tValue.data
            } else {
                desiredColor = DEFAULT_OUTER_COLOR
            }

            desiredSliderHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, desiredSliderHeightDp, resources.displayMetrics).toInt()
            desiredSliderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, desiredSliderWidthDp, resources.displayMetrics).toInt()
            desiredSliderHeight = layoutAttrs.getDimensionPixelSize(R.styleable.SlideToActView_slider_height, desiredSliderHeight)

            borderRadius = layoutAttrs.getDimensionPixelSize(R.styleable.SlideToActView_border_radius, DEFAULT_BORDER_RADIUS)
            outerColor = layoutAttrs.getColor(R.styleable.SlideToActView_outer_color, desiredColor)
            innerColor = layoutAttrs.getColor(R.styleable.SlideToActView_inner_color, DEFAULT_INNER_COLOR)
            textMessage = layoutAttrs.getString(R.styleable.SlideToActView_text)

            isLocked = layoutAttrs.getBoolean(R.styleable.SlideToActView_slider_locked, false);

            var textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE, resources.displayMetrics).toInt()
            textSize = layoutAttrs.getDimensionPixelSize(R.styleable.SlideToActView_text_size, textSizePx)
            actualAreaMargin = layoutAttrs.getDimensionPixelSize(R.styleable.SlideToActView_area_margin, DEFAULT_AREA_MARGIN)
            originAreaMargin = actualAreaMargin
        } finally {
            layoutAttrs.recycle()
        }

        innerRect = RectF((actualAreaMargin + position).toFloat(), actualAreaMargin.toFloat(),
                (areaHeight + position).toFloat() - actualAreaMargin.toFloat(),
                areaHeight.toFloat() - actualAreaMargin.toFloat())

        outerRect = RectF(actualAreaWidth.toFloat(), 0f, areaWidth.toFloat() - actualAreaWidth.toFloat(), areaHeight.toFloat())

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            mDrawableArrow = context.resources.getDrawable(R.drawable.ic_arrow_right_white_48dp, context.theme)
            mDrawableTick = context.resources.getDrawable(R.drawable.animated_tick, context.theme)
        } else {
            mDrawableArrow = context.resources.getDrawable(R.drawable.ic_arrow_right_white_48dp)
            mDrawableTick = context.resources.getDrawable(R.drawable.animated_tick)
        }

        outerPaint.color = outerColor
        innerPaint.color = innerColor

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = textSize.toFloat()
        textPaint.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        textPaint.color = innerColor

        outerColorFilter = LightingColorFilter(outerColor, 1)
        innerColorFilter = LightingColorFilter(innerColor, 1)
        mDrawableArrow.colorFilter = outerColorFilter
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var width: Int

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredSliderWidth, widthSize);
        } else {
            width = desiredSliderWidth;
        }
        setMeasuredDimension(width, desiredSliderHeight);
    }

    @SuppressLint("NewApi")
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
            var outline = outlineProviders[0]
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

        // Tick animation drawing
        if (drawTick) {
            if (mDrawableTick is AnimationDrawable) {
                mDrawableTickFrame = mDrawableTick.getFrame(tickFrame);
                mDrawableTickFrame?.setBounds(
                        actualAreaWidth + tickMargin,
                        tickMargin,
                        areaWidth - tickMargin - actualAreaWidth,
                        areaHeight - tickMargin)

                mDrawableTickFrame?.colorFilter = innerColorFilter
                if ((actualAreaWidth + tickMargin) <= (areaWidth - tickMargin - actualAreaWidth) &&
                        (tickMargin <= areaHeight - tickMargin)) { // Left <= Right && Top <= Bottom
                    mDrawableTickFrame?.draw(canvas)
                }
            }
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

                        var positionAnimator = ValueAnimator.ofInt(position, 0)
                        positionAnimator.duration = 300
                        positionAnimator.addUpdateListener({
                            position = it.animatedValue as Int
                            invalidate()
                        })
                        positionAnimator.start()
                    } else if (position > 0 && positionPerc >= graceValue) {
                        isEnabled = false // Fully disable touch events
                        startAnimationComplete()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (flagMoving) {
                        var diffX = event.x - lastX
                        lastX = event.x
                        increasePosition(diffX.toInt())
                        invalidate()
                    }
                }
            }
            return true
        }
        return super.onTouchEvent(event)
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
        var animSet = AnimatorSet()

        // Animator that moves the cursor
        var finalPositionAnimator = ValueAnimator.ofInt(position, areaWidth - areaHeight)
        finalPositionAnimator.addUpdateListener({
            position = it.animatedValue as Int
            invalidate()
        })

        // Animator that bounce away the cursors
        var marginAnimator = ValueAnimator.ofInt(actualAreaMargin, (innerRect.width() / 2).toInt() + actualAreaMargin)
        marginAnimator.addUpdateListener({
            actualAreaMargin = it.animatedValue as Int
            invalidate()
        })
        marginAnimator.interpolator = AnticipateOvershootInterpolator(2f)

        // Animator that reduces the outer area (to right)
        var areaAnimator = ValueAnimator.ofInt(0, (areaWidth - areaHeight) / 2)
        areaAnimator.addUpdateListener({
            actualAreaWidth = it.animatedValue as Int
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                var outline = outlineProviders[actualAreaWidth]
                if (outline is ViewOutlineProvider) {
                    outlineProvider = outline
                }
            }
            invalidate()
        })

        tickMargin = DEFAULT_ICON_MARGIN
        // Animator that draw the Tick
        var tickAnimator = ValueAnimator.ofInt(0, 41)
        tickAnimator.addUpdateListener({
            drawTick = true
            tickFrame = it.animatedValue as Int
            invalidate()
        })
        tickAnimator.duration = 700

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
                isCompleted = true;
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
    @SuppressLint("NewApi")
    private fun startAnimationReset() {
        var animSet = AnimatorSet()

        // Animator that enlarges the outer area
        var tickAnimator = ValueAnimator.ofInt(tickMargin, areaWidth / 2)
        tickAnimator.addUpdateListener({
            tickMargin = it.animatedValue as Int
            invalidate()
        })

        var positionAnimator = ValueAnimator.ofInt(position, 0)
        positionAnimator.addUpdateListener({
            position = it.animatedValue as Int
            invalidate()
        })

        // Animator that enlarges the outer area
        var areaAnimator = ValueAnimator.ofInt(actualAreaWidth, 0)
        areaAnimator.addUpdateListener({
            actualAreaWidth = it.animatedValue as Int
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                var outline = outlineProviders[actualAreaWidth]
                if (outline is ViewOutlineProvider) {
                    outlineProvider = outline
                }
            }
            invalidate()
        })

        // Animator that re-draw the cursors
        var marginAnimator = ValueAnimator.ofInt(actualAreaMargin, originAreaMargin)
        marginAnimator.addUpdateListener({
            actualAreaMargin = it.animatedValue as Int
            invalidate()
        })

        // Animator that makes the arrow appear
        var arrowAnimator = ValueAnimator.ofInt(arrowMargin, DEFAULT_ICON_MARGIN)
        arrowAnimator.addUpdateListener({
            arrowMargin = it.animatedValue as Int
            invalidate()
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
                isCompleted = false;
                drawTick = false;
                isEnabled = true;
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
    @SuppressLint("NewApi")
    private fun generateOutlineProviders(end: Int): List<SlideToActOutlineProvider> {
        var outlines = ArrayList<SlideToActOutlineProvider>()
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
}


