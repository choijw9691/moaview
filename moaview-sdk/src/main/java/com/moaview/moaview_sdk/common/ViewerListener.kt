package com.moaview.moaview_sdk.common

import android.view.View
import com.moaview.moaview_sdk.common.TouchArea

// 사용자 터치 이벤트 callback
interface OnTouchAreaListener {
    fun onTouch(touchArea: TouchArea)
}

// 리사이클러 뷰 스크롤 callback
interface onScrollListener {
    fun onScrollFinish()
    fun onScrollTop()
    fun onScrollBottom()
}

interface memoCallback {
    fun onClickCallBack(view: View)
}


