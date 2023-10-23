package com.moaview.moaview.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO ssin :: 테스트필요 - 임시로 급히 추가해둠
class ItemSpacingDecoration(private val columnSpacing: Int, private val spanCount: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        setColumnSpacing(outRect, parent?.getChildAdapterPosition(view), parent, view, columnSpacing)
    }

    private fun setColumnSpacing(outRect: Rect?, position: Int?, parent: RecyclerView?, view: View, columnSpacing: Int) {

        if (position == null || parent == null)
            return

        val grid = parent.layoutManager as GridLayoutManager
        val spanCount = grid.spanCount
        val spanSize = grid.spanSizeLookup.getSpanSize(position)
        val spanIndex = grid.spanSizeLookup.getSpanIndex(position, spanCount)
        val viewType = grid.getItemViewType(view)
        if(viewType == 0 || viewType == 2) {
            return
        }

        outRect?.top = 0
        outRect?.bottom = columnSpacing / 2
        if (spanIndex == 0) {
            outRect?.left = columnSpacing
        } else {
            outRect?.left = columnSpacing / 2
        }

        if (spanIndex + spanSize == spanCount) {
            outRect?.right = columnSpacing
        } else {
            outRect?.right = columnSpacing / 2
        }
    }
}