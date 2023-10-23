package com.moaview.moaview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.moaview.moaview.db.ContentsEntity
import com.moaview.moaview.databinding.ItemUpdateBinding


class UpdateImageListAdapter(
    val clickListener: (String) -> Unit
) : ListAdapter<String, RecyclerView.ViewHolder>(diffUtil) {

    inner class UpdateViewHolder(var binding: ItemUpdateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContentsEntity) {
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UpdateViewHolder(
            ItemUpdateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as UpdateViewHolder).binding.imageView.setOnClickListener {
            clickListener(currentList[position])
        }

        Glide.with(holder.binding.imageView.context)
            .load(currentList[position])
            .into(holder.binding.imageView)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}