package com.moaview.moaview.view.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import com.moaview.moaview.databinding.CustomDialogBinding

class CustomDialog (context : Context) : Dialog(context) {

    private lateinit var binding : CustomDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 만들어놓은 dialog_profile.xml 뷰를 띄운다.
        binding = CustomDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        binding.checkButton.setOnClickListener {
            dismiss()
        }
    }
}
