package com.moaview.moaview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.moaview.ViewerApplication
import com.moaview.moaview.R
import com.moaview.moaview.data.ContentWithHeaderData
import com.moaview.moaview.db.ContentsEntity
import com.moaview.moaview.common.ListModeEnum
import com.moaview.moaview.util.CommonUtil
import com.moaview.moaview.common.convertDate
import com.moaview.moaview.databinding.EmptyScreenBinding
import com.moaview.moaview.databinding.ItemCoverviewBinding
import com.moaview.moaview.databinding.ItemDetailviewBinding
import com.moaview.moaview.databinding.ItemEmptySearchBinding
import com.moaview.moaview.databinding.ItemHeaderBinding
import com.moaview.moaview.databinding.ItemListviewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

class FileListAdapter(
    val listMode: ListModeEnum,
    val clickListener: (ContentsEntity?, View) -> Unit,
    val longClickListener : (ContentsEntity)->Unit,
    val checkBoxListener: (Int, Boolean) -> Unit,
    val isSearchView: Boolean = false
) : ListAdapter<ContentWithHeaderData, RecyclerView.ViewHolder>(diffUtil) {

    var alphaValue = 0f
    var isDeleteMode = false
    var isAllCheckboxChecked = false

    val HEADER = 0 // 헤더 뷰
    val ITEM = 1 // 리사이클러 아이템 뷰
    val EMPTY = 2 // 데이터가 없을 때 뜨는 뷰

    var searchTitle = ""

    inner class CoverViewHolder(var binding: ItemCoverviewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContentsEntity) {
            binding.apply {
                var percent = 0f
                if(item.currentPage !=1) percent = ((item.currentPage.toFloat() / item.totalPage) * 100)
                textID.text = item.title
                pagePercent.text ="${round(percent).toInt()}%"
                slidebar.value = round(percent * 100) / 100
                popupmenu.setOnClickListener { clickListener(item, popupmenu) }
                popupmenuContainer.setOnClickListener { clickListener(item, popupmenu)  }
                checkButton.apply {
                    setOnClickListener { checkBoxListener(item.id, checkButton.isChecked) }
                    isChecked = isAllCheckboxChecked
                    visibility = when {
                        isDeleteMode -> {
                            View.VISIBLE
                        }
                        else -> {
                            alphaValue = 1f
                            View.GONE
                        }
                    }
                }
                coverImage.apply {
                    alpha = alphaValue
                    Glide.with(context)
                        .load(item.coverImage)
                        .into(this)
                }
                container.apply {
                    setOnClickListener {
                        clickListener(item, binding.coverImage)
                        checkButton.isChecked = !checkButton.isChecked
                        checkBoxListener(item.id, checkButton.isChecked)
                    }
                    setOnLongClickListener {
                        longClickListener(item)
                        true
                    }
                }
            }

            if (isDeleteMode) {
                binding.checkButton.isChecked = false
                binding.checkButton.visibility = View.VISIBLE
            } else {
                binding.checkButton.isChecked = false
                binding.checkButton.visibility = View.GONE
                alphaValue = 1f
            }

            if (isAllCheckboxChecked) {
                binding.checkButton.isChecked = true
            }

            binding.coverImage.alpha = alphaValue
        }
    }

    inner class ListViewHolder(var binding: ItemListviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContentsEntity) {
            var percent = 0f
            if(item.currentPage !=1) percent = ((item.currentPage.toFloat() / item.totalPage) * 100)
            binding.apply {
                textID.text = item.title
                pagePercent.text = "${round(percent).toInt()}%"
                slidebar.value = round(percent * 100) / 100
                dateFileSize.text = "${item.readDate.time.convertDate(
                    SimpleDateFormat(
                        "yyyy.MM.dd",
                        Locale.getDefault()
                    )
                )} | ${CommonUtil.convertByte(item.fileSize.toLong())}"
                popupmenu.setOnClickListener {
                    clickListener(item, popupmenu)
                }
                checkButton.apply {
                    setOnClickListener { checkBoxListener(item.id, checkButton.isChecked) }
                    isChecked = isAllCheckboxChecked
                    visibility = when {
                        isDeleteMode -> {
                            View.VISIBLE
                        }
                        else -> {
                            alphaValue = 1f
                            View.GONE
                        }
                    }
                }
                coverImage.apply {
                    alpha = alphaValue
                    Glide.with(context)
                        .asBitmap()
                        .transform(RoundedCorners(30))
                        .load(item.coverImage)
                        .into(this)
                }
                layoutItemList.apply {

                    setOnClickListener {
                        clickListener(item, binding.coverImage)
                        checkButton.isChecked = !checkButton.isChecked
                        checkBoxListener(item.id, checkButton.isChecked)
                    }
                    setOnLongClickListener {
                        longClickListener(item)
                        true
                    }
                }
            }
        }
    }

    inner class DetailViewHolder(var binding: ItemDetailviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContentsEntity) {
            var percent = 0f
            if(item.currentPage !=1) percent = ((item.currentPage.toFloat() / item.totalPage) * 100)
            binding.apply {
                textID.text = item.title
                pagePercent.text = "${round(percent).toInt()}%"
                slidebar.value = round(percent).toInt().toFloat()
                dateFileSize.text =
                    item.readDate.time.convertDate(
                    SimpleDateFormat(
                        "yyyy.MM.dd",
                        Locale.getDefault()
                    )
                ) +" | "+ CommonUtil.convertByte(item.fileSize.toLong())

                popupmenu.setOnClickListener {
                    clickListener(item, popupmenu)
                }

                checkButton.apply {
                    setOnClickListener { checkBoxListener(item.id, checkButton.isChecked) }
                    isChecked = isAllCheckboxChecked
                    visibility = when {
                        isDeleteMode -> {
                            View.VISIBLE
                        }
                        else -> {
                            alphaValue = 1f
                            View.GONE
                        }
                    }
                }

                container.apply {
                    setOnClickListener {
                        clickListener(item, binding.coverImage)
                        checkButton.isChecked = !checkButton.isChecked
                        checkBoxListener(item.id, checkButton.isChecked)
                    }
                    setOnLongClickListener {
                        longClickListener(item)
                        true
                    }
                }
            }
        }
    }

    inner class HeaderViewHolder(var binding: ItemHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    inner class EmptyViewHolder(var binding: EmptyScreenBinding) : RecyclerView.ViewHolder(binding.root)

    inner class EmptySearchViewHolder(var binding: ItemEmptySearchBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind() {
            binding.resultTitle.text = "'${searchTitle}'"
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER ->
                HeaderViewHolder(
                    ItemHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ITEM ->
                when (listMode) {
                    ListModeEnum.COVERVIEW -> {
                        CoverViewHolder(
                            ItemCoverviewBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                            )
                        )
                    }
                    ListModeEnum.LISTVIEW -> {
                        ListViewHolder(
                            ItemListviewBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                            )
                        )
                    }
                    ListModeEnum.DETAILVIEW -> {
                        DetailViewHolder(
                            ItemDetailviewBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                            )
                        )
                    }
                }

            EMPTY -> {
                if (!isSearchView) {
                    EmptyViewHolder(
                        EmptyScreenBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    )
                } else {
                    EmptySearchViewHolder(
                        ItemEmptySearchBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    )
                }
            }
            else -> {
                throw ClassCastException("Unknown viewType $viewType")
            }
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            // HEADER
            is HeaderViewHolder -> {
                if (getItemViewType(1) == EMPTY) {
                    holder.binding.BooksCountContainer.visibility == View.GONE
                } else {
                    CoroutineScope(Dispatchers.Main).launch{
                        holder.binding.BooksCount.text = String.format(
                            ViewerApplication.getApplicationContext().getString(
                                R.string.total_contents
                            ), (currentList.size - 1)
                        )
                    }
                }
            }
            // ITEM
            is CoverViewHolder -> {
                val data = getItem(position) as ContentWithHeaderData.Item
                holder.bind(data.content)
                if (isSearchView) {
                    holder.binding.popupmenu.visibility = View.GONE
                }
            }
            is ListViewHolder -> {
                val data = getItem(position) as ContentWithHeaderData.Item
                holder.bind(data.content)
                if (isSearchView) {
                    holder.binding.popupmenu.visibility = View.GONE
                }
            }
            is DetailViewHolder -> {
                val data = getItem(position) as ContentWithHeaderData.Item
                holder.bind(data.content)
                if (isSearchView) {
                    holder.binding.popupmenu.visibility = View.GONE
                }
            }
            is EmptySearchViewHolder ->{
                holder.bind()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ContentWithHeaderData.Header -> HEADER
            is ContentWithHeaderData.Empty -> EMPTY
            is ContentWithHeaderData.Item -> ITEM
        }
    }

    fun hideSwitchViews(switch: Boolean) {
        isDeleteMode = switch
        alphaValue = 0.5f
        notifyDataSetChanged()
    }

    fun setAllCheckboxStatus(isAllChecked: Boolean) {
        isAllCheckboxChecked = isAllChecked
        notifyDataSetChanged()
    }

    fun addHeaderAndSubmitList(list: List<ContentsEntity>?) {
        val items = when (list.isNullOrEmpty()) {
            true -> listOf(ContentWithHeaderData.Empty)
            false -> if (!isSearchView) {
                listOf(ContentWithHeaderData.Header) + list.map { ContentWithHeaderData.Item(it) }
            } else {
                list.map { ContentWithHeaderData.Item(it) }
            }
        }
        submitList(items)
    }

    fun setSearchText(searchTitle: String) {
        this.searchTitle = searchTitle
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ContentWithHeaderData>() {
            override fun areItemsTheSame(
                oldItem: ContentWithHeaderData,
                newItem: ContentWithHeaderData
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ContentWithHeaderData,
                newItem: ContentWithHeaderData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}