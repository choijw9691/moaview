package com.moaview.moaview.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO ssin :: 테스트필요 - 임시로 급히 추가해둠
class ItemDividerDecoration(
    private val height: Int,
    private val padding: Int,
    private val color: String
) : RecyclerView.ItemDecoration() {

    private val paint = Paint()
    init {
        paint.color = Color.parseColor(color)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingStart + padding
        val right = parent.width - parent.paddingEnd - padding
        for (i in 0 until parent.childCount) {
            val grid = parent.layoutManager as GridLayoutManager
            val child = parent.getChildAt(i)
            val viewType = grid.getItemViewType(child)
            if(viewType == 1){
                val params = child.layoutParams as RecyclerView.LayoutParams
                val top = (child.bottom + params.bottomMargin).toFloat()
                val bottom = top + height
                c.drawRect(left.toFloat(), top, right.toFloat(), bottom, paint)
            }
        }
    }
}