package com.moaview.moaview_sdk.util

import android.content.Context
import android.content.res.Configuration
import com.moaview.moaview_sdk.view.activity.ViewerActivity

object ScreenSize {

    fun getScreenWidth(context: Context): Int {     // TODO ssin :: context의 extension으로 만들어
        val display = context.resources?.displayMetrics
        val deviceWidth = display?.widthPixels
        var width = deviceWidth!!

        if (ViewerActivity.ORIENTATION == Configuration.ORIENTATION_LANDSCAPE){
            width = deviceWidth!!  + getnavigationBarHeight(context)
        }
        return width!!
    }

    fun getScreenHeight(context: Context): Int {    // TODO ssin :: context의 extension으로 만들어
        val display = context.resources?.displayMetrics
        val deviceHeight = display?.heightPixels
        var height = deviceHeight!!
        if (ViewerActivity.ORIENTATION == Configuration.ORIENTATION_PORTRAIT){
            height = deviceHeight!! + getStatusBarHeight(context) + getnavigationBarHeight(context)
        }
        return height
    }

    fun getnavigationBarHeight(context: Context):Int{
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        var navigationbarHeight = 0
        if (resourceId > 0) {
            navigationbarHeight = context.resources.getDimensionPixelSize(resourceId)
         return  navigationbarHeight
        }
        return 0
    }
    fun getStatusBarHeight(context: Context):Int{
        var statusbarHeight = 0
        val resourceId: Int = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusbarHeight = context.resources.getDimensionPixelSize(resourceId)
           return statusbarHeight
        }
        return 0
    }
}