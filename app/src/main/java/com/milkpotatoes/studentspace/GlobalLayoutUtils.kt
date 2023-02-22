package com.milkpotatoes.studentspace

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.FrameLayout
import kotlin.math.min


class GlobalLayoutUtils(activity: Activity, private var isImmersed: Boolean = true) {

    // 当前界面根布局，就是我们设置的 setContentView()
    private var mChildOfContent: View
    private var frameLayoutParams: FrameLayout.LayoutParams

    // 变化前的试图高度
    private var usableHeightPrevious = 0
    private val activity: Activity = activity

    init {
        val content: FrameLayout = activity.findViewById(android.R.id.content)
        mChildOfContent = content.getChildAt(0)

        // 添加布局变化监听
        mChildOfContent.viewTreeObserver.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
        frameLayoutParams = mChildOfContent.layoutParams as FrameLayout.LayoutParams
    }

    fun onKeyBoardOpen() {}
    fun onKeyBoardClose() {}

    private fun possiblyResizeChildOfContent(activity: Activity) {

        // 当前可视区域的高度
        val usableHeightNow = computeUsableHeight()
        // 当前高度值和之前的进行对比，变化将进行重绘
        if (usableHeightNow != usableHeightPrevious) {
            // 获取当前屏幕高度
            // Ps：并不是真正的屏幕高度，是当前app的窗口高度，分屏时的高度为分屏窗口高度
            var usableHeightSansKeyboard = mChildOfContent.rootView.height
            // 高度差值：屏幕高度 - 可视内容高度
            var heightDifference = usableHeightSansKeyboard - usableHeightNow
            // 差值为负，说明获取屏幕高度时出错，宽高状态值反了，重新计算
            if (heightDifference < 0) {
                usableHeightSansKeyboard = mChildOfContent.rootView.width
                heightDifference = usableHeightSansKeyboard - usableHeightNow
            }

            when {
                // keyboard probably just became visible
                // 如果差值大于屏幕高度的 1/4，则认为输入软键盘为弹出状态
                heightDifference > usableHeightSansKeyboard / 4 ->
                    // 设置布局高度为：屏幕高度 - 高度差
                {
                    onKeyBoardOpen()
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference
                }
                // keyboard probably just became hidden
                // 虚拟导航栏显示
                heightDifference >= getNavigationBarHeight(activity) -> {
                    frameLayoutParams.height = usableHeightSansKeyboard
                    onKeyBoardClose()
                }
                // 其他情况直接设置为可视高度即可
                else -> {
                    onKeyBoardClose()
                    frameLayoutParams.height = usableHeightNow
                }
            }
        }

        // 刷新布局，会重新测量、绘制
        mChildOfContent.requestLayout()
        // 保存高度信息
        usableHeightPrevious = usableHeightNow

    }

    /**
     * 获取可视内容区域的高度
     */
    @Suppress("DEPRECATION")
    @SuppressLint("ServiceCast")
    private fun computeUsableHeight(): Int {
        val r = Rect()
        // 当前窗口可视区域，不包括通知栏、导航栏、输入键盘区域
        mChildOfContent.getWindowVisibleDisplayFrame(r)
        var usableHeight: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val b = activity.windowManager.currentWindowMetrics.bounds
            b.bottom - b.top
        } else {
            val dm = DisplayMetrics()
            activity.windowManager.defaultDisplay.getRealMetrics(dm)
            dm.heightPixels
        }
        val navigationBarHeight = getNavigationBarHeight(activity)
        return min(
            usableHeight + navigationBarHeight, if (isImmersed) {
                // 沉浸模式下，底部坐标就是内容有效高度
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (activity.isInMultiWindowMode) {
                        r.bottom - r.top
                    } else r.bottom + navigationBarHeight
                } else r.bottom + navigationBarHeight
            } else {
                // 非沉浸模式下，去掉通知栏的高度 r.top（可用于通知栏高度的计算）
                r.bottom - r.top + navigationBarHeight
            }
        )
//        return usableHeight
    }

    // 获取导航栏真实的高度（可能未显示）
    private fun getNavigationBarHeight(context: Context): Int {
        var result = 0
        val resources = context.resources
        val resourceId =
            resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}