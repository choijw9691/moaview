package com.moaview.moaview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.moaview.moaview.R
import com.moaview.moaview.common.ViewPagerMultipleHolder

class ViewPagerAdapter(
    dataList: ArrayList<Unit>,
    var holderMode: ViewPagerMultipleHolder
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var item = dataList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (holderMode) {
            ViewPagerMultipleHolder.GUIDE_VIEW_HOLDER -> {
                PagerViewHolder(parent)
            }
            ViewPagerMultipleHolder.DIALOG_DETAIL_IMAGE_VIEW_HOLDER -> {
                UpdateImageViewHolder(parent)
            }
        }
    }

    override fun getItemCount(): Int = item.size

    // 가이드 화면 뷰홀더
    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_viewpager, parent, false)
    )

    // 수정 화면 확대보기 이미지 뷰홀더
    inner class UpdateImageViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_dialog_detail_image, parent, false)
    )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PagerViewHolder -> {
                var imageView = holder.itemView.findViewById<ImageView>(R.id.imageView)
                var subTextView = holder.itemView.findViewById<TextView>(R.id.subTextView) as TextView
                when (position) {
                    0 -> subTextView.text =
                        subTextView.context.getString(R.string.sub_title_tutorial_step_3)
                    1 -> subTextView.text =
                        subTextView.context.getString(R.string.sub_title_tutorial_step_1)
                    2 -> subTextView.text =
                        subTextView.context.getString(R.string.sub_title_tutorial_step_2)
                }
                Glide.with(imageView.context)
                    .load((item as ArrayList<Int>)[position] )
                    .into(imageView)
            }

            is UpdateImageViewHolder -> {
              var imageView = holder.itemView.findViewById<ImageView>(R.id.detailImageView)
                Glide.with(imageView.context)
                    .load((item as ArrayList<String>)[position])
                    .transform(RoundedCorners(30))
                    .into(imageView)
            }
        }
    }
}