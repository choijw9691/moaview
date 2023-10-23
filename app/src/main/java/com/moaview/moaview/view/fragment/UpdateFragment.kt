package com.moaview.moaview.view.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.moaview.moaview.R
import com.moaview.moaview.adapter.UpdateImageListAdapter
import com.moaview.moaview.adapter.ViewPagerAdapter
import com.moaview.moaview.databinding.FragmentUpdateBinding
import com.moaview.moaview.common.ViewPagerMultipleHolder
import com.moaview.moaview.util.CommonUtil
import com.moaview.moaview.view.WrapContentGridLayoutManager
import com.moaview.moaview.view.WrapContentLinearLayoutManager
import com.moaview.moaview.view.activity.HomeActivity
import com.moaview.moaview.viewmodel.ContentsViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.io.File
import java.io.IOException


class UpdateFragment : BaseFragment<FragmentUpdateBinding>(FragmentUpdateBinding::inflate) {

    private val viewModel: ContentsViewModel by activityViewModels()
    private lateinit var updateTitle: String
    private lateinit var orgImagePath: String
    private lateinit var updateImagePath: String

    private val dialog: CommonDialogFragment by lazy {
        CommonDialogFragment(R.layout.dialog_viewall, {
            dialog.setTotalView()

            it.findViewById<ImageView>(R.id.backButton).setOnClickListener {
                dialog.dismiss()

            }
            it.findViewById<TextView>(R.id.totalTextView).text = "전체 " + pathList.size.toString()
        }, { dialogView, View ->
            if (View.id == R.id.sendButton) {
                dialog.dismiss()
            }
        })
    }

    var pathList = java.util.ArrayList<String>()
    lateinit var adapter: UpdateImageListAdapter
    var isEditMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleEditText.clearFocus()

        adapter = UpdateImageListAdapter { path ->
            var detailDialog = setDetailDialog(path)
            detailDialog.path = path
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        updateTitle = viewModel.updateContent.value?.title.toString()
        binding.titleEditText.setText(updateTitle)

        val linearLayoutManager = context?.let { WrapContentLinearLayoutManager(it) }
        linearLayoutManager?.orientation = LinearLayoutManager.HORIZONTAL
        binding.recyclerview.layoutManager = linearLayoutManager
        binding.recyclerview.adapter = adapter
        binding.totalTextView.setOnClickListener {
            if (!dialog.isAdded) {
                dialog.show(requireActivity().supportFragmentManager, null)
            }
        }
        binding.titleEditText.apply {
            setOnClickListener {
                if (isEditMode) {
                    hideKeyBoard()
                } else {
                    showKeyBoard()
                }
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {

                    if (text.toString() == updateTitle) {
                        binding.editButton.visibility = View.GONE
                        if (orgImagePath == updateImagePath) {
                            disabledButton()
                        }
                    } else {
                        binding.editButton.visibility = View.VISIBLE
                    }
                }
            })

        }

        orgImagePath =
        viewModel.updateContent.value?.coverImage.toString()
        updateImagePath = orgImagePath
        Glide.with(this)
            .load(orgImagePath)
            .into(binding.coverImageView)


        pathList = CommonUtil.getChildImagePathList(
            HomeActivity.rootPath + viewModel.updateContent.value?.title
        )

        adapter.submitList(pathList)

        binding.updateButton.setOnClickListener {
            if (isEditMode) {
                Toast.makeText(context, "제목 설정을 저장해 주세요.", Toast.LENGTH_SHORT).show()
            } else {
                var orgFile = File(HomeActivity.rootPath + viewModel.updateContent.value?.title)
                var updateFile = File(HomeActivity.rootPath + binding.titleEditText.text.toString())
                var updateEntity = viewModel.updateContent.value
                if (orgFile.name == binding.titleEditText.text.toString()) {
                    if (orgImagePath != updateImagePath) {
                        updateEntity?.coverImage = File(updateImagePath).path
                        updateEntity?.let { it1 -> viewModel.update(it1) }
                    }
                    requireActivity().onBackPressed()
                } else {
                    try {
                        CommonUtil.setFileRename(orgFile, updateFile)
                        updateImagePath = updateImagePath.replaceFirst(updateEntity?.title.toString(),updateFile.name)
                        updateEntity?.coverImage = File(updateImagePath).path
                        updateEntity?.title = updateFile.name
                        updateEntity?.let { it1 -> viewModel.update(it1) }
                    } catch (e: NoSuchFileException) {
                        Log.d("JIWOUNG", "errorcheck: " + "NoSuchFileException")
                    } catch (e: FileAlreadyExistsException) {
                        Log.d("JIWOUNG", "errorcheck: " + "FileAlreadyExistsException")
                    } catch (e: IOException) {
                        Log.d("JIWOUNG", "errorcheck: " + "IOException")
                    }
                    requireActivity().onBackPressed()
                }
            }
        }
        binding.container.setOnClickListener {
            hideKeyBoard()
        }

        binding.editButton.setOnClickListener {
            binding.titleEditText.setText(updateTitle)
            binding.editButton.visibility = View.GONE
            if (orgImagePath == updateImagePath) {
                disabledButton()
            }
        }
    }

    fun CommonDialogFragment.setTotalView() {
        var adapter = UpdateImageListAdapter() { path ->
            setDetailDialog(path)
        }
        val linearLayoutManager =
            WrapContentGridLayoutManager(
                dialogView.context,
                CommonUtil.calculateSpanCount(dialogView.context, 100)
            )
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        adapter.submitList(pathList)
    }

    private fun showUpdateButton() {
        binding.updateButton.isEnabled = true
        binding.updateButton.backgroundTintList = context?.let {
            ContextCompat.getColor(
                it,
                R.color.color_f45b7f
            )
        }?.let {
            ColorStateList.valueOf(
                it
            )
        }
    }

    private fun disabledButton() {
        binding.updateButton.isEnabled = false
        binding.updateButton.backgroundTintList = context?.let {
            ContextCompat.getColor(
                it,
                com.moaview.moaview_sdk.R.color.color_b1b1b1
            )
        }?.let {
            ColorStateList.valueOf(
                it
            )
        }
    }

    private fun setCoverImage(path: String) {
        updateImagePath = path
        if (updateImagePath != orgImagePath) {
            showUpdateButton()
        } else if (updateTitle == binding.titleEditText.text.toString()) {
            disabledButton()
        }
        Glide.with(this)
            .load(path)
            .into(binding.coverImageView)
    }

    fun hideKeyBoard() {
        isEditMode = false
        binding.titleEditText.clearFocus()
        var imm =
            (activity as HomeActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.titleEditText.windowToken, 0)
        binding.titleEditText.text.toString()
        if (updateTitle != binding.titleEditText.text.toString()) {
            showUpdateButton()
            binding.editButton.visibility = View.VISIBLE
        }
    }

    fun showKeyBoard() {
        isEditMode = true
        binding.titleEditText.requestFocus()
        var imm =
            (activity as HomeActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.titleEditText, 0)
    }

    fun setDetailDialog(path: String): CommonDialogFragment {
        var updatePosition = 0
        var detailDialog = CommonDialogFragment(R.layout.dialog_detail_image, {
            var viewPager = it.findViewById<ViewPager2>(R.id.dialog_detail_image_viewPager)
            viewPager.adapter = ViewPagerAdapter(
                pathList as ArrayList<Unit>,
                ViewPagerMultipleHolder.DIALOG_DETAIL_IMAGE_VIEW_HOLDER
            )
            var toolbar =
                it.findViewById<MaterialToolbar>(R.id.topAppBar)
            for (position in 0 until pathList.size) {
                if (pathList[position] == path) {
                    viewPager.setCurrentItem(position, false)
                    toolbar.title = "${(position + 1)}/${pathList.size}"
                    updatePosition = position
                }
            }
            viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    toolbar.title = "${(position + 1)}/${pathList.size}"
                    updatePosition = position
                }
            })
        }, { dialogView, View ->
            if (View.id == R.id.sendButton) {
                setCoverImage(pathList[updatePosition])
                if (dialog.isAdded) {
                    dialog?.dismiss()
                }
            }
        })

        if (!detailDialog.isAdded) {
            detailDialog.show(requireActivity().supportFragmentManager, null)
        }
        return detailDialog
    }
}