package com.moaview.moaview_sdk.view.fragment

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.moaview.moaview_sdk.R
import com.moaview.moaview_sdk.data.BookMark
import com.moaview.moaview_sdk.common.memoCallback
import com.moaview.moaview_sdk.databinding.FragmentMemoPopUpDialogBinding
import com.moaview.moaview_sdk.util.CommonUtil
import com.moaview.moaview_sdk.util.CommonUtil.hideKeyBoard
import com.moaview.moaview_sdk.view.activity.ViewerActivity
import com.moaview.moaview_sdk.view.activity.ViewerActivity.Companion.memoPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 메모 작성
 * */
class MemoPopUpDialogFragment : DialogFragment() {

    companion object {
        private const val MAX_LENGTH_MEMO = 500
    }

    var onMemoCallback: memoCallback? = null
    var page: Int = 1
    private lateinit var binding: FragmentMemoPopUpDialogBinding
    private lateinit var bookMarkData: BookMark
    private var originMemoText = ""
    private var isMemoContentsSave = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
        page = memoPage
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemoPopUpDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { hideKeyBoard(it, view) }
        dialog?.let { CommonUtil.dialogFullScreen(it) }
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        bookMarkData = ViewerActivity.bookMarkDataList.find { it.bookmark_page == page }!!
        binding.currentPage.text = bookMarkData.bookmark_page.toString() + "P"
        binding.pageCountTextView.text = String.format(
            getString(R.string.text_count),
            binding.contentTextView.text.length,
            MAX_LENGTH_MEMO
        )

        binding.commonAppBar.title.text = "메모"
        binding.commonAppBar.backButton.setOnClickListener {
            if (!isMemoContentsSave) {
                CommonUtil.showCustomDialog(
                    requireContext(),
                    getString(R.string.dialog_memo_update),
                    getString(R.string.cancel),
                    getString(R.string.save),
                    {
                        onMemoCallback?.onClickCallBack(binding.commonAppBar.topAppBar)
                        dismiss()
                    },
                    {
                        ViewerActivity.bookMarkDataList.find { it.bookmark_page == page }?.bookmark_memo =
                            binding.contentTextView.text.toString()
                        CommonUtil.writeBookMarkFile()
                        onMemoCallback?.onClickCallBack(binding.commonAppBar.topAppBar)
                        dismiss()
                    },
                    true
                )
            } else {
                onMemoCallback?.onClickCallBack(binding.commonAppBar.topAppBar)
                dismiss()
            }
        }

        Glide.with(requireContext())
            .load(ViewerActivity.metaDataList[page - 1].path)
            .into(binding.imageView)

        binding.contentTextView.apply {
            originMemoText = bookMarkData.bookmark_memo
            if (originMemoText.isNotEmpty()) {
                setText(originMemoText)
                binding.removeButton.visibility = View.VISIBLE
            }
            onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    requestFocus()
                    CoroutineScope(Dispatchers.Main).launch {
                        setSelection(text.length)
                    }
                } else {
                    hideKeyBoard(context, view)
                }
            }
        }

        binding.removeButton.setOnClickListener {
            requireContext()?.let { _context ->
                CommonUtil.showCustomDialog(
                    _context,
                    getString(R.string.dialog_exist_memo_delete),
                    getString(R.string.no),
                    getString(R.string.yes),
                    {},
                    {
                        setEditTextLayout()
                        ViewerActivity.bookMarkDataList.find { it.bookmark_page == page }?.bookmark_memo =
                            ""
                        CommonUtil.writeBookMarkFile()
                        isMemoContentsSave = true
                        binding.contentTextView.setText("")
                        binding.removeButton.visibility = View.GONE
                    },
                    false
                )
            }
        }

        binding.buttonSave.setOnClickListener {
            ViewerActivity.bookMarkDataList.find { it.bookmark_page == page }?.bookmark_memo =
                binding.contentTextView.text.toString()

            ViewerActivity.bookMarkDataList.find { it.bookmark_page == page }?.apply {
                bookmark_memo = binding.contentTextView.text.toString()
                bookmark_date = System.currentTimeMillis()
            }
            CommonUtil.writeBookMarkFile()
            isMemoContentsSave = true
            setEditTextLayout()
            binding.buttonSave.visibility = View.GONE
            binding.removeButton.visibility = View.VISIBLE
        }

        binding.contentTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.contentTextView.text.isNotEmpty()) binding.buttonSave.visibility =
                    View.VISIBLE
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (binding.contentTextView.text.isEmpty()) {
                    binding.buttonSave.visibility = View.GONE
                    isMemoContentsSave = true
                } else {
                    binding.buttonSave.visibility = View.VISIBLE
                    isMemoContentsSave = false
                }
                binding.pageCountTextView.text =
                    binding.contentTextView.text.length.toString() + "/" + "500"
            }
        })
    }

    private fun setEditTextLayout() {
        binding.contentTextView.clearFocus()
        view?.let { context?.let { it1 -> hideKeyBoard(it1, it) } }
    }

    fun setMemoCallback(callback: memoCallback) {
        onMemoCallback = callback
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