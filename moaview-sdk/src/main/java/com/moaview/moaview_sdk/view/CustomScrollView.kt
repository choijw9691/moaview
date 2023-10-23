package com.moaview.moaview_sdk.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import com.moaview.moaview_sdk.R
import com.moaview.moaview_sdk.common.OnTouchAreaListener
import com.moaview.moaview_sdk.common.PageTurnSettingType
import com.moaview.moaview_sdk.common.TouchArea
import com.moaview.moaview_sdk.view.activity.ViewerActivity
import com.moaview.moaview_sdk.view.activity.ViewerActivity.Companion.settingData
import com.moaview.moaview_sdk.util.CommonUtil.getTouchArea
import com.moaview.moaview_sdk.util.ScreenSize
import kotlin.math.abs
import kotlin.math.max

/**
 * SLIDE 보기 모드 시 상위 컨테이너 역할
 * */
class CustomScrollView : ScrollView {

    companion object {
        private const val SWIPE_THRESHOLD = 350
        private const val SWIPE_VELOCITY_THRESHOLD = 350
        private const val INVALID_POINTER_ID = -1
    }

    private var mActivePointerId = INVALID_POINTER_ID
    private var scaleDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null
    private var scaleFactor = 1f
    private var maxWidth = 0.0f
    private var maxHeight = 0.0f
    private var maxTop = 0.0f
    private var lastTouchPositionX: Float = 0.toFloat()
    private var lastTouchPositionY: Float = 0.toFloat()
    private var positionX: Float = 0.toFloat()
    private var positionY: Float = 0.toFloat()
    private var width: Float = 0.toFloat()
    private var height: Float = 0.toFloat()
    private var minZoom = 1.0f
    private var maxZoom = 3.0f
    private var isDoubleTap: Boolean = false
    private var isTouchUp: Boolean = false
    var startAnimation : Boolean = true
    private var screenHeight = 0.0f // TODO ssin :: height랑 무슨 차이지?

    var onTouchAreaListener: OnTouchAreaListener? = null

    constructor(context: Context) : super(context) {
        if (!isInEditMode)
            scaleDetector = ScaleGestureDetector(getContext(), ScaleListener())
        gestureDetector = GestureDetector(getContext(), GestureListener())
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        if (!isInEditMode)
            scaleDetector = ScaleGestureDetector(getContext(), ScaleListener())
        gestureDetector = GestureDetector(getContext(), GestureListener())

    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (!isInEditMode)
            scaleDetector = ScaleGestureDetector(getContext(), ScaleListener())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        scrollTo(0, 0)
        var layout = getChildAt(0) as LinearLayout
        var imageView = layout.getChildAt(0) as ImageView
        width = imageView.layoutParams.width.toFloat()
        height = imageView.layoutParams.height.toFloat()
        screenHeight = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        if (layout.childCount > 1) {
            width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
            height = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        } else {
            width = width   // TODO ssin :: ???
        }
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

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        super.onTouchEvent(ev)
        val action = ev.action

        scaleDetector!!.onTouchEvent(ev)
        gestureDetector!!.onTouchEvent(ev)
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                lastTouchPositionX = x
                lastTouchPositionY = y
                mActivePointerId = ev.getPointerId(0)
            }

            MotionEvent.ACTION_MOVE -> {
                if (isTouchUp) {

                    val pointerIndex =
                        action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                    val x = ev.getX(pointerIndex)
                    val y = ev.getY(pointerIndex)
                    val dx = x - lastTouchPositionX
                    val dy = y - lastTouchPositionY

                    positionX += dx
                    positionY += dy

                    if (positionX > 0.0f) positionX = 0.0f
                    else if (positionX < maxWidth) {
                        positionX = maxWidth
                    }

                    if (positionY > 0.0) positionY = 0.0F
                    else if (positionY < maxHeight) positionY = maxHeight
                    lastTouchPositionX = x
                    lastTouchPositionY = y

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
                val pointerIndex =
                    action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                val pointerId = ev.getPointerId(pointerIndex)
                if (pointerId == mActivePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    lastTouchPositionX = ev.getX(newPointerIndex)
                    lastTouchPositionY = ev.getY(newPointerIndex)
                    mActivePointerId = ev.getPointerId(newPointerIndex)
                }
            }
        }

        return true
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()
        if (scaleFactor == minZoom) {
            positionX = 0.0f
            positionY = 0.0f
        }
        canvas.translate(positionX, positionY)
        canvas.scale(scaleFactor, scaleFactor)
        super.dispatchDraw(canvas)
        canvas.restore()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            isTouchUp = false

            scaleFactor *= detector.scaleFactor
            scaleFactor = max(minZoom, scaleFactor.coerceAtMost(maxZoom))

            if (scaleFactor < 3f) {
                val centerX = detector.focusX
                val centerY = detector.focusY + scrollY
                var diffX = centerX - positionX
                var diffY = centerY - positionY
                diffX = diffX * detector.scaleFactor - diffX
                diffY = diffY * detector.scaleFactor - diffY
                positionX -= diffX
                positionY -= diffY
            }
            maxWidth = width - width * scaleFactor
            maxHeight = (height - height * scaleFactor)
            maxTop = (-height * scaleFactor)
            invalidate()
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent): Boolean {

            isDoubleTap = true
            if (scaleFactor == 1F) {
                var rowY = e.rawY + scrollY
                scaleFactor = 3F
                positionX = e.rawX + e.rawX * -3F
                positionY = rowY + rowY * -3F
                maxWidth = width - width * scaleFactor
                maxHeight = height - height * scaleFactor
                isTouchUp = true
                invalidate()
            } else {
                scaleFactor = 1F
                positionX = 0F
                positionY = 0F
                maxWidth = width - width * scaleFactor
                maxHeight = height - height * scaleFactor
                invalidate()
            }
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onShowPress(e: MotionEvent) {
        }
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {

            var screenWidth = ScreenSize.getScreenWidth(context)
            var screenHeight = ScreenSize.getScreenHeight(context)

            var touchArea = getTouchArea(
                e.x,
                e.y,
                screenWidth,
                screenHeight,
                settingData.twoPageModeInOrientation,
                enumValueOf(settingData.pageTurnSettingType))

            var currentTwoPageMode = false  // orientation 분기 제거를 위한 two page 여부 지역 변수
            if(ViewerActivity.ORIENTATION == Configuration.ORIENTATION_LANDSCAPE) {
                currentTwoPageMode = settingData.twoPageModeInOrientation
            }

            var refactorTouchArea = when(settingData.pageTurnSettingType) { // 터치 모드에 따라 실제 디바이스 기준 터치 영역을 보정

                PageTurnSettingType.BEFORE_NEXT.name -> {
                    when(touchArea){
                        TouchArea.LEFT, TouchArea.RIGHT -> touchArea
                        TouchArea.BOOKMARK_LEFT -> {
                            if (!currentTwoPageMode) TouchArea.LEFT else touchArea
                        }
                        else -> touchArea
                    }
                }

                PageTurnSettingType.NEXT_BEFORE.name -> {
                    when(touchArea){
                        TouchArea.LEFT -> TouchArea.RIGHT
                        TouchArea.RIGHT -> TouchArea.LEFT
                        TouchArea.BOOKMARK_LEFT -> {
                            if(!currentTwoPageMode) TouchArea.RIGHT else touchArea
                        }
                        else -> touchArea
                    }
                }

                PageTurnSettingType.NEXT_NEXT.name -> {
                    when(touchArea){
                        TouchArea.LEFT, TouchArea.RIGHT -> TouchArea.RIGHT
                        TouchArea.BOOKMARK_LEFT -> {
                            if(!currentTwoPageMode) TouchArea.RIGHT else touchArea
                        }
                        else -> touchArea
                    }
                }

                PageTurnSettingType.TOP_DOWN_BEFORE_NEXT.name -> {
                    when(touchArea){
                        TouchArea.TOP -> TouchArea.LEFT
                        TouchArea.BOTTOM -> TouchArea.RIGHT
                        TouchArea.BOOKMARK_LEFT -> {
                            if(!currentTwoPageMode) TouchArea.LEFT else touchArea
                        }
                        else -> touchArea
                    }
                }

                PageTurnSettingType.TOP_DOWN_NEXT_NEXT.name -> {
                    when(touchArea){
                        TouchArea.TOP, TouchArea.BOTTOM -> TouchArea.RIGHT
                        TouchArea.BOOKMARK_LEFT -> {
                            if(!currentTwoPageMode) TouchArea.RIGHT else touchArea
                        }
                        else -> touchArea
                    }
                }
                else -> touchArea
            }

            if(refactorTouchArea == TouchArea.LEFT || refactorTouchArea == TouchArea.RIGHT) {
                resetZoomStatus()
            }
            onTouchAreaListener?.onTouch(refactorTouchArea)
            return true // TODO ssin :: return false인 경우 true인 경우 영향도 체크
        }

        private fun resetZoomStatus(){
            positionX = 0F
            positionY = 0F
            scaleFactor = 1.0F
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return false
        }

        override fun onLongPress(e: MotionEvent) {}

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {

            if (scaleFactor > 1.2F) {  // TODO ssin :: 뭔 조건이지? 필요없으면 삭제
                if ((-positionX.toInt() == (width * scaleFactor - width).toInt()) || positionX.toInt() == 0) {

                } else {
                    return true
                }
            }

            val diffX = e2.x - e1.x
            if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                val shake: Animation = AnimationUtils.loadAnimation(context, R.anim.shake)
                if (startAnimation && scaleFactor>1.0) {
                    this@CustomScrollView.startAnimation(shake)
                    startAnimation = false
                } else {
                    if (diffX > 0) {
                        positionX = 0F
                        positionY = 0F
                        scaleFactor = 1.0F
                        onTouchAreaListener?.onTouch(TouchArea.LEFT)
                    } else {
                        positionX = 0F
                        positionY = 0F
                        scaleFactor = 1.0F
                        onTouchAreaListener?.onTouch(TouchArea.RIGHT)
                    }
                    scrollTo(0, 0)
                    startAnimation = true
                    return true
                }
            }
            return false
        }
    }

    fun setChangePageCallback(callback: OnTouchAreaListener) {
        onTouchAreaListener = callback
    }
}
