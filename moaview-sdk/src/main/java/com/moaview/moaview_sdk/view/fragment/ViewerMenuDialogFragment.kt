package com.moaview.moaview_sdk.view.fragment

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import com.moaview.moaview_sdk.data.BookMark
import com.moaview.moaview_sdk.datastore.UserSettingDataStore
import com.moaview.moaview_sdk.common.memoCallback
import com.moaview.moaview_sdk.common.PageModeSettingType
import com.moaview.moaview_sdk.common.TouchArea
import com.moaview.moaview_sdk.view.activity.ViewerActivity.Companion.historyStack
import com.moaview.moaview_sdk.view.activity.ViewerActivity.Companion.memoPage
import com.moaview.moaview_sdk.util.CommonUtil
import com.moaview.moaview_sdk.view.activity.ViewerActivity
import com.google.android.material.slider.Slider
import com.moaview.moaview_sdk.R
import com.moaview.moaview_sdk.databinding.BottomSheetFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 뷰어 내 상/하단 메뉴
 * */
class ViewerMenuDialogFragment : DialogFragment() {

    private lateinit var binding: BottomSheetFragmentBinding
    private var currentBookMark: BookMark? = null
    var visibleBookMark = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetFragmentBinding.inflate(inflater, container, false)

        binding.commonAppBar.menuBookmark.visibility = View.VISIBLE
        binding.commonAppBar.menuNote.visibility = View.VISIBLE

        visibleBookMark = if(savedInstanceState != null){
            savedInstanceState.getBoolean("visibleBookMark")
        }else{
            var activity = (requireActivity() as ViewerActivity)
            activity.getCurrentVisibleBookMark(activity.getRecyclerViewItemPosition())
        }

        currentBookMark = ViewerActivity.bookMarkDataList.find { i ->
            i.bookmark_page == ViewerActivity.bookData.currentPage
        }
        getPageMode()
        if (historyStack.size == 0) {

            binding.reStartButton.visibility = View.INVISIBLE
            binding.reStartButtonInvisible.visibility = View.VISIBLE

        } else {
            binding.reStartButton.visibility = View.VISIBLE
            binding.reStartButtonInvisible.visibility = View.INVISIBLE
        }

        binding.reStartButton.setOnClickListener {
            var page = (requireActivity() as ViewerActivity).popHistoryStack()
            if (page != -1) {
                binding.currentPage.text = page.toString()
                binding.totalPage.text = "/${ViewerActivity.bookData.totalPage}"
                binding.slidebar.value = page.toFloat()
            }
        }

        binding.bottomNavigcation.run {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.ic_touch_bottom_sheet -> {
                        if (getPageMode()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                var settingsManager = context?.let { UserSettingDataStore(it) }
                                settingsManager?.setPageMode(PageModeSettingType.SCROLL)
                                getPageMode()
                            }
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                var settingsManager = context?.let { UserSettingDataStore(it) }
                                settingsManager?.setPageMode(PageModeSettingType.SLIDE)
                                getPageMode()
                            }
                        }
                    }
                    R.id.ic_bookmark_bottom_sheet -> {
                        BookMarkListDialogFragment().show(parentFragmentManager, null)
                    }
                    R.id.ic_format_bottom_sheet -> {
                        SettingBottomSheetDialogFragment().show(parentFragmentManager, null)
                    }
                    R.id.ic_settings_bottom_sheet -> {
                        ViewerSettingDialogFragment().show(parentFragmentManager, null)
                    }
                }
                dismiss()
                true
            }
        }

        binding.commonAppBar.menuNote.setOnClickListener {

            if (currentBookMark == null) {
                binding.commonAppBar.menuBookmark.performClick()
            }
            currentBookMark = ViewerActivity.bookMarkDataList.find { i ->
                i.bookmark_page == ViewerActivity.bookData.currentPage
            }
            currentBookMark?.let { it1 ->
                var dialog = MemoPopUpDialogFragment()
                memoPage = it1.bookmark_page
                dialog.show(parentFragmentManager, null)

                var callback = object : memoCallback {
                    override fun onClickCallBack(view: View) {
                        /*           if (currentBookMark != null) {
                                       binding.commonAppBar.menuBookmark.setBackgroundResource(R.drawable.btn_bookmark_2)
                                       if (currentBookMark!!.bookmak_memo.isNotEmpty()) {
                                           binding.commonAppBar.menuNote.setBackgroundResource(R.drawable.btn_note_on)
                                       }

                                   } else {
                                       binding.commonAppBar.menuBookmark.setBackgroundResource(R.drawable.btn_bookmark)
                                       binding.commonAppBar.menuNote.setBackgroundResource(R.drawable.btn_note)
                                   }        */
                    }
                }
                dialog.setMemoCallback(callback)
                dismiss()
            }
        }

        binding.commonAppBar.menuBookmark.setOnClickListener {
            setBookMark()
        }

        binding.commonAppBar.backButton.setOnClickListener {
            var intent = Intent()
            intent.putExtra("currentPage", ViewerActivity.bookData.currentPage)
            intent.putExtra("id", ViewerActivity.bookData.id)
            requireActivity().setResult(2003103, intent)
            (requireActivity() as ViewerActivity).onBackPressed()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.let { CommonUtil.dialogFullScreen(it) }

        binding.bottomNavigcation.itemIconTintList = null

        binding.outOfRealmView.setOnClickListener {
            dismiss()
        }
        binding.commonAppBar.title.text = ViewerActivity.bookData.title
        binding.slidebar.addOnChangeListener { slider, value, fromUser ->
            binding.currentPage.text = value.toInt().toString()
            binding.totalPage.text = "/${ViewerActivity.bookData.totalPage}"

        }
        CoroutineScope(Dispatchers.Main).launch {
            if (ViewerActivity.bookData.totalPage == 1) {
                binding.slidebar.valueFrom = 0f
            } else binding.slidebar.valueFrom = 1f
            binding.slidebar.valueTo = ViewerActivity.bookData.totalPage.toFloat()
            binding.slidebar.value = ViewerActivity.bookData.currentPage.toFloat()
            binding.currentPage.text = ViewerActivity.bookData.currentPage.toString()
            binding.totalPage.text = "/${ViewerActivity.bookData.totalPage}"
        }

        binding.slidebar.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                (requireActivity() as ViewerActivity).jumpPage(slider.value.toInt())
            }

        })


        if (visibleBookMark) {
            binding.commonAppBar.menuBookmark.setBackgroundResource(R.drawable.btn_bookmark_2)
            var bookMemo =
                ViewerActivity.bookMarkDataList.find { i -> i.bookmark_page == ViewerActivity.bookData.currentPage }
            if (bookMemo != null) {
                if (bookMemo.bookmark_memo.isNotEmpty()) {
                    binding.commonAppBar.menuNote.setBackgroundResource(R.drawable.btn_note_on)
                }
            }
        } else {
            binding.commonAppBar.menuBookmark.setBackgroundResource(R.drawable.btn_bookmark)
            binding.commonAppBar.menuNote.setBackgroundResource(R.drawable.btn_note)
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
            dialog.window?.setBackgroundDrawable(null)
            val windowParams: WindowManager.LayoutParams =
                dialog.window?.attributes!!
            windowParams.dimAmount = 0f
            windowParams.flags = windowParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dialog.window?.attributes = windowParams
        }

    }

    fun getPageMode(): Boolean {
        if (ViewerActivity.settingData.pageMode == PageModeSettingType.SLIDE.toString()) {
            binding.bottomNavigcation.menu.get(0).icon =
                context?.let { ContextCompat.getDrawable(it, R.drawable.ic_btm_05) }
            binding.bottomNavigcation.menu.get(0).title = "스크롤 보기"
            return true
        } else {
            binding.bottomNavigcation.menu.get(0).icon =
                context?.let { ContextCompat.getDrawable(it, R.drawable.ic_btm_01) }
            binding.bottomNavigcation.menu.get(0).title = "넘겨 보기"
            return false
        }
    }

    fun setVisibleHistoryButton(visible: Boolean) {
        if (visible) {
            binding.reStartButton.visibility = View.VISIBLE
            binding.reStartButtonInvisible.visibility = View.INVISIBLE
        } else {
            binding.reStartButton.visibility = View.INVISIBLE
            binding.reStartButtonInvisible.visibility = View.VISIBLE
        }
    }

    private fun setBookMark() {
        var activity = (requireActivity() as ViewerActivity)
        var bookMemo = ViewerActivity.bookMarkDataList.find { i -> i.bookmark_page == ViewerActivity.bookData.currentPage }
        if (ViewerActivity.ORIENTATION == Configuration.ORIENTATION_LANDSCAPE &&
            ViewerActivity.settingData.twoPageModeInOrientation &&
            ViewerActivity.settingData.pageMode == PageModeSettingType.SLIDE.toString()) {
            activity.setBookMark(TouchArea.BOOKMARK_LEFT)
        } else activity.setBookMark(TouchArea.BOOKMARK_RIGHT)
        dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var activity = (requireActivity() as ViewerActivity)
        visibleBookMark =  activity.getCurrentVisibleBookMark(activity.getRecyclerViewItemPosition())
        outState.putBoolean("visibleBookMark", visibleBookMark)
    }

}
