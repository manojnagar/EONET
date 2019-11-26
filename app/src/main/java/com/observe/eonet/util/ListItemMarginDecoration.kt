package com.observe.eonet.util

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewItemDecoration(
    private val cardMargin: Int,
    dividerColor: Int,
    private val dividerHeight: Int
) : RecyclerView.ItemDecoration() {

    private var paint = Paint()

    init {
        paint.color = dividerColor
        paint.isAntiAlias = true
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        val left = parent.left + cardMargin
        val right = parent.right - cardMargin

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin + cardMargin / 2
            val bottom = top + dividerHeight

            //Draw the divider
            c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = cardMargin
            }
            left = cardMargin
            right = cardMargin
            bottom = cardMargin
        }
    }
}