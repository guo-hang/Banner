package com.guohang.banner.indicator

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout

class DefaultIndicator: IIndicator {
    private var mCurPosition = 0
    private var mCount = 0
    private var mIndicatorSize = dp2px(8)
    private var mIndicatorMargin = dp2px(5)
    private var mColorSelected = Color.WHITE
    private var mColorUnSelected = Color.GRAY
    private var mLayoutParams: RelativeLayout.LayoutParams? = null
    private lateinit var mContext: Context
    private val mRootView by lazy { LinearLayout(mContext) }

    constructor(context: Context) {
        mContext = context
    }

    override fun rootView(): View = mRootView

    override fun setCount(count: Int) {
        if (mCount != 0) mRootView.removeAllViews()

        mCount = count
        for (index in 0 until count) {
            ImageView(mContext).apply {
                layoutParams = LinearLayout.LayoutParams(mIndicatorSize , mIndicatorSize).apply {
                    leftMargin = mIndicatorMargin
                    rightMargin = mIndicatorMargin
                }


                setImageDrawable(getBg())
                isSelected = index == mCurPosition

                mRootView.addView(this)
            }
        }
    }

    override fun select(position: Int) {
        if (position in  0..mCount) {
            mRootView.getChildAt(mCurPosition).isSelected = false
            mRootView.getChildAt(position).isSelected = true
            mCurPosition = position
        }
    }

    override fun indicatorParams(): RelativeLayout.LayoutParams =
            if (null != mLayoutParams) mLayoutParams!! else
                RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT , RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    addRule(RelativeLayout.CENTER_HORIZONTAL)
                    bottomMargin = dp2px(10)
                }

    private fun getBg()=
        StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_selected) , getDrawable(mColorSelected))
            addState(intArrayOf(-android.R.attr.state_selected) , getDrawable(mColorUnSelected))
        }

    private fun getDrawable(color: Int)=
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setSize(mIndicatorSize , mIndicatorSize)
        }

    // 设置
    fun setSize(size: Int): DefaultIndicator {
        mIndicatorSize = size
        return this
    }

    fun setMargin(margin: Int): DefaultIndicator {
        mIndicatorMargin = margin
        return this
    }

    fun setColorSelected(color: Int): DefaultIndicator {
        mColorSelected = color
        return this
    }

    fun setColorUnSelected(color: Int): DefaultIndicator {
        mColorUnSelected = color
        return this
    }

    fun setIndicatorParams(params: RelativeLayout.LayoutParams) {
        mLayoutParams = params
    }

    private fun dp2px(dp: Int) = (mContext.resources.displayMetrics.density * dp + 0.5).toInt()
}