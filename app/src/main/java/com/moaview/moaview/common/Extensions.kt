package com.moaview.moaview.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.moaview.moaview.view.fragment.BookListFragment
import java.text.SimpleDateFormat

fun View.setSelectedColor(textViewList: Array<TextView>, imageViewList:Array<ImageView>?, selectedColor: Int, nonSelectedColor: Int):Int? {
    var selectedPosition=0  // TODO ssin :: return 받아 쓰는 부분 다른거로 처리해
    for (position in textViewList.indices) {
        if (textViewList[position] == this) {

            textViewList[position].setTextColor(selectedColor)
            if (imageViewList != null){
                imageViewList[position].backgroundTintList = ColorStateList.valueOf(selectedColor)
                imageViewList[position].setColorFilter(selectedColor)
            }
            selectedPosition = position
        } else {
            textViewList[position].setTextColor(nonSelectedColor)
            if (imageViewList != null) {
                imageViewList[position].backgroundTintList = ColorStateList.valueOf(nonSelectedColor)
                imageViewList[position].setColorFilter(nonSelectedColor)
            }
        }
    }
    return selectedPosition
}

fun Long.convertDate(formatter: SimpleDateFormat) : String = formatter.format(this)

fun String.getFileExtension() : String = substring(this.lastIndexOf(".") + 1)

fun String.showToast(context: Context, length: Int) = Toast.makeText(context, this, length).show()

fun RecyclerView.setItemDecoration(removeDecoration: RecyclerView.ItemDecoration, addDecoration: RecyclerView.ItemDecoration){
    this.removeItemDecoration(removeDecoration)
    this.removeItemDecoration(addDecoration)

    this.addItemDecoration(addDecoration)
}

fun Context.dpToPx(dp: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()

fun Context.quickPopup(toView: View, fnSetup: (() -> Unit) -> BookListFragment.PopupInfo) {
    fun makePopupClosure(
        toView: View,
        fnSetup: (() -> Unit) -> BookListFragment.PopupInfo
    ): () -> Unit {
        var pop: PopupWindow? = null
        fun dismiss() = pop?.dismiss()
        return {
            val popInfo = fnSetup(::dismiss)
            val width = if (popInfo.width == 0) {
                ViewGroup.LayoutParams.WRAP_CONTENT
            } else {
                dpToPx(popInfo.width)
            }

            val height = if (popInfo.height == 0) {
                ViewGroup.LayoutParams.WRAP_CONTENT
            } else {
                dpToPx(popInfo.height)
            }

            pop = PopupWindow(
                popInfo.v,
                width,
                height,
                true
            ).apply {
                showAsDropDown(toView, dpToPx(popInfo.x), dpToPx(popInfo.y))
                elevation = 10f
            }
        }
    }

    makePopupClosure(toView) { dismiss ->
        fnSetup(dismiss)
    }.apply {
        this()
    }
}

fun Activity.start(destination: Class<out Activity>) {
    val intent = Intent(this, destination)
    startActivity(intent)
}