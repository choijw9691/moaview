package com.moaview.moaview_sdk.view.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.moaview.moaview_sdk.R
import com.moaview.moaview_sdk.adapter.BookMarkListAdapter
import com.moaview.moaview_sdk.common.memoCallback
import com.moaview.moaview_sdk.databinding.FragmentBookMarkListDialogBinding
import com.moaview.moaview_sdk.util.CommonUtil
import com.moaview.moaview_sdk.view.activity.ViewerActivity
import com.moaview.moaview_sdk.view.activity.ViewerActivity.Companion.memoPage

/**
 * 독서 노트
 * */
class BookMarkListDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentBookMarkListDialogBinding
    lateinit var adapter: BookMarkListAdapter

    var isEditMode = false
    var isAllCheck = false

    var selectCheckBoxPosition = HashMap<Int, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookMarkListDialogBinding.inflate(inflater, container, false)
        binding.commonAppBar.title.text = ViewerActivity.bookData.title
        binding.commonAppBar.backButton.setOnClickListener {
            dismiss()
        }

        setBookmarkListUi()

        binding.editButton.setOnClickListener {
            hideSwitchViews(true)
            binding.choiceBookTextView.text = String.format(getString(R.string.select_info), 0)
        }

        binding.buttonCancel.setOnClickListener {
            isAllCheck = false
            hideSwitchViews(false)
        }

        binding.buttonSelectAll.setOnClickListener {
            if (isAllCheck) {
                isAllCheck = false
                adapter.allCheck(false)
                binding.buttonSelectAll.text = getString(R.string.select_all)
                binding.removeButton.visibility = View.GONE
            } else {
                isAllCheck = true
                adapter.allCheck(true)
                binding.buttonSelectAll.text = getString(R.string.deselect_all)
                binding.removeButton.visibility = View.VISIBLE
                var size = ViewerActivity.bookMarkDataList.size
                for (i in 0 until size!!) {
                    selectCheckBoxPosition[ViewerActivity.bookMarkDataList[i].bookmark_page] = true
                }
            }
            binding.choiceBookTextView.text =
                String.format(getString(R.string.select_info), selectCheckBoxPosition.size)
        }

        binding.removeButton.setOnClickListener {

            context?.let {

                if (isExistMemo(selectCheckBoxPosition)) {   // 다중 삭제 시 메모가 있는 항목이 있는 경우 컨펌팝업
                    CommonUtil.showCustomDialog(
                        it,
                        getString(R.string.dialog_exist_memo_delete),
                        getString(R.string.no),
                        getString(R.string.yes),
                        {},
                        {
                            for (i in selectCheckBoxPosition) {
                                if (i.value) {
                                    deleteBookmarkData(i.key)
                                    adapter.submitList(ViewerActivity.bookMarkDataList)
                                }
                            }
                            CommonUtil.writeBookMarkFile()
                            hideSwitchViews(false)
                        },
                        false
                    )
                } else {
                    for (i in selectCheckBoxPosition) {
                        if (i.value) {
                            deleteBookmarkData(i.key)
                            adapter.submitList(CommonUtil.sortBookMarkList(ViewerActivity.bookMarkDataList))
                        }
                    }
                    CommonUtil.writeBookMarkFile()
                    hideSwitchViews(false)
                }
            }
        }

        // 북마크 리스트 어댑터 생성 및 적용
        adapter = BookMarkListAdapter(
            { view, page ->
                when (view.id) {
                    R.id.layout_memo_image -> {
                        if (activity is ViewerActivity) (activity as ViewerActivity).jumpPage(page) //페이지아니고 포지션
                        dismiss()
                    }
                    R.id.button_memo_add, R.id.detailButton -> {
                        memoPage = page
                        var dialog = MemoPopUpDialogFragment()
                        dialog.show(parentFragmentManager, null)
                        dialog.setMemoCallback(callback)
                    }
                }
            },
            { view, page, isEmptyMemo ->
                var pop = PopupMenu(view.context, view, Gravity.END, 0, R.style.MyPopupMenu)
                if (isEmptyMemo) {
                    pop?.menuInflater?.inflate(R.menu.empty_memo_menu, pop.menu)
                } else {
                    pop?.menuInflater?.inflate(R.menu.popup_menu, pop.menu)
                }
                var listener = PopupListener(page)
                pop.setOnMenuItemClickListener(listener)
                pop.show()
            },
            { position, isChecked ->
                onCheckedChanged(position, isChecked)
            }
        )
        binding.recyclerview.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerview.adapter = adapter
        adapter.submitList(CommonUtil.sortBookMarkList(ViewerActivity.bookMarkDataList))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let { CommonUtil.dialogFullScreen(it) }
    }

    override fun onResume() {
        super.onResume()
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

    inner class PopupListener(var page: Int) : PopupMenu.OnMenuItemClickListener {
        override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
            when (menuItem?.itemId) {

                R.id.removeAll -> {

                    context?.let {
                        CommonUtil.showCustomDialog(
                            it,
                            getString(R.string.dialog_exist_memo_delete),
                            getString(R.string.no),
                            getString(R.string.yes),
                            {},
                            {
                                deleteBookmarkData(page)
                                CommonUtil.writeBookMarkFile()
                                if (selectCheckBoxPosition.isEmpty()) setBookmarkListUi()
                            }, false
                        )
                    }
                }

                R.id.removeMemo -> {
                    context?.let {
                        CommonUtil.showCustomDialog(
                            it,
                            getString(R.string.dialog_memo_delete),
                            getString(R.string.no),
                            getString(R.string.yes),
                            {},
                            {
                                deleteMemoData(page)
                                adapter.submitList(CommonUtil.sortBookMarkList(ViewerActivity.bookMarkDataList))
                                adapter.notifyDataSetChanged()
                            }, false
                        )
                    }

                }
                R.id.removeBookMark -> {
                    deleteBookmarkData(page)
                    CommonUtil.writeBookMarkFile()
                }
            }
            return false
        }
    }

    private fun onCheckedChanged(id: Int, isChecked: Boolean) {

        if (isChecked) {
            selectCheckBoxPosition[id] = isChecked
        } else {
            selectCheckBoxPosition.remove(id)
        }

        if (selectCheckBoxPosition.size == ViewerActivity.bookMarkDataList.size) {
            isAllCheck = true
            binding.buttonSelectAll.text = getString(R.string.deselect_all)
        } else {
            isAllCheck = false
            binding.buttonSelectAll.text = getString(R.string.select_all)
        }
        if (selectCheckBoxPosition.isEmpty()) binding.removeButton.visibility = View.GONE
        else binding.removeButton.visibility = View.VISIBLE
        binding.choiceBookTextView?.text =
            String.format(getString(R.string.select_info), selectCheckBoxPosition.size)
    }

    private fun hideSwitchViews(isEditMode: Boolean) {  // 리스틋화면 <-> 삭제화면 전환
        this.isEditMode = isEditMode
        selectCheckBoxPosition.clear()
        adapter.allCheck(false)
        adapter.hideSwitchViews(isEditMode)
        setBookmarkListUi()
    }

    private fun setBookmarkListUi() {

        binding.buttonSelectAll.text = getString(R.string.select_all)

        if (ViewerActivity.bookMarkDataList.isEmpty()) {    // 북마크 데이터가 없는 경우
            binding.bookmarkEmptyScreen.visibility = View.VISIBLE
            binding.basicRelativeLayout.visibility = View.GONE
            binding.modifyRelativeLayout.visibility = View.GONE
            binding.recyclerview.visibility = View.GONE
            binding.removeButton.visibility = View.GONE
        } else {    // 북마크 데이터가 있는 경우
            binding.totalCount.text = "전체 " + ViewerActivity.bookMarkDataList.size.toString()
            binding.bookmarkEmptyScreen.visibility = View.GONE
            if (isEditMode) { // 삭제 모드인 경우
                binding.apply {
                    basicRelativeLayout.visibility = View.GONE
                    modifyRelativeLayout.visibility = View.VISIBLE
                    if (selectCheckBoxPosition.isNotEmpty())
                        removeButton.visibility = View.VISIBLE
                    else
                        removeButton.visibility = View.GONE

                    recyclerview.visibility = View.VISIBLE
                }
            } else {    // 리스트 모드인 경우
                binding.basicRelativeLayout.visibility = View.VISIBLE
                binding.modifyRelativeLayout.visibility = View.GONE
                binding.recyclerview.visibility = View.VISIBLE
                binding.removeButton.visibility = View.GONE
            }
        }
    }

    private fun isExistMemo(selectedCheckBox: HashMap<Int, Boolean>): Boolean {
        for (checkBox in selectedCheckBox) {
            if (checkBox.value) {
                var targetData = ViewerActivity.bookMarkDataList.find { bookmark ->
                    bookmark.bookmark_page == checkBox.key
                }
                if (!targetData?.bookmark_memo.isNullOrEmpty())
                    return true
                break
            }
        }
        return false
    }

    private fun deleteMemoData(page: Int) {
        ViewerActivity.bookMarkDataList.find { bookmark -> bookmark.bookmark_page == page }?.bookmark_memo =
            ""
    }

    private fun deleteBookmarkData(page: Int) {
        ViewerActivity.bookMarkDataList.remove(
            ViewerActivity.bookMarkDataList.find {
                it.bookmark_page == page
            }
        )
        adapter.submitList(ViewerActivity.bookMarkDataList)
        if (selectCheckBoxPosition.isEmpty()) setBookmarkListUi()
        var activity = requireActivity() as ViewerActivity
        activity.getCurrentVisibleBookMark(activity.getRecyclerViewItemPosition())
    }

    var callback = object : memoCallback {
        override fun onClickCallBack(view: View) {
            when (view.id) {
                R.id.topAppBar, R.id.removeButton -> {
                    adapter.submitList(CommonUtil.sortBookMarkList(ViewerActivity.bookMarkDataList))
                    adapter.notifyDataSetChanged()
                }
                R.id.closeButton -> {
                    dismiss()
                }
            }
        }
    }
}