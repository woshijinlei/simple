package com.jeck.simplething.recyclerview

import android.util.Log
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView

/**
 * 一个设置recyclerView某个或者某些位置的item被选择其他不选择的Adapter
 * 如果我们不想或者不需要在数据结构中添加类似isSelected标记字段
 * 不需要 notifyDataSetChanged刷新数据更新UI显示效果
 */
abstract class SimpleItemSelectedAdapter<T : RecyclerView.ViewHolder> : RecyclerView.Adapter<T>() {
    private val TAG = "SimpleItem"
    private var mIsNeedCheckPositions = mutableMapOf<Int, Int>()
    private var mSelectedPositions = hashSetOf<Int>()
    private var mRecyclerView: RecyclerView? = null
    private val mSelectedFlag = 0
    private val mUnSelectedFlag = 1

    @Suppress("UNCHECKED_CAST")
    fun removeSelectedPosition(position: Int) {
        mRecyclerView?.also {
            if (mSelectedPositions.contains(position)) {
                val lastHolder = it.findViewHolderForAdapterPosition(position) as? T
                if (lastHolder != null) {
                    Log.d(TAG, "onItemUnSelected success when setSelectedPosition")
                    mIsNeedCheckPositions.remove(position)
                    onItemUnSelected(lastHolder, position)
                } else {
                    Log.d(TAG, "onItemUnSelected failed  when setSelectedPosition")
                    mIsNeedCheckPositions[position] = mUnSelectedFlag
                }
                mSelectedPositions.remove(position)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun addSelectedPosition(position: Int) {
        mRecyclerView?.also {
            val currentHolder = it.findViewHolderForAdapterPosition(position) as? T
            if (currentHolder != null) {
                Log.d(TAG, "onItemSelected failed  when setSelectedPosition")
                mIsNeedCheckPositions.remove(position)
                onItemSelected(currentHolder, position)
            } else {
                mIsNeedCheckPositions[position] = mSelectedFlag
                Log.d(TAG, "onItemSelected failed  when setSelectedPosition")
            }
            mSelectedPositions.add(position)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun setSelectedPosition(position: Int) {
        mRecyclerView?.also { recyclerView ->
            val size = mIsNeedCheckPositions.size
            for (p in 0 until size) {
                mIsNeedCheckPositions[p] = mUnSelectedFlag
            }
            mSelectedPositions.forEach { it1 ->
                val lastHolder = recyclerView.findViewHolderForAdapterPosition(it1) as? T
                if (lastHolder != null) {
                    Log.d(TAG, "onItemUnSelected success when setSelectedPosition")
                    mIsNeedCheckPositions.remove(it1)
                    onItemUnSelected(lastHolder, it1)
                } else {
                    Log.d(TAG, "onItemUnSelected failed  when setSelectedPosition")
                    mIsNeedCheckPositions[it1] = mUnSelectedFlag
                }
            }
            val currentHolder = recyclerView.findViewHolderForAdapterPosition(position) as? T
            if (currentHolder != null) {
                Log.d(TAG, "onItemSelected failed  when setSelectedPosition")
                mIsNeedCheckPositions.remove(position)
                onItemSelected(currentHolder, position)
            } else {
                mIsNeedCheckPositions[position] = mSelectedFlag
                Log.d(TAG, "onItemSelected failed  when setSelectedPosition")
            }
            mSelectedPositions.clear()
            mSelectedPositions.add(position)
        }
    }

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.mRecyclerView = recyclerView
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.mRecyclerView = null
    }

    @CallSuper
    override fun onViewAttachedToWindow(holder: T) {
        val position = holder.adapterPosition
        if (mIsNeedCheckPositions.contains(position)) {
            Log.d(TAG, "re onItemSelected when onViewAttachedToWindow")
            mIsNeedCheckPositions.remove(position)?.also {
                if (it == mSelectedFlag) {
                    onItemSelected(holder, position)
                } else {
                    onItemUnSelected(holder, position)
                }
            }
        }
    }

    @CallSuper
    override fun onBindViewHolder(holder: T, position: Int) {
        if (mSelectedPositions.contains(position)) {
            onItemSelected(holder, position)
        } else {
            onItemUnSelected(holder, position)
        }
    }

    abstract fun onItemSelected(viewHolder: T, position: Int)

    abstract fun onItemUnSelected(viewHolder: T, position: Int)
}