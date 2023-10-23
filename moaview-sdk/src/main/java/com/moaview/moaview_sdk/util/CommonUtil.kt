package com.moaview.moaview_sdk.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.moaview.moaview_sdk.data.BookMark
import com.moaview.moaview_sdk.data.BookMarkList
import com.moaview.moaview_sdk.common.PageTurnSettingType
import com.moaview.moaview_sdk.common.TouchArea
import com.moaview.moaview_sdk.view.activity.ViewerActivity
import com.moaview.moaview_sdk.view.activity.ViewerActivity.Companion.bookMarkDataList
import com.moaview.moaview_sdk.view.activity.ViewerActivity.Companion.rootPath
import com.google.gson.Gson
import com.moaview.moaview_sdk.R
import com.moaview.moaview_sdk.common.BOOKMARK_FILE_NAME
import com.moaview.moaview_sdk.common.FILE_FORMAT_JPG
import com.moaview.moaview_sdk.common.FILE_FORMAT_PNG
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.PrintWriter
import java.util.*


object CommonUtil {

    /** File Util */
    fun writeBookMarkFile() {
        try {
            PrintWriter(FileWriter(File(rootPath, BOOKMARK_FILE_NAME), false))
                .use {
                    val gson = Gson()
                    val jsonString = gson.toJson(
                        BookMarkList(
                            bookMarkDataList,
                            ViewerActivity.bookData.currentPage
                        )
                    )
                    it.write(jsonString)
                    it.close()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun writeJsonToFile(file: File, data: Any) { // TODO ssin :: 실제 파일에 데이터를 쓰는 용도로만 사용
        try {
            PrintWriter(FileWriter(file, false))
                .use {
                    val gson = Gson()
                    val jsonString = gson.toJson(data)
                    it.write(jsonString)
                    it.close()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getChildImagePathList(path: String): ArrayList<String> {
        var fileList = ArrayList<String>()
        var file = File(path)
        if (file.exists()) {
            var filelist = file.listFiles()    // TODO ssin :: 하위 폴더에 폴더 내 이미지 가져와짐??
            Arrays.sort(filelist)
            for (i in filelist.indices) {
                if (filelist[i]!!.isFile) {
                    if(filelist[i]!!.name.endsWith(FILE_FORMAT_JPG, true) ||
                        filelist[i]!!.name.endsWith(FILE_FORMAT_PNG, true)) {
                        fileList.add(filelist[i]!!.path)
                    }
                } else if (filelist[i]!!.isDirectory) {
                    fileList += getChildImagePathList(filelist[i]!!.path) // 재귀함수 호출
                }
            }
        }
        return fileList
    }

    fun sortBookMarkList( // TODO ssin :: 공통함수로 만들어 쓰기 - 북마크 외 조건도 받도록
        list: ArrayList<BookMark>
    ): ArrayList<BookMark> {
        val comparator: Comparator<BookMark> = compareBy {
            Date(it.bookmark_date)
        }
        var reversedList = list.sortedWith(comparator).reversed()
        var reversedArrayList = ArrayList<BookMark>()
        for (i in reversedList) {
            reversedArrayList.add(i)
        }
        return reversedArrayList
    }

    /** View Util */
    fun dialogFullScreen(dialog: Dialog) {
        dialog.actionBar?.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog.window?.setDecorFitsSystemWindows(false)
            val controller = dialog.window?.insetsController
            if (controller != null) {
                if (ViewerActivity.settingData.hideSoftKey) {
                    controller.hide(
                        WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
                    )
                } else {
                    controller.hide(WindowInsets.Type.statusBars())
                    controller.show(WindowInsets.Type.navigationBars())
                }
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            dialog.window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
            dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            dialog.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    fun setFullScreen(activity: AppCompatActivity) {
        activity.supportActionBar?.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(false)
            val controller = activity.window.insetsController
            if (controller != null) {
                if (ViewerActivity.settingData.hideSoftKey) {
                    controller.hide(
                        WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
                    )
                } else {
                    activity.window.setDecorFitsSystemWindows(true)
                    controller.hide(WindowInsets.Type.statusBars())
                    controller.show(WindowInsets.Type.navigationBars())
                }
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    fun setBrightness(value: Float, window: Window) {   // TODO ssin :: 시스템이랑 합치기 - 파라미터로 분기
        var params = window.attributes
        params.screenBrightness = value / 100
        window.attributes = params
    }

    fun setSystemBrightness(window: Window) {
        var params = window.attributes
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = params
    }

    fun showDialog(
        context: Context,
        titleString: String,
        messageString: String,
        positiveString: String,
        negativeString: String?,
        positiveCallback: () -> Unit,
        negativeCallback: () -> Unit
    ) {
        var builder = AlertDialog.Builder(context)

        if (titleString != null || titleString != "") {
            builder.setTitle(titleString)
        }

        if (messageString != null || messageString != "") {
            builder.setMessage(messageString)
        }

        if (positiveString != null || positiveString != "") {
            builder.setPositiveButton(positiveString, object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    positiveCallback()
                }
            })
        }
        if (negativeString != null || negativeString != "") {
            builder.setNegativeButton(negativeString, object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    negativeCallback()
                }
            })
        }

        var alertDialog = builder.create()
        alertDialog.show()
        val nbutton: Button = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        nbutton.setTextColor(ContextCompat.getColor(context!!, R.color.black))
        val pbutton: Button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        pbutton.setTextColor(ContextCompat.getColor(context!!, R.color.black))
    }

    fun hideKeyBoard(context: Context, view: View) {

        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)

    }

    fun showKeyBoard(activity: Activity, editText: EditText) {
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        val imm: InputMethodManager? = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(editText, 0)
        editText.setSelection(editText.text.length)
    }

    fun showCustomDialog(
        context: Context,
        title: String,
        leftButtonContent: String,
        rightButtonContent: String,
        leftButtonClickListener: () -> Unit,
        rightButtonClickListener: () -> Unit,
        visibleCloseButton: Boolean
    ) {
        val dialog = Dialog(context)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 백그라운드 컬러 투명 (이걸 해줘야 background가 설정해준 모양으로 변함)
        dialog.setContentView(R.layout.last_page_dialog)
        dialog.findViewById<TextView>(R.id.titleTextView).text = title
        dialog.findViewById<LinearLayout>(R.id.closeButton).apply {
            if(!visibleCloseButton){
                visibility = View.INVISIBLE
            }
        }.setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.leftButton).apply {
            text = leftButtonContent
            setBackgroundResource(R.drawable.corner_radius_50_background)
            backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.color_e1e1e1))
        }.setOnClickListener {
            leftButtonClickListener()
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.rightButton).apply {
            text = rightButtonContent
            setBackgroundResource(R.drawable.corner_radius_50_background)
            backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.main_color))

        }.setOnClickListener {
            rightButtonClickListener()
            dialog.dismiss()
        }
        dialog.show()
    }

    fun getTouchArea(
        x: Float,
        y: Float,
        viewWidth: Int,
        viewHeight: Int,
        twoPageModeInLandscape: Boolean,
        pageTurnSettingType: PageTurnSettingType
    ): TouchArea {

        if(x >= viewWidth-(viewWidth/7) && y <= viewHeight/7) {   // 오른쪽 북마크 영역
            return TouchArea.BOOKMARK_RIGHT
        }

        if(twoPageModeInLandscape && x <= (viewWidth/7) && y <= viewHeight/7) {     // 왼쪽 북마크 영역
            return TouchArea.BOOKMARK_LEFT
        }

        when(pageTurnSettingType) {
            PageTurnSettingType.BEFORE_NEXT, PageTurnSettingType.NEXT_NEXT, PageTurnSettingType.NEXT_BEFORE -> {
                if(x < viewWidth/3){
                    return TouchArea.LEFT
                } else if(x > viewWidth/3*2) {
                    return TouchArea.RIGHT
                }
            }
            PageTurnSettingType.TOP_DOWN_NEXT_NEXT, PageTurnSettingType.TOP_DOWN_BEFORE_NEXT -> {
                if(y < viewHeight/3){
                    return TouchArea.TOP
                } else if(y > viewHeight/3*2) {
                    return TouchArea.BOTTOM
                }
            }
        }

        return TouchArea.CENTER
    }

     fun pathToBitmap(path: String?): Bitmap? {
        return try {
            val f = File(path)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            BitmapFactory.decodeStream(FileInputStream(f), null, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun showToastButton(view:View) {

        view.visibility = View.VISIBLE
        var fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        fadeIn.duration = 1500

        if (!fadeIn.isStarted) {    // TODO ssin :: isStarted는 왜필요?
            fadeIn.start()
        }

        fadeIn.addListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(p0: Animator) {
                val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
                fadeOut.duration = 1500
                fadeOut.start()            }

            override fun onAnimationEnd(p0: Animator) {
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })
    }

}