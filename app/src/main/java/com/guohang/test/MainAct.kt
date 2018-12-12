package com.guohang.test

import android.app.Activity
import android.os.Bundle
import com.guohang.banner.Banner
import kotlinx.android.synthetic.main.act_main.*

class MainAct: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)

//        (banner as Banner<Int>)
//                .setDefaultIndicator {
//
//                }
//                .setData(listOf(R.mipmap.ic_launcher , R.mipmap.ic_launcher ,R.mipmap.ic_launcher ))
    }
}