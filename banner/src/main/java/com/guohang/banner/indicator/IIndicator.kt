package com.guohang.banner.indicator

import android.view.View
import android.widget.RelativeLayout

interface IIndicator {
    fun setCount(count: Int)
    fun select(position: Int)

    fun rootView(): View
    fun indicatorParams(): RelativeLayout.LayoutParams
}