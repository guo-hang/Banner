package com.guohang.banner

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.guohang.banner.indicator.DefaultIndicator
import com.guohang.banner.indicator.IIndicator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class Banner<T>: RelativeLayout {
    private val tag = "Banner"
    //是否打印log
    private var mEnableLog = true
    //是否自动轮播
    private var mIsAutoPlay = true
    //轮播间隔
    private var mDelayTime = 0L
    //指示器大小
    private var mIndicatorSize = 0
    //指示器间距
    private var mIndicatorMargin = 0
    //RxJava 轮询
    private var mDisposable: Disposable? = null
    //设置数据
    private var mData: List<T>? = null
    //创建单页界面
    private var mCreatePage: (T)-> View = {
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            if (it is Int) setImageResource(it)
        }
    }
    //ViewPage
    private var mViewPager: ViewPager? = null
    // 指示器
    private var mIndicators: IIndicator? = null
    //默认图片
    private var mDefaultImg: ImageView? = null
    //当前坐标
    private var mCurPosition = 0
    //
    private val mPagers: MutableList<View> by lazy { mutableListOf<View>() }

    constructor(context: Context) : this(context , null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs , 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attrs?.apply { initAttrs(context , attrs) }
    }

    //解析自定义属性
    private fun initAttrs(context: Context , attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Banner)
        mIsAutoPlay = typedArray.getBoolean(R.styleable.Banner_isAutoPlay , BannerConfig.IS_AUTO_PLAY)
        mDelayTime = typedArray.getInt(R.styleable.Banner_delayTime , BannerConfig.DELAY_TIME).toLong()
        mIndicatorSize = typedArray.getDimensionPixelSize(R.styleable.Banner_indicatorSize , 0)
        mIndicatorMargin = typedArray.getDimensionPixelSize(R.styleable.Banner_indicatorMargin , 0)

        val drawable = typedArray.getDrawable(R.styleable.Banner_defaultSrc)
        drawable?.apply { setDefaultImg(this) }

        typedArray.recycle()
    }

    //设置默认图片
    private fun setDefaultImg(drawable: Drawable) {
        if (null == mDefaultImg) {
            mDefaultImg = ImageView(context)
            addView(mDefaultImg , RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.MATCH_PARENT))
        }
        mDefaultImg?.setImageDrawable(drawable)
    }

    /**
     * =========================================   设置    =====================================================
     */
    //是否打印log
    fun enableLog(enable: Boolean): Banner<T> {
        mEnableLog = enable
        return this
    }

    //Viewpager 页面数据
    fun createPage(page: (T)-> View): Banner<T> {
        mCreatePage = page
        return this
    }

    //设置viewpager
    fun setViewPager(fuc: ViewPager.()-> Unit): Banner<T> {
        initViewPager()
        mViewPager?.fuc()
        return this
    }

    //设置默认指示器
    fun setDefaultIndicator(fuc: DefaultIndicator.() -> Unit): Banner<T>  {
        mIndicators?.apply { this@Banner.removeView(this.rootView()) }

        if (mIndicators !is DefaultIndicator) mIndicators = DefaultIndicator(context)
        (mIndicators as? DefaultIndicator)?.fuc()
        return this
    }

    //自定义指示器
    fun setCustomIndicator(indicator: IIndicator): Banner<T>  {
        mIndicators?.apply { this@Banner.removeView(this.rootView()) }

        mIndicators = indicator
        return this
    }

    //设置数据
    fun setData(list: List<T>): Banner<T> {
        mData = list
        if (mData?.isNotEmpty() == true) {
            initViewPager()
            setViewPager()

            initIndicator()
            setIndicator()

            startAutoPlay()
        }
        return this
    }

    /**
     *  ========================================    根据数据构建界面    ==============================
     */

    //初始化viewpager
    private fun initViewPager() {
        if (null == mViewPager) {
            mViewPager = ViewPager(context).apply {
                layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.MATCH_PARENT)
                this@Banner.addView(this)
                this.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrollStateChanged(state: Int) {}

                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                    override fun onPageSelected(position: Int) {
                        mCurPosition = position
                        mIndicators?.select(position)
                    }
                })
            }
        }
    }

    //
    private fun setViewPager() {
        mViewPager?.adapter?.apply {
            mPagers.clear()
            this.notifyDataSetChanged()
            mCurPosition = 0
        } ?: apply {
            mViewPager?.adapter = object : PagerAdapter() {
                override fun isViewFromObject(view: View, `object`: Any) = view === `object`

                override fun getCount() = mData?.size ?: 0

                override fun instantiateItem(container: ViewGroup, position: Int): Any {
                    val view = if (position in 0 until mPagers.size) mPagers[position] else
                        mCreatePage(mData!![position]).apply { mPagers.add(this) }

                    container.addView(view)
                    return view
                }

                override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                    container.removeView(`object` as View)
                }
            }
        }
    }

    //初始化指示器
    private fun initIndicator() {
        if (null == mIndicators) {
            mIndicators = DefaultIndicator(context)
        }

        mIndicators?.apply {
            this@Banner.addView(this.rootView() , this.indicatorParams())
        }
    }

    private fun setIndicator() {
        mIndicators?.setCount(mData?.count() ?: 0)
    }

    /**
     * =========================================   手势处理    =====================================
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (mIsAutoPlay) {
            when(ev?.action) {
                MotionEvent.ACTION_DOWN -> stopAutoPlay()

                MotionEvent.ACTION_UP -> startAutoPlay()
                MotionEvent.ACTION_CANCEL -> startAutoPlay()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * ==========================================   轮播      ======================================
     */
    fun startAutoPlay() {
        if(mIsAutoPlay && mData?.isNotEmpty() == true && mDisposable?.isDisposed != false) {
            mDisposable = Observable.interval(mDelayTime, mDelayTime, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        nextPage()
                    }
        }
        log("startAutoPlay")
    }

    fun stopAutoPlay() {
        if (mDisposable?.isDisposed == false) {
            mDisposable?.dispose()
            mDisposable = null
        }
        log("stopAutoPlay")
    }

    private fun nextPage() {
        var position = (mCurPosition + 1) % mData!!.size
        mViewPager?.setCurrentItem(position , position != 0)
    }
    /**
     * =========================================    生命周期    =====================================
     */
//    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
//        super.onWindowFocusChanged(hasWindowFocus)
//        if (hasWindowFocus) {
//            startAutoPlay()
//        } else {
//            stopAutoPlay()
//        }
//    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == View.VISIBLE) {
            startAutoPlay()
        }  else {
            stopAutoPlay()
        }
        log("onWindowVisibilityChanged  visibility = $visibility")
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAutoPlay()
        log("onDetachedFromWindow")
    }


    //log
    private fun log(msg: String) {
        if (mEnableLog)
        Log.i(tag , msg)
    }
}