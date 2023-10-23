package com.moaview.moaview.view.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.moaview.moaview.R
import com.google.android.material.appbar.MaterialToolbar

class CommonDialogFragment(
    private val layoutResId: Int,
    val onReady: (View) -> Unit,
    val onClick: (View, View) -> Unit
) : DialogFragment() {

    lateinit var dialogView: View
    var path = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }


    override fun onDestroyView() {
        if (dialog != null && retainInstance) {
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialogView = inflater.inflate(layoutResId, container, false)
        return dialogView
    }

    override fun onResume() {
        super.onResume()
        // full Screen code

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        if (layoutResId == com.moaview.moaview.R.layout.dialog_loading) {
            // setCanceledOnTouchOutside(false)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } else {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            if (layoutResId == com.moaview.moaview.R.layout.dialog_detail_image) {
                var sendButton =
                    dialogView.findViewById<TextView>(com.moaview.moaview.R.id.sendButton)
                sendButton.setOnClickListener {
                    onClick(dialogView, sendButton)
                    dismiss()
                }
                var backButton =
                    dialogView.findViewById<ImageView>(com.moaview.moaview.R.id.backButton)
                backButton.setOnClickListener {
                    dismiss()
                }
            } else if (layoutResId == R.layout.business_information) {
                var topAppBar = dialogView.findViewById<MaterialToolbar>(R.id.topAppBar)
                topAppBar.setNavigationOnClickListener {
                    dismiss()
                }
            }
            var topAppBar = dialogView.findViewById<MaterialToolbar>(R.id.topAppBar)
            topAppBar.setNavigationOnClickListener {
                dismiss()
            }
        }

        onReady(dialogView)
    }
}