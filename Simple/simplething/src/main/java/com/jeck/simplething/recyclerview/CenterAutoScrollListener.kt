package com.jeck.simplething.recyclerview

import android.view.View
import androidx.core.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import kotlin.math.abs

/**
 * 无法滑到的position不会得到监听
 * cannotAutoScrollToCenterCount
 * cannotDragPassToCenterCount
 */
class CenterAutoScrollListener(
    private val recyclerView: RecyclerView,
    private var isLinearSmooth: Boolean = false,
    private val autoScrolledPositionProvider: (position: Int) -> Unit
) :
    RecyclerView.OnScrollListener() {
    private val lm = recyclerView.layoutManager as LinearLayoutManager
    private val middleFeelLength = 20
    private var canAutoScroll = true
    private var hasPassed = false
    private val orientation = lm.orientation

    var cannotDragPassToCenterCount = 0
        private set
    val cannotAutoScrollToCenterCount = cannotDragPassToCenterCount - 1//

    var dragChildPassToCenterListener: ((position: Int) -> Unit)? = null

    init {
        recyclerView.doOnDetach {
            if (ViewCompat.isAttachedToWindow(it)) {
                recyclerView.removeOnScrollListener(this)
            }
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        calculateCannotScrollCounts()//how
        if (recyclerView.scrollState == SCROLL_STATE_DRAGGING) {
            checkChildPassCenter()
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        when (newState) {
            SCROLL_STATE_IDLE -> {
                if (canAutoScroll) {//防止二次执行
                    canAutoScroll = false
                    scrollChildToCenter()
                } else {
                    canAutoScroll = true
                }
            }
        }
    }

    private fun checkChildPassCenter() {
        var with = 0f
        var height = 0f
        val parent = if (orientation == LinearLayoutManager.HORIZONTAL) {
            ((recyclerView.x + recyclerView.x + recyclerView.measuredWidth) / 2).apply {
                with = this
            }
        } else {
            ((recyclerView.y + recyclerView.y + recyclerView.measuredHeight) / 2).apply {
                height = this
            }
        }
        recyclerView.findChildViewUnder(with, height)?.also {
            val child = if (orientation == LinearLayoutManager.HORIZONTAL) {
                (it.x + it.x + it.measuredWidth) / 2
            } else {
                (it.y + it.y + it.measuredHeight) / 2
            }
            val providePosition = recyclerView.getChildAdapterPosition(it)
            if (parent >= child - middleFeelLength && parent <= child + middleFeelLength) {
                if (!hasPassed) {
                    hasPassed = true
                    dragChildPassToCenterListener?.invoke(providePosition)
                }
            } else {
                hasPassed = false
            }
        }
    }

    private fun scrollChildToCenter() {
        val firstVisiblePosition = lm.findFirstVisibleItemPosition()
        val lastVisiblePosition = lm.findLastVisibleItemPosition()
        var suiteView: View? = null
        var t = Int.MAX_VALUE
        var targetPosition: Int = 0
        for (i in firstVisiblePosition..lastVisiblePosition) {
            lm.findViewByPosition(i)?.also {
                val distance = if (orientation == LinearLayoutManager.HORIZONTAL) {
                    val parent = (recyclerView.x + recyclerView.x + recyclerView.measuredWidth) / 2
                    val child = (it.x + it.x + it.measuredWidth) / 2
                    abs((child - parent).toDouble()).toInt()
                } else {
                    val parent = (recyclerView.y + recyclerView.y + recyclerView.measuredHeight) / 2
                    val child = (it.y + it.y + it.measuredHeight) / 2
                    abs((child - parent).toDouble()).toInt()
                }
                if (distance < t) {
                    t = distance
                    targetPosition = i
                    suiteView = it
                }
            }
        }
        suiteView?.let {
            if (targetPosition < cannotDragPassToCenterCount - 1) return
            val totalItems = recyclerView.adapter?.itemCount ?: 0
            if ((targetPosition > (totalItems - 1) - (cannotDragPassToCenterCount - 1))) return
            autoScrolledPositionProvider.invoke(targetPosition)
            if (orientation == LinearLayoutManager.HORIZONTAL) {
                recyclerView.smoothScrollToHorizontalCenter(it, isLinearSmooth)
            } else {
                recyclerView.smoothScrollToVerticalCenter(it, isLinearSmooth)
            }

        }
    }

    private fun calculateCannotScrollCounts(): Int {
        if (cannotDragPassToCenterCount != 0) return cannotDragPassToCenterCount
        while (true) {
            lm.findViewByPosition(cannotDragPassToCenterCount)?.also {
                val parent: Float
                val child: Float
                if (orientation == LinearLayoutManager.HORIZONTAL) {
                    parent = (recyclerView.x + recyclerView.x + recyclerView.measuredWidth) / 2
                    child = (it.x + it.x + it.measuredWidth) / 2
                } else {
                    parent = (recyclerView.y + recyclerView.y + recyclerView.measuredHeight) / 2
                    child = (it.y + it.y + it.measuredHeight) / 2
                }
                if (child < parent) {
                    cannotDragPassToCenterCount++
                } else {
                    return cannotDragPassToCenterCount
                }
            } ?: return cannotDragPassToCenterCount
        }
    }
}
