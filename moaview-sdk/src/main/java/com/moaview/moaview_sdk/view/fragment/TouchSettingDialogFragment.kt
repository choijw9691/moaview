package com.moaview.moaview_sdk.view.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import com.moaview.moaview_sdk.R
import com.moaview.moaview_sdk.datastore.UserSettingDataStore
import com.moaview.moaview_sdk.common.PageTurnSettingType
import com.moaview.moaview_sdk.databinding.FragmentPageTurnSettingDialogBinding
import com.moaview.moaview_sdk.util.CommonUtil
import com.moaview.moaview_sdk.view.activity.ViewerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 뷰어 터치 모드 설정
 * */
class TouchSettingDialogFragment : DialogFragment(), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    private lateinit var binding: FragmentPageTurnSettingDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPageTurnSettingDialogBinding.inflate(inflater, container, false)
        binding.radioPrevNext.setOnClickListener(this)
        binding.radioNextNext.setOnClickListener(this)
        binding.radioTopPrevBottomNext.setOnClickListener(this)
        binding.radioTopNextBottomNext.setOnClickListener(this)
        binding.radioLeft.setOnClickListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let { CommonUtil.dialogFullScreen(it) }
        initSetting()
        binding.commonAppBar.backButton.setOnClickListener {
            dismiss()
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
        }
    }

    fun initSetting() {
        when (ViewerActivity.settingData.pageTurnSettingType) {
            PageTurnSettingType.BEFORE_NEXT.toString() -> binding.radioPrevNext.isChecked = true
            PageTurnSettingType.NEXT_NEXT.toString() -> binding.radioNextNext.isChecked = true
            PageTurnSettingType.TOP_DOWN_BEFORE_NEXT.toString() -> binding.radioTopPrevBottomNext.isChecked =
                true
            PageTurnSettingType.TOP_DOWN_NEXT_NEXT.toString() -> binding.radioTopNextBottomNext.isChecked =
                true
            PageTurnSettingType.NEXT_BEFORE.toString() -> binding.radioLeft.isChecked = true
        }
    }


    override fun onClick(view: View?) {
        if (view is RadioButton) {
            if (binding.radioPrevNext.isChecked && view.id == R.id.radio_prev_next) {
                CoroutineScope(Dispatchers.Main).launch {
                    var settingsManager = context?.let { UserSettingDataStore(it) }
                    settingsManager?.setPageTurnSettingType(PageTurnSettingType.BEFORE_NEXT)
                }
            } else {
                binding.radioPrevNext.isChecked = false
            }

            if (binding.radioNextNext.isChecked && view.id == R.id.radio_next_next) {
                CoroutineScope(Dispatchers.Main).launch {
                    var settingsManager = context?.let { UserSettingDataStore(it) }
                    settingsManager?.setPageTurnSettingType(PageTurnSettingType.NEXT_NEXT)
                }
            } else {
                binding.radioNextNext.isChecked = false
            }

            if (binding.radioTopPrevBottomNext.isChecked && view.id == R.id.radio_top_prev_bottom_next) {
                CoroutineScope(Dispatchers.Main).launch {
                    var settingsManager = context?.let { UserSettingDataStore(it) }
                    settingsManager?.setPageTurnSettingType(PageTurnSettingType.TOP_DOWN_BEFORE_NEXT)
                }
            } else {
                binding.radioTopPrevBottomNext.isChecked = false
            }

            if (binding.radioTopNextBottomNext.isChecked && view.id == R.id.radio_top_next_bottom_next) {
                CoroutineScope(Dispatchers.Main).launch {
                    var settingsManager = context?.let { UserSettingDataStore(it) }
                    settingsManager?.setPageTurnSettingType(PageTurnSettingType.TOP_DOWN_NEXT_NEXT)
                }
            } else {
                binding.radioTopNextBottomNext.isChecked = false
            }

            if (binding.radioLeft.isChecked && view.id == R.id.radio_left) {
                CoroutineScope(Dispatchers.Main).launch {
                    var settingsManager = context?.let { UserSettingDataStore(it) }
                    settingsManager?.setPageTurnSettingType(PageTurnSettingType.NEXT_BEFORE)
                }
            } else {
                binding.radioLeft.isChecked = false
            }
        }
    }
}