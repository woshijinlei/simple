package com.jeck.simplething.recyclerview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import androidx.core.view.forEach
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LinearItemDecoration(
    private var borderWith: Int = 0,
    private var borderColor: Int? = null,
    private var isSurroundItem: Boolean = false
) :
    RecyclerView.ItemDecoration() {
    private val decoratedBounds = Rect()

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (borderColor == null) return
        parent.forEach {
            parent.getDecoratedBoundsWithMargins(it, decoratedBounds)
            c.drawRect(decoratedBounds, Paint().apply {
                color = borderColor as Int
            })

        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val lm = parent.layoutManager as? LinearLayoutManager
        when {
            lm?.orientation == LinearLayoutManager.HORIZONTAL -> setHorizontalOutRect(
                outRect,
                view,
                parent
            )
            lm?.orientation == LinearLayoutManager.VERTICAL -> setVerticalOutRect(
                outRect,
                view,
                parent
            )
            else -> outRect.setEmpty()
        }
    }

    private fun setHorizontalOutRect(
        outRect: Rect,
        view: View,
        parent: RecyclerView
    ) {
        var left = borderWith
        val right: Int
        val position = parent.getChildAdapterPosition(view)
        when {
            position == 0 -> {
                left = borderWith
                right = borderWith / 2
            }
            position < parent.adapter!!.itemCount - 1 -> {
                left = borderWith / 2
                right = borderWith / 2
            }
            else -> {
                left = borderWith / 2
                right = borderWith
            }
        }
        val s = if (isSurroundItem) borderWith else 0
        outRect.set(left, s, right, s)
    }

    private fun setVerticalOutRect(
        outRect: Rect,
        view: View,
        parent: RecyclerView
    ) {
        var top = borderWith
        val bottom: Int
        val position = parent.getChildAdapterPosition(view)
        when {
            position == 0 -> {
                top = borderWith
                bottom = borderWith / 2
            }
            position < parent.adapter!!.itemCount - 1 -> {
                top = borderWith / 2
                bottom = borderWith / 2
            }
            else -> {
                top = borderWith / 2
                bottom = borderWith
            }
        }
        val s = if (isSurroundItem) borderWith else 0
        outRect.set(s, top, s, bottom)
    }
}

/**
 * 3 4 列特殊处理（小间距整型造成的误差）
 * 实际的间距和设置的borderWith有偏差
 */
class GridItemDecoration(
    private val borderWith: Int = 0,//px 3列2倍 4列3倍
    private var borderColor: Int? = null
) : RecyclerView.ItemDecoration() {

    private val decoratedBounds = Rect()
    private val compensate = mutableListOf<Float>()
    private val paint = Paint()
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        borderColor?.let { borderColor ->
            parent.forEach {
                parent.getDecoratedBoundsWithMargins(it, decoratedBounds)
                c.drawRect(decoratedBounds, paint.apply {
                    color = borderColor
                })
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val lm = parent.layoutManager as? GridLayoutManager
        if (lm?.orientation == GridLayoutManager.VERTICAL) {
            setVerticalOutRect(outRect, view, parent)
        }
    }

    private fun setVerticalOutRect(
        outRect: Rect,
        view: View,
        parent: RecyclerView
    ) {
        var left: Float
        var right: Float
        val gm = parent.layoutManager as GridLayoutManager
        val spanCount = gm.spanCount
        val is3Or4ColumnsSmallSize = spanCount == 3 || spanCount == 4
        for (i in 0 until spanCount) {
            compensate.add(((i.toFloat()) / (spanCount - 1)))
        }
        var top = borderWith / 2f
        var bottom = borderWith / 2f
        if (is3Or4ColumnsSmallSize) {
            if (spanCount == 3) {
                top = borderWith.toFloat()
                bottom = borderWith.toFloat()
            } else if (spanCount == 4) {
                top = borderWith * 1f
                bottom = borderWith * 2f
            }
        }
        val adapterPosition = parent.getChildAdapterPosition(view)
        val gravity = checkPosition(spanCount, adapterPosition)
        val horizontalPositionRotail = (compensate[(adapterPosition) % spanCount])
        (view.layoutParams as RecyclerView.LayoutParams).apply {
            topMargin =
                (borderWith * compensate.getOrElse(1) { 1 / 2f }).toInt()
            if (is3Or4ColumnsSmallSize) {
                if (spanCount == 3) {
                    topMargin = borderWith
                } else if (spanCount == 4) {
                    topMargin = borderWith
                }
            }
        }
        when (gravity) {
            Gravity.START -> {
                left = borderWith * horizontalPositionRotail
                right = borderWith * (1 - horizontalPositionRotail)
                if (is3Or4ColumnsSmallSize) {
                    if (spanCount == 3) {
                        left = 0f
                        right = borderWith * 2f
                    } else if (spanCount == 4) {
                        left = 0f
                        right = borderWith * 3f
                    }
                }
            }
            Gravity.END -> {
                left = borderWith * horizontalPositionRotail
                right = borderWith * (1 - horizontalPositionRotail)
                if (is3Or4ColumnsSmallSize) {
                    if (spanCount == 3) {
                        left = borderWith * 2f
                        right = 0f
                    } else if (spanCount == 4) {
                        left = borderWith * 3f
                        right = 0f
                    }
                }
            }
            else -> {
                left = borderWith * horizontalPositionRotail
                right = borderWith * (1 - horizontalPositionRotail)
                if (is3Or4ColumnsSmallSize) {
                    if (spanCount == 3) {
                        left = borderWith.toFloat()
                        right = borderWith.toFloat()
                    } else if (spanCount == 4) {//0 1 2 3
                        left = (((adapterPosition) % spanCount) * borderWith).toFloat()
                        right = 3 * borderWith - left
                    }
                }
            }
        }
        outRect.set(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    private fun checkPosition(
        spanCount: Int,
        position: Int
    ): Int {
        return when ((position + 1) % spanCount) {
            1 -> Gravity.START
            0 -> Gravity.END
            else -> Gravity.CENTER
        }
    }
}