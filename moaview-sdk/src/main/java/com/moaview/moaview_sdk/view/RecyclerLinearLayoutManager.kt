package com.moaview.moaview_sdk.view

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class RecyclerLinearLayoutManager(var context: Context?, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {
    private var isHorizontalScrollEnabled = false
    private var isVerticalScrollEnabled = false
    fun setHorizontalScrollEnabled(isHorizontalScrollEnabled: Boolean) {
        this.isHorizontalScrollEnabled = isHorizontalScrollEnabled
    }

    override fun canScrollHorizontally(): Boolean {
        return isHorizontalScrollEnabled && super.canScrollHorizontally()
    }

    fun setVerticalScrollEnabled(isVerticalScrollEnabled: Boolean) {
        this.isVerticalScrollEnabled = isVerticalScrollEnabled
    }

    override fun canScrollVertically(): Boolean {
        return isVerticalScrollEnabled && super.canScrollVertically()
    }

}