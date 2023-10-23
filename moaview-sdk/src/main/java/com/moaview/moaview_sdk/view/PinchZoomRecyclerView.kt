package com.moaview.moaview_sdk.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.recyclerview.widget.RecyclerView
import com.moaview.moaview_sdk.common.OnTouchAreaListener
import com.moaview.moaview_sdk.common.onScrollListener
import com.moaview.moaview_sdk.common.TouchArea
import com.moaview.moaview_sdk.util.ScreenSize

/**
 *
 * */
class PinchZoomRecyclerView : RecyclerView {
    private var mActivePointerId = INVALID_POINTER_ID
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mDetector: GestureDetector? = null
    private var mScaleFactor = 1f
    private var maxWidth = 0.0f
    private var maxHeight = 0.0f
    private var mLastTouchX: Float = 0.toFloat()
    private var mLastTouchY: Float = 0.toFloat()
    private var mPosX: Float = 0.toFloat()
    private var mPosY: Float = 0.toFloat()
    private var width: Float = 0.toFloat()
    private var height: Float = 0.toFloat()
    private var minZoom = 1.0f
    private var maxZoom = 3.0f
    private val SWIPE_THRESHOLD = 350
    private val SWIPE_VELOCITY_THRESHOLD = 350
    private var isTouchUp: Boolean = false
    var callbackListner: OnTouchAreaListener? = null
    var onScrollListener: onScrollListener? = null

    constructor(context: Context) : super(context) {
        if (!isInEditMode)
            mScaleDetector = ScaleGestureDetector(getContext(), ScaleListener())
        mDetector = GestureDetector(getContext(), gestureListener())
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        if (!isInEditMode)
            mScaleDetector = ScaleGestureDetector(getContext(), ScaleListener())
        mDetector = GestureDetector(getContext(), gestureListener())

    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (!isInEditMode)
            mScaleDetector = ScaleGestureDetector(getContext(), ScaleListener())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        height = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        mPosX = 0F
        mPosY = 0F
        mScaleFactor = 1.0F
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }

        return false
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)

        if (state == SCROLL_STATE_IDLE) {
            onScrollListener?.onScrollFinish()
            if (!canScrollVertically(-1)) {
                onScrollListener?.onScrollTop()
            } else if (!canScrollVertically(1)) {
                onScrollListener?.onScrollBottom()
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        super.onTouchEvent(ev)
        val action = ev.action
        mScaleDetector!!.onTouchEvent(ev)
        mDetector!!.onTouchEvent(ev)
            when (action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {

                    val x = ev.x
                    val y = ev.y
                    mLastTouchX = x
                    mLastTouchY = y
                    mActivePointerId = ev.getPointerId(0)
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isTouchUp) {

                        val pointerIndex =
                            action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                        val x = ev.getX(pointerIndex)
                        val y = ev.getY(pointerIndex)
                        val dx = x - mLastTouchX
                        val dy = y - mLastTouchY

                        mPosX += dx
                        mPosY += dy

                        if (mPosX > 0.0f) mPosX = 0.0f
                        else if (mPosX < maxWidth) {
                            mPosX = maxWidth

                        }

                        if (mPosY > 0.0f) mPosY = 0.0f
                        else if (mPosY < maxHeight) mPosY = maxHeight

                        mLastTouchX = x
                        mLastTouchY = y

                        invalidate()
                    }

                }

                MotionEvent.ACTION_UP -> {
                    mActivePointerId = INVALID_POINTER_ID
                }

                MotionEvent.ACTION_CANCEL -> {
                    mActivePointerId = INVALID_POINTER_ID
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    isTouchUp = true
                    (layoutManager as RecyclerLinearLayoutManager).setVerticalScrollEnabled(true)

                    val pointerIndex =
                        action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                    val pointerId = ev.getPointerId(pointerIndex)
                    if (pointerId == mActivePointerId) {
                        val newPointerIndex = if (pointerIndex == 0) 1 else 0
                        mLastTouchX = ev.getX(newPointerIndex)
                        mLastTouchY = ev.getY(newPointerIndex)
                        mActivePointerId = ev.getPointerId(newPointerIndex)
                    }

                }
            }


        return true
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()

        if (mScaleFactor == minZoom) {
            mPosX = 0.0f
            mPosY = 0.0f

        }
        canvas.translate(mPosX, mPosY)
        canvas.scale(mScaleFactor, mScaleFactor)
        super.dispatchDraw(canvas)
        canvas.restore()
    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            isTouchUp = false
            (layoutManager as RecyclerLinearLayoutManager).setVerticalScrollEnabled(false)

            mScaleFactor *= detector.scaleFactor
            mScaleFactor = Math.max(minZoom, Math.min(mScaleFactor, maxZoom))

            if (mScaleFactor < 3f) {
                val centerX = detector.focusX
                val centerY = detector.focusY
                var diffX = centerX - mPosX
                var diffY = centerY - mPosY
                diffX = diffX * detector.scaleFactor - diffX
                diffY = diffY * detector.scaleFactor - diffY
                mPosX -= diffX
                mPosY -= diffY

            }
            maxWidth = width - width * mScaleFactor
            maxHeight = (height - height * mScaleFactor)

            invalidate()
            return true
        }
    }

    private inner class gestureListener : GestureDetector.SimpleOnGestureListener() {


        override fun onDoubleTap(e: MotionEvent): Boolean {

            if (mScaleFactor == 1F) {

                mScaleFactor = 3F
                mPosX = e.x + e.x * -3F
                mPosY = e.y + e.y * -3F
                maxWidth = width - width * mScaleFactor
                maxHeight = height - height * mScaleFactor
                isTouchUp = true
                (layoutManager as RecyclerLinearLayoutManager).setVerticalScrollEnabled(true)

                invalidate()
            } else {
                mScaleFactor = 1F
                mPosX = 0F
                mPosY = 0F
                maxWidth = width - width * mScaleFactor
                maxHeight = height - height * mScaleFactor
                invalidate()
            }


            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        //화면이 눌렸다 떼어지는 경우
        override fun onShowPress(e: MotionEvent) {

        }

        //화면이 한 손가락으로 눌렸다 떼어지는 경우
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            var screenWidth = ScreenSize.getScreenWidth(context)
            var screenHeight = ScreenSize.getScreenHeight(context)
            if (e != null) {
                if (e.x >= screenWidth - (screenWidth / 7)) {
                    if (e.y <= screenHeight / 7) {
                        callbackListner?.onTouch(TouchArea.BOOKMARK_RIGHT)
                        return true
                    }
                }
            }
            callbackListner?.onTouch(TouchArea.CENTER)
            return true
        }

        //화면이 눌린채 일정한 속도와 방향으로 움직였다 떼어지는 경우
        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return false
        }

        //화면을 손가락으로 오랫동안 눌렀을 경우
        override fun onLongPress(e: MotionEvent) {
        }

        //화면이 눌린채 손가락이 가속해서 움직였다 떼어지는 경우
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return false
        }
    }

    fun setChangePageCallback(callback: OnTouchAreaListener) {
        callbackListner = callback
    }

    fun setOnScrollChangeListener(onScrollListener: onScrollListener) {
        this.onScrollListener = onScrollListener
    }


    companion object {
        private const val INVALID_POINTER_ID = -1
    }

}