package com.moaview.moaview_sdk.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.moaview.moaview_sdk.data.BookMark
import com.moaview.moaview_sdk.databinding.ItemBookmarkListBinding
import com.moaview.moaview_sdk.view.activity.ViewerActivity
import java.text.SimpleDateFormat
import java.util.*

class BookMarkListAdapter(
    val clickListener: (View,Int) -> Unit,
    val popupListener:(View,Int,Boolean)->Unit,
    val checkBoxListener: (Int, Boolean) -> Unit
) : ListAdapter<BookMark, RecyclerView.ViewHolder>(diffUtil) {

    var alphaValue = 0f
    var isDelete = false
    var isAllCheck = false

    inner class BookMarkViewHolder(var binding: ItemBookmarkListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BookMark) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BookMarkViewHolder(
            ItemBookmarkListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Glide.with((holder as BookMarkViewHolder).binding.imageMemo.context)
            .load(ViewerActivity.metaDataList[currentList[position].bookmark_page-1].path)
            .into(holder.binding.imageMemo)
        Log.d("JIWOUNMG","bindcheck: "+ currentList[position].bookmark_page)

        holder.binding.bookmarkPage.text = currentList[position].bookmark_page.toString() +"P"
        val dateFormat = SimpleDateFormat("yyyy.MM.dd")
        holder.binding.bookmarkDate.text = dateFormat.format(Date(currentList[position].bookmark_date))
        holder.binding.contentTextView.text = currentList[position].bookmark_memo
        holder.binding.layoutMemoImage.setOnClickListener {
            clickListener(it,currentList[position].bookmark_page)
        }

        var memo = currentList[position].bookmark_memo.trim()
        var isEmptyMemo = false
        if (memo==null||memo==""||memo.isEmpty()){
            isEmptyMemo = true
            holder.binding.layoutMemoText.visibility = View.GONE
            holder.binding.buttonMemoAdd.visibility = View.VISIBLE
        }else{
            holder.binding.layoutMemoText.visibility = View.VISIBLE
            holder.binding.buttonMemoAdd.visibility = View.GONE
        }

        holder.binding.popupmenu.setOnClickListener {
            popupListener(it,currentList[position].bookmark_page,isEmptyMemo)
        }
        holder.binding.buttonMemoAdd.setOnClickListener {
            clickListener(it,currentList[position].bookmark_page)
        }
        holder.binding.detailButton.setOnClickListener {
            clickListener(it,currentList[position].bookmark_page)
        }

        holder.binding.checkButton.apply{
            setOnClickListener { checkBoxListener(currentList[position].bookmark_page, this.isChecked) }
            isChecked = isAllCheck
            visibility = when{
                isDelete -> {
                    View.VISIBLE
                }
                else -> {
                    alphaValue = 1f
                    View.GONE
                }
            }
        }
        if (isDelete) {
            holder.binding.checkButton.isChecked = false
            holder.binding.checkButton.visibility = View.VISIBLE
            alphaValue = 0.5f
        } else {
            holder. binding.checkButton.isChecked = false
            holder.binding.checkButton.visibility = View.GONE
            alphaValue = 1f
        }

        holder.binding.layoutMemoContainer.alpha = alphaValue

        if (isAllCheck) {
            holder.binding.checkButton.isChecked = true
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<BookMark>() {
            override fun areItemsTheSame(
                oldItem: BookMark,
                newItem: BookMark
            ): Boolean {
                return oldItem.bookmark_page == newItem.bookmark_page && oldItem.bookmark_date == newItem.bookmark_date
            }

            override fun areContentsTheSame(
                oldItem: BookMark,
                newItem: BookMark
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    fun hideSwitchViews(switch: Boolean) {
        isDelete = switch
        alphaValue = 0.5f
        notifyDataSetChanged()
    }

    fun allCheck(isCheck: Boolean) {
        isAllCheck = isCheck
        notifyDataSetChanged()
    }
}