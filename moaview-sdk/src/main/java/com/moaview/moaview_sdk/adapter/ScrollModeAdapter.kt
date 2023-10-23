package com.moaview.moaview_sdk.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.moaview.moaview_sdk.data.MetaData
import com.moaview.moaview_sdk.databinding.ItemScrollBinding
import com.moaview.moaview_sdk.util.CommonUtil.pathToBitmap
import com.moaview.moaview_sdk.util.ScreenSize
import java.io.File

class ScrollModeAdapter(
    var mContext: Context,
    var orientation: Int
) : RecyclerView.Adapter<ScrollModeAdapter.ViewHolder>() {

    var weight: Double = 0.0
    var width: Double = 0.0
    var height: Double = 0.0
    var list = ArrayList<MetaData>()

    inner class ViewHolder(var binding: ItemScrollBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemScrollBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        weight = (ScreenSize.getScreenWidth(mContext).toDouble() / list[position].width)
        height = list[position].height * weight
        width = ScreenSize.getScreenWidth(mContext).toDouble()

        var imageView = holder.binding.imageView
        var param = imageView.layoutParams
        param.width = width.toInt()
        param.height = height.toInt()
        imageView.layoutParams = param

        var bitmap = pathToBitmap(File(list[position].path).path)?.let {
            Bitmap.createScaledBitmap(
                it, width.toInt(), height.toInt(), false
            )
        }
        Glide.with(mContext).asBitmap().load(bitmap).listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
        }).into(imageView)
    }

    fun setListArray(list: ArrayList<MetaData>) {
        this.list = list
    }

    override fun getItemCount(): Int {
        return list.size
    }
}