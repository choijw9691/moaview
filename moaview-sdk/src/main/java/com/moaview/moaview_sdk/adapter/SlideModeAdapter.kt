package com.moaview.moaview_sdk.adapter

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.moaview.moaview_sdk.R
import com.moaview.moaview_sdk.common.OnTouchAreaListener
import com.moaview.moaview_sdk.data.MetaData
import com.moaview.moaview_sdk.common.PageModeSettingType
import com.moaview.moaview_sdk.databinding.ItemSlideBinding
import com.moaview.moaview_sdk.view.activity.ViewerActivity
import com.moaview.moaview_sdk.util.CommonUtil
import com.moaview.moaview_sdk.util.ScreenSize
import java.io.File
import kotlin.math.roundToInt

class SlideModeAdapter(
    val context: Context
) : RecyclerView.Adapter<SlideModeAdapter.ViewHolder>() {

    private lateinit var callBackListener: OnTouchAreaListener
    var position = 0
    var weight: Double = 0.0
    var width: Double = 0.0
    var height: Double = 0.0
    var list = ArrayList<ArrayList<MetaData>>()

    inner class ViewHolder(var binding: ItemSlideBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ArrayList<MetaData>) {
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemSlideBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        this.position = position

        holder.binding.customScrollView.verticalScrollbarPosition = 0
        holder.binding.customScrollView.setChangePageCallback(callBackListener)

        for (i in 0 until list[position].size) {

            var imageView = ImageView(context)
            if (holder.binding.childLinearLayout.getChildAt(i) != null) {
                imageView = (holder.binding.childLinearLayout.getChildAt(i) as ImageView)
            }
            if (ViewerActivity.ORIENTATION == Configuration.ORIENTATION_LANDSCAPE) {
                if (!ViewerActivity.settingData.twoPageModeInOrientation) {    //가로모드 1page
                    weight =
                        (ScreenSize.getScreenWidth(context).toDouble() / list[position][i].width)
                    height = (list[position][i].height * weight)
                    width = ScreenSize.getScreenWidth(context).toDouble()

                } else if (ViewerActivity.settingData.twoPageModeInOrientation) { //가로모드 2page
                    weight =
                        ScreenSize.getScreenHeight(context).toDouble() / list[position][i].height
                    width = list[position][i].width * weight
                    height = ScreenSize.getScreenHeight(context).toDouble()
                }
            } else { //세로모드
                weight =
                    ScreenSize.getScreenWidth(context).toDouble() / list[position][i].width
                height = list[position][i].height * weight
                width = ScreenSize.getScreenWidth(context).toDouble()
                if (height > ScreenSize.getScreenHeight(context).toDouble()) {
                    width -= (height - ScreenSize.getScreenHeight(context)
                        .toDouble()).roundToInt()
                    height = ScreenSize.getScreenHeight(context).toDouble()
                }
            }

            if (holder.binding.childLinearLayout.getChildAt(i) == null) {
                holder.binding.childLinearLayout.addView(imageView)
            }

            var param = imageView.layoutParams
            param.width = width.toInt()
            param.height = height.toInt()
            imageView.layoutParams = param

            var bitmap = CommonUtil.pathToBitmap(File(list[position][i].path).path)?.let {
                Bitmap.createScaledBitmap(
                    it, width.roundToInt(), height.roundToInt(), false
                )
            }
            if (ViewerActivity.ORIENTATION == Configuration.ORIENTATION_LANDSCAPE &&
                ViewerActivity.settingData.twoPageModeInOrientation &&
                ViewerActivity.settingData.pageMode == PageModeSettingType.SLIDE.toString() && list[list.size - 1].size == 1 && position == list.size - 1
            ) {
                var emptyImageView = ImageView(context)
                if(holder.binding.childLinearLayout.childCount>1){
                    (holder.binding.childLinearLayout.removeViewAt(1))
                }
                holder.binding.childLinearLayout.addView(emptyImageView)
                var param = emptyImageView.layoutParams
                param.width = ScreenSize.getScreenWidth(context) / 2
                param.height = ScreenSize.getScreenHeight(context)
                emptyImageView.layoutParams = param
            }
            Glide.with(context).asBitmap().load(bitmap).listener(object : RequestListener<Bitmap> {
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
        var array = context.resources.getStringArray(R.array.colorArray)
        holder.binding.childLinearLayout.setBackgroundColor(Color.parseColor(array[ViewerActivity.settingData.colorType]));
    }

    fun setChangePageCallback(callback: OnTouchAreaListener) {
        callBackListener = callback
    }

    fun setListArray(list: ArrayList<ArrayList<MetaData>>) {
        this.list = list
    }

    override fun getItemCount(): Int {
        return list.size
    }
}