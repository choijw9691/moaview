package com.moaview.moaview.view.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.moaview.moaview.R
import com.moaview.moaview.adapter.ViewPagerAdapter
import com.moaview.moaview.databinding.FragmentGuideDialogBinding
import com.moaview.moaview.datastore.SettingDataStore
import com.moaview.moaview.common.ViewPagerMultipleHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GuideDialogFragment  : DialogFragment() {

    private lateinit var binding: FragmentGuideDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGuideDialogBinding.inflate(inflater, container, false)
        if (dialog!=null){
            dialog!!.setCanceledOnTouchOutside(false)
        }
        binding.viewPager.adapter = ViewPagerAdapter(getDrawableList() as ArrayList<Unit>,
            ViewPagerMultipleHolder.GUIDE_VIEW_HOLDER
        )
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeButton.setOnClickListener {
            if (binding.checkButton.isChecked){
                CoroutineScope(Dispatchers.Main).launch {
                    var settingsManager = SettingDataStore(requireContext())
                    settingsManager.setTutorialState(true)
                    dismiss()
                }
            } else {
                dismiss()
            }
        }
    }

    private fun getDrawableList(): ArrayList<Int> {
        return arrayListOf(R.drawable.img_step_03, R.drawable.img_step_01, R.drawable.img_step_02)
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
}