package com.jeck.simplething

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.children

class SimpleFlowLayout : ViewGroup {

    private var mHorizontalMargin = 0
    private var mVerticalMargin = 0
    private var mItemHeight = 0

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val typeArray =
            context.obtainStyledAttributes(attrs, R.styleable.SimpleFlowLayout, defStyleAttr, 0)
        mHorizontalMargin =
            typeArray.getDimensionPixelSize(R.styleable.SimpleFlowLayout_horizontalMargin, 0)
        mVerticalMargin =
            typeArray.getDimensionPixelSize(R.styleable.SimpleFlowLayout_verticalMargin, 0)
        mItemHeight = typeArray.getDimensionPixelSize(R.styleable.SimpleFlowLayout_itemHeight, 0)
        if (mVerticalMargin <= 0f) throw IllegalArgumentException("mItemHeight can not be 0")
        typeArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentSpecWith = MeasureSpec.getSize(widthMeasureSpec)
        val paddingWith = paddingLeft + paddingRight
        val paddingHeight = paddingTop + paddingBottom
        val heightSpec =
            MeasureSpec.makeMeasureSpec(
                mItemHeight + paddingHeight,
                MeasureSpec.EXACTLY
            )//measureChildren() include padding
        measureChildren(widthMeasureSpec, heightSpec)
        var totalHeight = mItemHeight + paddingHeight
        var usedWith = paddingWith
        var lastLineMaxHeight = 0
        val count = childCount
        for (i in 0 until count) {
            val change = checkChangeLine(parentSpecWith, usedWith, getChildAt(i).measuredWidth)
            if (change) {
                lastLineMaxHeight = 0
                usedWith = paddingWith + getChildAt(i).measuredWidth + mHorizontalMargin
                totalHeight += (mItemHeight + mVerticalMargin)
            } else {
                if (getChildAt(i).measuredHeight > lastLineMaxHeight) {
                    lastLineMaxHeight = getChildAt(i).measuredHeight
                }
                usedWith += (getChildAt(i).measuredWidth + mHorizontalMargin)
            }
        }
        setMeasuredDimension(parentSpecWith, totalHeight)
    }

    private fun checkChangeLine(
        parentSpecWith: Int,
        usedWith: Int,
        currentChildWith: Int
    ): Boolean {
        return parentSpecWith - usedWith < currentChildWith
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val paddingWith = paddingLeft + paddingRight
        var left = paddingLeft
        var top = paddingTop
        var useWith = paddingWith
        children.forEach {
            if (checkChangeLine(this.measuredWidth, useWith, it.measuredWidth)) {
                left = paddingLeft
                top += (mItemHeight + mVerticalMargin)
                useWith = paddingWith
            }
            val childVerticalCenter = (mItemHeight / 2 - it.measuredHeight / 2)
            it.layout(
                left,
                top + childVerticalCenter,
                left + it.measuredWidth,
                top + childVerticalCenter + it.measuredHeight
            )
            left += (it.measuredWidth + mHorizontalMargin)
            useWith += (it.measuredWidth + mHorizontalMargin)
        }
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams? {
        return MarginLayoutParams(context, attrs)
    }

}