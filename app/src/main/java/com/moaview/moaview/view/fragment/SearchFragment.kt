package com.moaview.moaview.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.moaview.moaview.R
import com.moaview.moaview.adapter.FileListAdapter
import com.moaview.moaview.databinding.FragmentSearchBinding
import com.moaview.moaview.db.ContentsEntity
import com.moaview.moaview.common.ListModeEnum
import com.moaview.moaview.common.SortState
import com.moaview.moaview.util.CommonUtil
import com.moaview.moaview.common.quickPopup
import com.moaview.moaview.common.setItemDecoration
import com.moaview.moaview.common.setSelectedColor
import com.moaview.moaview.view.WrapContentGridLayoutManager
import com.moaview.moaview.view.activity.HomeActivity.Companion.CURRENT_PAGE
import com.moaview.moaview.view.fragment.BookListFragment.Companion.gridItemDecoration
import com.moaview.moaview.view.fragment.BookListFragment.Companion.linearItemDecoration
import com.moaview.moaview.viewmodel.ContentsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.moaview.moaview_sdk.data.Entity
import com.moaview.moaview_sdk.view.activity.ViewerActivity
import java.util.*
import kotlin.collections.set

class SearchFragment : BaseFragment<FragmentSearchBinding>(FragmentSearchBinding::inflate),
    View.OnClickListener {

    private lateinit var adapter: FileListAdapter

    private var spanCount = 2

    private var selectCheckBoxPosition = HashMap<Int, Boolean>()

    private lateinit var bottomSheetDialog: BottomSheetDialog

    private lateinit var bottomSheetView: View

    private val viewModel: ContentsViewModel by activityViewModels()

    private var isEditMode = false

    lateinit var textViewList: Array<TextView>
    lateinit var listModeTextViewList: Array<TextView>
    lateinit var listModeImageViewList: Array<ImageView>
    lateinit var coverTextView: TextView
    lateinit var listTextView: TextView
    lateinit var detailTextView: TextView
    lateinit var coverImageView: ImageView
    lateinit var listImageView: ImageView
    lateinit var detailImageView: ImageView

    private var searchResultList: List<ContentsEntity>? = null

    private var currentViewMode: String = ListModeEnum.COVERVIEW.toString()

    private var currentAlignState: Int? = 0

    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                CURRENT_PAGE -> {
                    var currentPage = result.data?.getIntExtra("currentPage", 1)
                    var id = result.data?.getIntExtra("id", 1)
                    var content = viewModel.allContents.value?.find { it.id == id }
                    if (currentPage != null) {
                        content?.currentPage = currentPage
                    }
                    content?.let { viewModel.update(it) }
                    adapter.notifyDataSetChanged()
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListType(ListModeEnum.COVERVIEW, null)
        binding.basicRelativeLayout.visibility = View.GONE

        if (savedInstanceState == null) {
        } else {
            var searchText = savedInstanceState.getString("SearchText");
            currentViewMode = savedInstanceState.getString("ListModeEnum").toString()
            binding.myEditText.setText(searchText)
            currentAlignState = savedInstanceState.getInt("currentAlignState")
            getSearchList()
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.searchButton.setOnClickListener {
            getSearchList()
            CommonUtil.hideKeyBoard(requireActivity(),view)

        }

        bottomSheetView = layoutInflater.inflate(R.layout.layout_contents_sort_filter, null)
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.alignButton.setOnClickListener {
            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetDialog.show()
        }

        var recentReadTextView = bottomSheetView.findViewById<TextView>(R.id.recentRead)
        var recentCreatedTextView = bottomSheetView.findViewById<TextView>(R.id.recentCreated)
        var fileNameOrderTextView = bottomSheetView.findViewById<TextView>(R.id.fileNameOrder)
        var byFileSizeTextView = bottomSheetView.findViewById<TextView>(R.id.byFileSize)

        textViewList = arrayOf(
            recentReadTextView,
            recentCreatedTextView,
            fileNameOrderTextView,
            byFileSizeTextView
        )

        recentReadTextView.setOnClickListener(this)
        recentCreatedTextView.setOnClickListener(this)
        fileNameOrderTextView.setOnClickListener(this)
        byFileSizeTextView.setOnClickListener(this)

        textViewList[currentAlignState!!].setSelectedColor(
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


        binding.myEditText.apply {
            setOnEditorActionListener { v, actionId, event ->
                when (actionId) {
                    IME_ACTION_SEARCH -> {
                        getSearchList()
                        CommonUtil.hideKeyBoard(requireActivity(), view)
                    }
                }
                true
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    if (binding.myEditText.text.toString().isEmpty()) {
                        binding.textClearButton.visibility = View.GONE
                    } else {
                        binding.textClearButton.visibility = View.VISIBLE
                    }
                }
            })
        }

        binding.changeViewButton.setOnClickListener {
            requireContext().quickPopup(binding.changeViewButton) { fnDismiss ->
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

                view.findViewById<LinearLayout>(R.id.coverView).setOnClickListener {
                    setListType(ListModeEnum.COVERVIEW, fnDismiss)
                }
                view.findViewById<LinearLayout>(R.id.listView).setOnClickListener {
                    setListType(ListModeEnum.LISTVIEW, fnDismiss)
                }

                view.findViewById<LinearLayout>(R.id.detailView).setOnClickListener {
                    setListType(ListModeEnum.DETAILVIEW, fnDismiss)
                }

                setListModeColor()

                return@quickPopup BookListFragment.PopupInfo(
                    view,
                    105,
                    0,
                    -5,
                    20
                )
            }
        }

        binding.textClearButton.setOnClickListener {
            binding.basicRelativeLayout.visibility = View.GONE
            binding.textClearButton.visibility = View.GONE
            binding.recyclerview.visibility = View.INVISIBLE
            binding.myEditText.text.clear()
        }
    }

    private fun listItemClicked(entity: ContentsEntity, view: View) {
        when (view.id) {
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
                    entity.readDate = Date(System.currentTimeMillis())
                    entity.opened = true
                    viewModel.update(entity)
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
    }

    private fun getSearchList() {
        if (binding.myEditText.text.toString().isEmpty()) {
            binding.searchResult.text = String.format(getString(R.string.search_result), 0)
            searchResultList = null
        } else {
            viewModel.getSearchList(binding.myEditText.text.toString())
                .observe(viewLifecycleOwner) {
                    binding.searchResult.text =
                        String.format(getString(R.string.search_result), it.size)
                    binding.searchResult.visibility = View.VISIBLE
                    searchResultList = it
                    adapter.setSearchText(binding.myEditText.text.toString())
                    setHeaderAndSubmitList()
                    adapter.notifyDataSetChanged()
                }
        }
    }

    override fun onClick(view: View?) {
        var sortingState = SortState.READ
        when (view?.id) {
            R.id.recentRead -> {
                sortingState = SortState.READ
            }
            R.id.recentCreated -> {
                sortingState = SortState.CREATED
            }
            R.id.fileNameOrder -> {
                sortingState = SortState.TITLE
            }
            R.id.byFileSize -> {
                sortingState = SortState.FILESIZE
            }
        }

        searchResultList = searchResultList?.let {
            CommonUtil.sortList(
                it.toMutableList(),
                sortingState
            )
        }

        currentAlignState = (view as TextView).setSelectedColor(
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
        setHeaderAndSubmitList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SearchText", binding.myEditText.text.toString())
        outState.putString("ListModeEnum", currentViewMode)
        currentAlignState?.let { outState.putInt("currentAlignState", it) }
    }

    private fun setListType(currentListMode: ListModeEnum, fnDismiss: (() -> Unit?)?) {

        currentViewMode = currentListMode.toString()

        when (currentListMode) {
            ListModeEnum.COVERVIEW -> {
                spanCount = CommonUtil.calculateSpanCount(requireContext(), 170)
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
            },{},
            { position, isChecked ->
                onCheckedChanged(position, isChecked)
            },
            true
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

        getSearchList()

        fnDismiss?.let { it() }
    }

    private fun setListModeColor() {

        var textView =
            when (currentViewMode) {
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

    private fun setHeaderAndSubmitList() {
        if (searchResultList?.size == 0) {
            binding.basicRelativeLayout.visibility = View.GONE
        } else {
            binding.basicRelativeLayout.visibility = View.VISIBLE
        }
        binding.recyclerview.visibility = View.VISIBLE
        adapter.addHeaderAndSubmitList(searchResultList)
    }
}