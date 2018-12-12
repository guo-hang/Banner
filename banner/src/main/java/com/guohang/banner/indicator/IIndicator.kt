package com.guohang.banner.indicator

import android.widget.RelativeLayout

interface IIndicator {
    fun setCount(count: Int)
    fun select(position: Int)

    fun indicatorParams(): RelativeLayout.LayoutParams
}