package com.moaview.moaview_sdk.view.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.*
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import com.moaview.moaview_sdk.datastore.UserSettingDataStore
import com.moaview.moaview_sdk.util.CommonUtil
import com.moaview.moaview_sdk.common.doOnApplyWindowInsets
import com.moaview.moaview_sdk.view.activity.ViewerActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.Slider
import com.moaview.moaview_sdk.R
import com.moaview.moaview_sdk.databinding.FragmentViewSettingDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 뷰어 내 하단 설정 메뉴
 * */
class SettingBottomSheetDialogFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentViewSettingDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewSettingDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let { CommonUtil.dialogFullScreen(it) }

        binding.layoutBottomSetting?.doOnApplyWindowInsets { insetView, windowInsets, initialPadding, _ ->
            insetView.updatePadding(
                bottom = initialPadding.bottom + windowInsets.getInsets(systemBars() or ime()).bottom
            )
        }

        if (ViewerActivity.settingData.systemBrightNessCheck) {
            binding.brightCheckBox.isChecked = true
            binding.slidebar.value = ViewerActivity.settingData.appBrightNessValue
            binding.slidebar.isEnabled = false
            binding.slidebar.trackActiveTintList =
                context?.let { ContextCompat.getColorStateList(it, R.color.main_color) }!!

        } else {
            binding.brightCheckBox.isChecked = false
            binding.slidebar.value = ViewerActivity.settingData.appBrightNessValue
            binding.slidebar.isEnabled = true
            binding.slidebar.trackActiveTintList =
                context?.let { ContextCompat.getColorStateList(it, R.color.main_color) }!!
        }

        binding.radioGroup[ViewerActivity.settingData.colorType].foreground =
            ResourcesCompat.getDrawable(resources, R.drawable.check_round, null)

        binding.backButton.setOnClickListener {
            dismiss()
        }

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, id ->
            for (i in 0 until radioGroup.childCount) {
                if (radioGroup.checkedRadioButtonId == radioGroup[i].id) {
                    radioGroup[i].foreground =
                        ResourcesCompat.getDrawable(resources, R.drawable.check_round, null)
                    CoroutineScope(Dispatchers.Main).launch {
                        var settingsManager = context?.let { UserSettingDataStore(it) }
                        settingsManager?.setPageColorType(i)
                    }
                } else {
                    radioGroup[i].foreground = null
                }
            }
        }



        binding.slidebar.apply {
            addOnChangeListener { slider, value, fromUser ->
                activity?.window?.let { CommonUtil.setBrightness(value, it) }
            }
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}

                override fun onStopTrackingTouch(slider: Slider) {
                    CoroutineScope(Dispatchers.Main).launch {
                        var settingsManager = context?.let { UserSettingDataStore(it) }
                        settingsManager?.setAppBrightNessValue(slider.value)
                    }
                }
            })

            binding.slidebarContainer?.setOnClickListener {
                if (!binding.slidebar.isEnabled) {
                    binding.toastButton?.let { it1 -> CommonUtil.showToastButton(it1) }
                }
            }
        }


        binding.brightCheckBox.setOnCheckedChangeListener { compoundButton, ischeck ->
            if (ischeck) {
                activity?.window?.let { CommonUtil.setSystemBrightness(it) }
                binding.slidebar.isEnabled = false
            } else {
                activity?.window?.let {
                    CommonUtil.setBrightness(
                        ViewerActivity.settingData.appBrightNessValue,
                        it
                    )
                }
                binding.slidebar.isEnabled = true
                binding.slidebar.trackActiveTintList =
                    context?.let { ContextCompat.getColorStateList(it, R.color.main_color) }!!
            }
            CoroutineScope(Dispatchers.Main).launch {
                var settingsManager = context?.let { UserSettingDataStore(it) }
                settingsManager?.setSystemBrightNessCheck(ischeck)
            }
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
            val windowParams: WindowManager.LayoutParams = dialog.window?.attributes!!
            windowParams.dimAmount = 0f
            windowParams.flags = windowParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dialog.window?.attributes = windowParams
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        object : BottomSheetDialog(requireContext(), theme) {
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()

                window?.let {
                    WindowCompat.setDecorFitsSystemWindows(it, false)
                }

                findViewById<View>(com.google.android.material.R.id.container)?.apply {
                    fitsSystemWindows = false
                    doOnApplyWindowInsets { insetView, windowInsets, _, initialMargins ->
                        insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            updateMargins(
                                top = initialMargins.top + windowInsets.getInsets(systemBars()).top
                            )
                        }
                    }
                }
                findViewById<View>(com.google.android.material.R.id.coordinator)?.fitsSystemWindows =
                    false
            }
        }
}