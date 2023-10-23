package com.moaview.moaview.view.fragment

import android.os.Bundle
import android.view.View
import com.moaview.moaview.R
import com.moaview.moaview.databinding.FragmentOptionBinding
import com.moaview.moaview.view.common.CustomDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OptionFragment : BaseFragment<FragmentOptionBinding>(FragmentOptionBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // var version =  "현재버전: V.${BuildConfig.VERSION_NAME} 최신버전: V.${BuildConfig.VERSION_NAME}"

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.buisnessVersion.setOnClickListener {
            CommonDialogFragment(R.layout.business_information,{},{ dialogView, View ->}).show(requireActivity().supportFragmentManager, null)
        }

        binding.InfoVersion.setOnClickListener {
            context?.let { it1 -> CustomDialog(it1) }?.show()
        }
    }
}