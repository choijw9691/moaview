package com.moaview.moaview.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.moaview.moaview.R
import com.moaview.moaview.adapter.FileListAdapter
import com.moaview.moaview.datastore.SettingDataStore
import com.moaview.moaview.db.ContentsEntity
import com.moaview.moaview.common.ListModeEnum
import com.moaview.moaview.common.SortState
import com.moaview.moaview.util.*
import com.moaview.moaview.util.CommonUtil.calculateSpanCount
import com.moaview.moaview.view.ItemDividerDecoration
import com.moaview.moaview.view.ItemSpacingDecoration
import com.moaview.moaview.view.WrapContentGridLayoutManager
import com.moaview.moaview.view.activity.HomeActivity
import com.moaview.moaview.view.activity.HomeActivity.Companion.CURRENT_PAGE
import com.moaview.moaview.viewmodel.ContentsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.moaview.moaview.common.convertDate
import com.moaview.moaview.common.quickPopup
import com.moaview.moaview.common.setItemDecoration
import com.moaview.moaview.common.setSelectedColor
import com.moaview.moaview.databinding.FragmentBookListBinding
import com.moaview.moaview.util.CommonUtil
import com.moaview.moaview_sdk.data.Entity
import com.moaview.moaview_sdk.view.activity.ViewerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class BookListFragment : BaseFragment<FragmentBookListBinding>(FragmentBookListBinding::inflate), View.OnClickListener {

    private lateinit var parentsActivity: HomeActivity
    private lateinit var callback: OnBackPressedCallback
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetView: View
    private lateinit var recentReadTextView: TextView
    private lateinit var recentCreatedTextView: TextView
    private lateinit var fileNameOrderTextView: TextView
    private lateinit var byFileSizeTextView: TextView
    private val viewModel: ContentsViewModel by activityViewModels()
    var isAllCheck = false
    var pressedBackKey: Long = 0
    private lateinit var adapter: FileListAdapter
    var selectCheckBoxPosition = HashMap<Int, Boolean>()

    companion object {
        var spanCount = 1
        val gridItemDecoration = ItemSpacingDecoration(40, spanCount)
        val linearItemDecoration = ItemDividerDecoration(1, 50, "#e1e1e1")
    }

    var isEditMode = false

    lateinit var textViewList: Array<TextView>
    lateinit var listModeTextViewList: Array<TextView>
    lateinit var listModeImageViewList: Array<ImageView>

    lateinit var coverTextView: TextView
    lateinit var listTextView: TextView
    lateinit var detailTextView: TextView
    lateinit var coverImageView: ImageView
    lateinit var listImageView: ImageView
    lateinit var detailImageView: ImageView

    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK -> {

                    CoroutineScope(Dispatchers.Main).launch {
                        var dialog = LoadingDialogFragment()
                        dialog.show(requireActivity().supportFragmentManager, null)
                        CoroutineScope(Dispatchers.IO).launch {
                            result?.data.also { it ->
                                var contentsEntity =
                                    context?.let { it1 ->
                                        viewModel.allContents.value?.let { it2 ->
                                            CommonUtil.copyFile(
                                                it1, it?.data,
                                                it2, dialog
                                            )
                                        }
                                    }

                                contentsEntity?.let { it1 ->
                                    if (!it1.downLoad) {
                                        it1.downLoad = true
                                        viewModel.insert(
                                            it1
                                        )
                                    }
                                }
                                dialog.dismiss()
                            }
                        }
                    }
                }
                CURRENT_PAGE -> {

                    var currentPage = result.data?.getIntExtra("currentPage", 1)
                    var id = result.data?.getIntExtra("id", 1)

                    var content = viewModel.allContents.value?.find { it.id == id }
                    if (currentPage != null) {
                        content?.currentPage = currentPage
                    }
                    content?.let { viewModel.update(it) }
                    ViewerActivity
                    adapter.notifyDataSetChanged()
                }
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.searchButton.setOnClickListener {
            parentsActivity = activity as HomeActivity
            parentsActivity.changeFragment(1)
        }
        binding.settingButton.setOnClickListener {
            parentsActivity = activity as HomeActivity
            parentsActivity.changeFragment(2)
        }

        binding.addbook.setOnClickListener {
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/zip"
                addCategory(Intent.CATEGORY_OPENABLE);
            }
            activityLauncher.launch(intent)
        }

        initViewMode()

        initSortState()

        setListType(enumValueOf(HomeActivity.settingData.listMode), null)

        binding.alignButton.setOnClickListener {
            bottomSheetDialog.show()
        }

        binding.changeViewButton.setOnClickListener {
            requireContext().quickPopup(binding.changeViewButton) { fnDismiss ->
                val inflater =
                    requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val view = inflater.inflate(R.layout.item_spinner, null)

                coverTextView = view.findViewById<TextView>(R.id.coverTextView)
                listTextView = view.findViewById<TextView>(R.id.listTextView)
                detailTextView = view.findViewById<TextView>(R.id.detailTextView)

                coverImageView = view.findViewById<ImageView>(R.id.coverImageView)
                listImageView = view.findViewById<ImageView>(R.id.listImageView)
                detailImageView = view.findViewById<ImageView>(R.id.detailImageView)

                listModeTextViewList = arrayOf(
                    coverTextView,
                    listTextView,
                    detailTextView
                )
                listModeImageViewList = arrayOf(
                    coverImageView,
                    listImageView,
                    detailImageView
                )

                setListModeColor()

                view.findViewById<LinearLayout>(R.id.coverView).setOnClickListener {
                    setListType(ListModeEnum.COVERVIEW, fnDismiss)
                }

                view.findViewById<LinearLayout>(R.id.listView).setOnClickListener {
                    setListType(ListModeEnum.LISTVIEW, fnDismiss)
                }

                view.findViewById<LinearLayout>(R.id.detailView).setOnClickListener {
                    setListType(ListModeEnum.DETAILVIEW, fnDismiss)
                }

                return@quickPopup PopupInfo(
                    view,
                    105,
                    0,
                    -5,
                    20
                )
            }
        }

        viewModel.allContents.observe(viewLifecycleOwner) {
            adapter.addHeaderAndSubmitList(it)
            binding.editButton.isEnabled = it.isNotEmpty()
            binding.removeButton.isEnabled = it.isNotEmpty()
            binding.recyclerview.adapter?.notifyDataSetChanged()
        }

        binding.editButton.setOnClickListener {
            hideSwitchViews(true)
            binding.choiceBookTextView.text = String.format(getString(R.string.select_info), 0)
        }

        binding.buttonCancel.setOnClickListener {
            selectCheckBoxPosition.clear()
            hideSwitchViews(false)
        }

        binding.buttonSelectAll.setOnClickListener {
            if (isAllCheck) {
                isAllCheck = false
                adapter.setAllCheckboxStatus(false)
                binding.buttonSelectAll.text = getString(R.string.select_all)
                selectCheckBoxPosition.clear()
                binding.removeButton.visibility = View.GONE
            } else {
                isAllCheck = true
                adapter.setAllCheckboxStatus(true)
                binding.buttonSelectAll.text = getString(R.string.deselect_all)
                selectCheckBoxPosition.clear()
                binding.removeButton.visibility = View.VISIBLE
                var size = viewModel.allContents.value?.size
                for (i in 0 until size!!) {
                    selectCheckBoxPosition[viewModel.allContents.value!![i].id] = true
                }

            }
            binding.choiceBookTextView.text =
                String.format(getString(R.string.select_info), selectCheckBoxPosition.size)
        }

        binding.removeButton.setOnClickListener {
            viewModel.deleteContents(selectCheckBoxPosition)
            hideSwitchViews(false)
            selectCheckBoxPosition.clear()
        }
    }

    private fun listItemClicked(entity: ContentsEntity, view: View) {
        when (view.id) {
            R.id.popupmenu -> {
                if (!isEditMode) {
                    requireContext().quickPopup(view) { fnDismiss ->
                        var listener = PopupListener(entity) {
                            fnDismiss()
                        }
                        val itemPopup = layoutInflater.inflate(R.layout.item_popup, null)
                        itemPopup.findViewById<LinearLayout>(R.id.detailContent)
                            .setOnClickListener(listener)
                        itemPopup.findViewById<LinearLayout>(R.id.updateContent)
                            .setOnClickListener(listener)
                        return@quickPopup PopupInfo(
                            itemPopup,
                            105,
                            0,
                            -105,
                            -55
                        )
                    }
                }
            }

            R.id.alignButton -> {
                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetDialog.show()
            }

            R.id.coverImage -> {
                if (!isEditMode) {
                    var intent = Intent(context, ViewerActivity::class.java)
                    intent.putExtra("isFirstOpen", entity.opened)
                    intent.putExtra(
                        "Entity",
                        Entity(
                            entity.id,
                            entity.title,
                            entity.createDate,
                            entity.readDate,
                            entity.contentsType,
                            entity.coverImage,
                            entity.currentPage,
                            entity.totalPage,
                            CommonUtil.convertByte(entity.fileSize.toLong())
                        )
                    )

                    activityLauncher.launch(intent)
                    //      startActivity(intent)
                    entity.readDate = Date(System.currentTimeMillis())
                    entity.opened = true
                    viewModel.update(entity)
                }
            }
            else -> {

            }
        }
    }

    inner class PopupListener(var entity: ContentsEntity, val clickListener: () -> Unit) :
        View.OnClickListener {

        override fun onClick(p0: View?) {
            clickListener()
            when (p0?.id) {
                R.id.detailContent -> {
                    showContentDetailView(entity)
                }
                R.id.updateContent -> {
                    viewModel.setUpdateContent(entity)
                    parentsActivity = activity as HomeActivity
                    parentsActivity.changeFragment(3)
                }
            }
        }
    }

    private fun onCheckedChanged(id: Int, isChecked: Boolean) {
        if (isChecked) {
            selectCheckBoxPosition[id] = isChecked
        } else {
            selectCheckBoxPosition.remove(id)
        }

        if (selectCheckBoxPosition.size == viewModel.allContents.value?.size) {
            isAllCheck = true
            binding.buttonSelectAll.text = getString(R.string.deselect_all)
            binding.removeButton.visibility = View.GONE

        } else {
            isAllCheck = false
            binding.buttonSelectAll.text = getString(R.string.select_all)
            if (isEditMode) {
                binding.removeButton.visibility = View.VISIBLE
            }

        }
        binding.choiceBookTextView?.text =
            String.format(getString(R.string.select_info), selectCheckBoxPosition.size)
        if (selectCheckBoxPosition.isEmpty()) {
            binding.removeButton.visibility = View.GONE
        } else {

            if (isEditMode) {
                binding.removeButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(view: View?) {
        CoroutineScope(Dispatchers.Main).launch {
            var sortingState = SortState.READ
            var settingsManager = context?.let { SettingDataStore(it) }

            when (view?.id) {
                R.id.recentRead -> {
                    sortingState = SortState.READ
                    settingsManager?.setSortState(sortingState)
                }
                R.id.recentCreated -> {
                    sortingState = SortState.CREATED
                    settingsManager?.setSortState(sortingState)
                }
                R.id.fileNameOrder -> {
                    sortingState = SortState.TITLE
                    settingsManager?.setSortState(sortingState)
                }
                R.id.byFileSize -> {
                    sortingState = SortState.FILESIZE
                    settingsManager?.setSortState(sortingState)
                }
            }
            viewModel.allContents.value?.let {
                viewModel.bookListFragmentSort(
                    it.toMutableList(),
                    sortingState
                )
            }

            (view as TextView).setSelectedColor(
                textViewList,
                null,
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_f45b7f
                ),
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_828282
                )
            )
            bottomSheetDialog.dismiss()
        }
    }

    fun hideSwitchViews(switch: Boolean) {
        adapter.hideSwitchViews(switch)
        if (switch) {
            isEditMode = true
            binding.apply {
                basicRelativeLayout.visibility = View.GONE
                modifyRelativeLayout.visibility = View.VISIBLE
                addbook.visibility = View.GONE
            }
        } else {
            isEditMode = false
            binding.apply {
                basicRelativeLayout.visibility = View.VISIBLE
                modifyRelativeLayout.visibility = View.GONE
                removeButton.visibility = View.GONE
                addbook.visibility = View.VISIBLE
            }
        }
        adapter.setAllCheckboxStatus(false)
        binding.buttonSelectAll.text = getString(R.string.select_all)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEditMode) {
                    selectCheckBoxPosition.clear()
                    hideSwitchViews(false)
                } else {
                    if (System.currentTimeMillis() - pressedBackKey >= 2000) {
                        pressedBackKey = System.currentTimeMillis()
                        Toast.makeText(
                            getContext(),
                            getString(R.string.notify_back_key),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        (activity as HomeActivity).appFinish()
                    }
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    // pop을 관리하기 싫어서 closure로 생성함
    data class PopupInfo(
        val v: View,
        val width: Int,
        val height: Int,
        val x: Int,
        val y: Int
    )

    private fun setListType(currentListMode: ListModeEnum, fnDismiss: (() -> Unit?)?) {
//        현재 리스트 타입 유지를 위한 저장
        CoroutineScope(Dispatchers.Main).launch {
            var settingsManager = SettingDataStore(requireContext())
            settingsManager.setListMode(currentListMode)
        }

        when (currentListMode) {
            ListModeEnum.COVERVIEW -> {
                spanCount = calculateSpanCount(requireContext(), 170)
                if (spanCount < 2) spanCount = 2
                binding.changeViewButton.background =
                    resources.getDrawable(R.drawable.sort_thumb, null)
                binding.recyclerview.setItemDecoration(linearItemDecoration, gridItemDecoration)
            }
            ListModeEnum.LISTVIEW -> {
                spanCount = 1
                binding.changeViewButton.background =
                    resources.getDrawable(R.drawable.sort_complex, null)
                binding.recyclerview.setItemDecoration(gridItemDecoration, linearItemDecoration)
            }
            ListModeEnum.DETAILVIEW -> {
                spanCount = 1
                binding.changeViewButton.background =
                    resources.getDrawable(R.drawable.sort_detail, null)
                binding.recyclerview.setItemDecoration(gridItemDecoration, linearItemDecoration)
            }
        }

        adapter = FileListAdapter(
            currentListMode,
            { entity, view ->
                entity?.let {
                    listItemClicked(it, view)
                }
            }, { showContentDetailView(it) },
            { position, isChecked ->
                if (isEditMode) {
                    onCheckedChanged(position, isChecked)
                }
            },
            false
        )

        val layoutManager = WrapContentGridLayoutManager(context, spanCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == adapter.HEADER) spanCount
                else if (adapter.getItemViewType(position) == adapter.EMPTY) spanCount
                else 1
            }
        }
        binding.recyclerview.layoutManager = layoutManager
        binding.recyclerview.adapter = adapter
        adapter.addHeaderAndSubmitList(viewModel.allContents.value)
        setListModeColor()
        fnDismiss?.let { it() }
    }

    private fun setListModeColor() {
        var textView =
            when (HomeActivity.settingData.listMode) {
                ListModeEnum.COVERVIEW.toString() -> listModeTextViewList[0]
                ListModeEnum.LISTVIEW.toString() -> listModeTextViewList[1]
                else -> listModeTextViewList[2]
            }

        textView.setSelectedColor(
            listModeTextViewList,
            listModeImageViewList,
            ContextCompat.getColor(
                requireContext(),
                R.color.color_f45b7f
            ),

            ContextCompat.getColor(
                requireContext(),
                R.color.color_828282
            )
        )
    }

    private fun initViewMode() {

        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.item_spinner, null)

        coverTextView = view.findViewById(R.id.coverTextView)
        listTextView = view.findViewById(R.id.listTextView)
        detailTextView = view.findViewById(R.id.detailTextView)

        coverImageView = view.findViewById(R.id.coverImageView)
        listImageView = view.findViewById(R.id.listImageView)
        detailImageView = view.findViewById(R.id.detailImageView)

        listModeTextViewList = arrayOf(
            coverTextView,
            listTextView,
            detailTextView
        )
        listModeImageViewList = arrayOf(
            coverImageView,
            listImageView,
            detailImageView
        )

        setListModeColor()
    }

    private fun initSortState() {
        bottomSheetView = layoutInflater.inflate(R.layout.layout_contents_sort_filter, null)
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        recentReadTextView = bottomSheetView.findViewById(R.id.recentRead)
        recentCreatedTextView = bottomSheetView.findViewById(R.id.recentCreated)
        fileNameOrderTextView = bottomSheetView.findViewById(R.id.fileNameOrder)
        byFileSizeTextView = bottomSheetView.findViewById(R.id.byFileSize)

        textViewList = arrayOf(
            recentReadTextView,
            recentCreatedTextView,
            fileNameOrderTextView,
            byFileSizeTextView
        )

        var textView =
            when (HomeActivity.settingData.sortState) {
                SortState.READ.toString() -> textViewList[0]
                SortState.CREATED.toString() -> textViewList[1]
                SortState.TITLE.toString() -> textViewList[2]
                else -> textViewList[3]
            }

        textView.setSelectedColor(
            textViewList,
            null,
            ContextCompat.getColor(
                requireContext(),
                R.color.color_f45b7f
            ),
            ContextCompat.getColor(
                requireContext(),
                R.color.color_828282
            )
        )

        recentReadTextView.setOnClickListener(this)
        recentCreatedTextView.setOnClickListener(this)
        fileNameOrderTextView.setOnClickListener(this)
        byFileSizeTextView.setOnClickListener(this)

    }

    fun showContentDetailView(entity: ContentsEntity) {
        val bottomSheetView =
            layoutInflater.inflate(R.layout.layout_contents_detail, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
        bottomSheetView.findViewById<TextView>(R.id.title).text = entity.title
        bottomSheetView.findViewById<TextView>(R.id.recentRead).text =
            entity.readDate.time.convertDate(
                SimpleDateFormat(
                    "yyyy.MM.dd HH:mm",
                    Locale.getDefault()
                )
            )
        bottomSheetView.findViewById<TextView>(R.id.recentCreated).text =
            entity.createDate.time.convertDate(
                SimpleDateFormat(
                    "yyyy.MM.dd",
                    Locale.getDefault()
                )
            )
        bottomSheetView.findViewById<TextView>(R.id.fileSize).text =
            CommonUtil.convertByte(entity.fileSize.toLong())
        bottomSheetView.findViewById<TextView>(R.id.pageCount).text = String.format(
            getString(R.string.detail_page_count),
            entity.currentPage,
            entity.totalPage
        )
    }
}

