package com.moaview.moaview_sdk.view.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.moaview.moaview_sdk.R
import com.moaview.moaview_sdk.data.ViewerSettingData
import com.moaview.moaview_sdk.datastore.UserSettingDataStore
import com.moaview.moaview_sdk.common.PageModeSettingType
import com.moaview.moaview_sdk.common.PageTurnSettingType
import com.moaview.moaview_sdk.databinding.FragmentOptionDialogBinding
import com.moaview.moaview_sdk.util.CommonUtil
import com.moaview.moaview_sdk.view.activity.ViewerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 뷰어 설정
 * */
class ViewerSettingDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //false로 설정해 주면 화면밖 혹은 뒤로가기 버튼시 다이얼로그라 dismiss 되지 않는다.
        isCancelable = true
    }

    private lateinit var binding: FragmentOptionDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOptionDialogBinding.inflate(inflater, container, false)
        binding.pageTurnSettingTypeContainer.setOnClickListener {
            TouchSettingDialogFragment().show(parentFragmentManager, null)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let { CommonUtil.dialogFullScreen(it) }
        initSetting()
        binding.twoPageModeInOrientationSwitchBtn.setOnCheckedChangeListener { compoundButton, isChecked ->
            CoroutineScope(Dispatchers.Main).launch {
                var settingsManager = context?.let { UserSettingDataStore(it) }
                settingsManager?.setTwoPageModeInOrientation(isChecked)
            }
        }
        binding.twoPageModeInFirstPageSwitchBtn.setOnCheckedChangeListener { compoundButton, isChecked ->
            CoroutineScope(Dispatchers.Main).launch {
                var settingsManager = context?.let { UserSettingDataStore(it) }
                settingsManager?.setTwoPageModeInFirstPage(isChecked)





            }
        }
        binding.hideSoftKeySwitchBtn.setOnCheckedChangeListener { compoundButton, isChecked ->
            CoroutineScope(Dispatchers.Main).launch {
                var settingsManager = context?.let { UserSettingDataStore(it) }
                settingsManager?.setHideSoftKey(isChecked)
            }
        }
        binding.screenAlwaysOnSwitchBtn.setOnCheckedChangeListener { compoundButton, isChecked ->
            CoroutineScope(Dispatchers.Main).launch {
                var settingsManager = context?.let { UserSettingDataStore(it) }
                settingsManager?.setScreenAlwaysOn(isChecked)
            }
        }
        binding.pageTurnVolumeKeySwitchBtn.setOnCheckedChangeListener { compoundButton, isChecked ->
            CoroutineScope(Dispatchers.Main).launch {
                var settingsManager = context?.let { UserSettingDataStore(it) }
                settingsManager?.setPageTurnVolumeKey(isChecked)
            }
        }
        binding.fixedScreenRotationSwitchBtn.setOnCheckedChangeListener { compoundButton, isChecked ->
            CoroutineScope(Dispatchers.Main).launch {
                var settingsManager = context?.let { UserSettingDataStore(it) }
                settingsManager?.setFixedScreenRotation(isChecked)
            }
        }

        binding.resetButton.setOnClickListener {

            context?.let { it1 ->
                CommonUtil.showCustomDialog(
                    it1,
                    getString(R.string.reset_title),
                    getString(R.string.no),
                    getString(R.string.yes),
                    {
                    },
                    {

                        CoroutineScope(Dispatchers.Main).launch {
                            var settingsManager = context?.let { UserSettingDataStore(it) }
                            settingsManager?.setPageMode(PageModeSettingType.SLIDE)
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            var settingsManager = context?.let { UserSettingDataStore(it) }
                            settingsManager?.setPageTurnSettingType(PageTurnSettingType.BEFORE_NEXT)
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            var settingsManager = context?.let { UserSettingDataStore(it) }
                            settingsManager?.setTwoPageModeInOrientation(false)
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            var settingsManager = context?.let { UserSettingDataStore(it) }
                            settingsManager?.setPageTurnVolumeKey(true)
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            var settingsManager = context?.let { UserSettingDataStore(it) }
                            settingsManager?.setTwoPageModeInFirstPage(true)
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            var settingsManager = context?.let { UserSettingDataStore(it) }
                            settingsManager?.setHideSoftKey(true)
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            var settingsManager = context?.let { UserSettingDataStore(it) }
                            settingsManager?.setScreenAlwaysOn(false)
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            var settingsManager = context?.let { UserSettingDataStore(it) }
                            settingsManager?.setFixedScreenRotation(false)
                        }
                        ViewerActivity.settingData = ViewerSettingData()
                        initSetting()
                    }, true
                )
            }

        }
        binding.commonAppBar.backButton.setOnClickListener{
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
        binding.twoPageModeInOrientationSwitchBtn.isChecked =
            ViewerActivity.settingData.twoPageModeInOrientation
        binding.twoPageModeInFirstPageSwitchBtn.isChecked =
            ViewerActivity.settingData.twoPageModeInFirstPage
        binding.hideSoftKeySwitchBtn.isChecked = ViewerActivity.settingData.hideSoftKey
        binding.screenAlwaysOnSwitchBtn.isChecked = ViewerActivity.settingData.screenAlwaysOn
        binding.pageTurnVolumeKeySwitchBtn.isChecked = ViewerActivity.settingData.pageTurnVolumeKey
        binding.fixedScreenRotationSwitchBtn.isChecked = ViewerActivity.settingData.fixedScreenRotation
    }
}