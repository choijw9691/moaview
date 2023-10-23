package com.moaview.moaview_sdk.view.activity

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moaview.moaview_sdk.adapter.SlideModeAdapter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.moaview.moaview_sdk.R
import com.moaview.moaview_sdk.adapter.ScrollModeAdapter
import com.moaview.moaview_sdk.common.BOOKMARK_FILE_NAME
import com.moaview.moaview_sdk.common.METADATA_FILE_NAME
import com.moaview.moaview_sdk.common.OnTouchAreaListener
import com.moaview.moaview_sdk.common.PageModeSettingType
import com.moaview.moaview_sdk.common.TouchArea
import com.moaview.moaview_sdk.common.onScrollListener
import com.moaview.moaview_sdk.data.BookMark
import com.moaview.moaview_sdk.data.BookMarkList
import com.moaview.moaview_sdk.data.Entity
import com.moaview.moaview_sdk.data.MetaData
import com.moaview.moaview_sdk.data.MetaListData
import com.moaview.moaview_sdk.data.ViewerSettingData
import com.moaview.moaview_sdk.databinding.ActivityViewerBinding
import com.moaview.moaview_sdk.datastore.UserSettingDataStore
import com.moaview.moaview_sdk.util.CommonUtil
import com.moaview.moaview_sdk.util.CommonUtil.showToastButton
import com.moaview.moaview_sdk.view.RecyclerLinearLayoutManager
import com.moaview.moaview_sdk.view.fragment.ViewerMenuDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class ViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewerBinding

    private lateinit var linearLayoutManager: RecyclerView.LayoutManager

    private lateinit var viewModeAdapter: SlideModeAdapter
    private lateinit var scrollModeAdapter: ScrollModeAdapter

    lateinit var menuDialogFragment: ViewerMenuDialogFragment

    companion object {
        lateinit var rootPath: String
        lateinit var bookData: Entity
        lateinit var settingData: ViewerSettingData
        var metaDataList = ArrayList<MetaData>()
        var bookMarkDataList = ArrayList<BookMark>()
        var historyStack = ArrayList<Int>()
        var ORIENTATION: Int = 0    // TODO ssin :: 왜 대문자여?
        var memoPage = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookData = intent.getSerializableExtra("Entity") as Entity
        rootPath =
            applicationContext.getExternalFilesDir(null)?.absolutePath + File.separator + bookData.title

        initMetaDataList()

        initBookmarkDataList()

        setSettingData(this)
    }

    private fun initMetaDataList() {

        if (metaDataList.isNotEmpty()) {
            metaDataList.clear()
        }

        var mataDataFile = File(rootPath, METADATA_FILE_NAME)
        if (mataDataFile.exists()) {
            metaDataList =
                jacksonObjectMapper().readValue<MetaListData>(mataDataFile).META_LIST // 자바8 이상의 메서드를 사용하려면 sdk 26이상이어야함
        } else {
            var titleList = CommonUtil.getChildImagePathList(rootPath)
            for (i in titleList) {
                val option = BitmapFactory.Options()
                option.inJustDecodeBounds = true
                BitmapFactory.decodeFile(i, option)
                metaDataList.add(MetaData(File(i).name, option.outWidth, option.outHeight, i))
            }
            CommonUtil.writeJsonToFile(mataDataFile, MetaListData(metaDataList))
        }
    }

    private fun initBookmarkDataList() {
        var bookmarkFile = File(rootPath, BOOKMARK_FILE_NAME)
        if (bookmarkFile.exists()) {
            bookMarkDataList =
                jacksonObjectMapper().readValue<BookMarkList>(bookmarkFile).BOOKMARK_LIST
        }
    }

    private fun setViewerAdapter() {
        // adapter 관련 init
        var list = ArrayList<ArrayList<MetaData>>()
        if (Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {   // 회전 90 / 270
            ORIENTATION = Configuration.ORIENTATION_LANDSCAPE
            if (settingData.pageMode == PageModeSettingType.SLIDE.name) {    // 슬라이드 보기 시
                if (settingData.twoPageModeInOrientation) {   // 가로모드 두 페이지 보기 시
                    viewModeAdapter = SlideModeAdapter(this)
                    linearLayoutManager =
                        RecyclerLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false).apply {
                            setHorizontalScrollEnabled(false)
                            orientation = LinearLayoutManager.VERTICAL
                        }
                    binding.recyclerview.layoutManager = linearLayoutManager
                    binding.recyclerview.adapter = viewModeAdapter
                    for (i in 0 until metaDataList.size step 2) {
                        var l = ArrayList<MetaData>()
                        l.add(metaDataList[i])
                        if ((metaDataList.size % 2 == 0)) {
                            l.add(metaDataList[i + 1])
                        } else {
                            if (i == metaDataList.size - 1) {
                                //    l.add(intent[i+1])
                            } else {
                                l.add(metaDataList[i + 1])
                            }
                        }
                        list.add(l)
                    }
                } else {    // 가로모드 한 페이지 보기 시
                    viewModeAdapter = SlideModeAdapter(this)
                    linearLayoutManager =
                        RecyclerLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    (linearLayoutManager as RecyclerLinearLayoutManager).setHorizontalScrollEnabled(false)
                    binding.recyclerview.layoutManager = linearLayoutManager
                    binding.recyclerview.adapter = viewModeAdapter
                    for (i in metaDataList) {
                        var l = ArrayList<MetaData>()
                        l.add(i)
                        list.add(l)
                    }
                }
            } else {    // 스크롤 보기 시
                scrollModeAdapter = ScrollModeAdapter(this, 0)
                linearLayoutManager = RecyclerLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                (linearLayoutManager as RecyclerLinearLayoutManager).setVerticalScrollEnabled(true)
                binding.recyclerview.layoutManager = linearLayoutManager
                binding.recyclerview.adapter = scrollModeAdapter
                list.add(metaDataList)
            }
        } else {     // 회전 0 / 180
            ORIENTATION = Configuration.ORIENTATION_PORTRAIT
            if (settingData.pageMode == PageModeSettingType.SCROLL.name) {
                scrollModeAdapter = ScrollModeAdapter(this, 1)
                linearLayoutManager = RecyclerLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                (linearLayoutManager as RecyclerLinearLayoutManager).setVerticalScrollEnabled(true)
                binding.recyclerview.adapter = scrollModeAdapter
                binding.recyclerview.layoutManager = linearLayoutManager
                list.add(metaDataList)
            } else {
                viewModeAdapter = SlideModeAdapter(this)
                linearLayoutManager =
                    RecyclerLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false).apply {
                        setHorizontalScrollEnabled(false)
                        orientation = LinearLayoutManager.HORIZONTAL
                    }
                binding.recyclerview.layoutManager = linearLayoutManager
                binding.recyclerview.adapter = viewModeAdapter
                (linearLayoutManager as RecyclerLinearLayoutManager).setHorizontalScrollEnabled(false)
                for (i in metaDataList) {
                    var l = ArrayList<MetaData>()
                    l.add(i)
                    list.add(l)
                }
            }
        }

        if (settingData.pageMode == PageModeSettingType.SCROLL.toString()) {
            binding.recyclerview.setChangePageCallback(onTouchAreaListener)
            binding.recyclerview.setOnScrollChangeListener(onScrollChangedListener)
            scrollModeAdapter.setListArray(list[0])
        } else {
            viewModeAdapter.setChangePageCallback(onTouchAreaListener)
            viewModeAdapter.setListArray(list)
        }

        customScrollToPosition(getContentsScrollIndex(bookData.currentPage))
        menuDialogFragment = ViewerMenuDialogFragment()
    }

    fun getRecyclerViewItemPosition(): Int {
        return (linearLayoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    private fun setSettingData(context: Context) {
        settingData = ViewerSettingData()

        runBlocking {
            CoroutineScope(Dispatchers.Main).launch {
                var settingsManager = UserSettingDataStore(context)
                settingsManager.pageMode.collectLatest {
                    settingData.pageMode = it
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                var settingsManager = UserSettingDataStore(context)
                settingsManager.pageTurnSettingType.collectLatest {
                    settingData.pageTurnSettingType = it
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                var settingsManager = UserSettingDataStore(context)
                settingsManager.twoPageModeInOrientation.collectLatest {
                    settingData.twoPageModeInOrientation = it
                    setViewerAdapter()
                }
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            var settingsManager = UserSettingDataStore(context)
            settingsManager.pageTurnVolumeKey.collectLatest {
                settingData.pageTurnVolumeKey = it
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            var settingsManager = UserSettingDataStore(context)
            settingsManager.hideSoftKey.collectLatest {
                settingData.hideSoftKey = it
                CommonUtil.setFullScreen(this@ViewerActivity)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            var settingsManager = UserSettingDataStore(context)
            settingsManager.screenAlwaysOn.collectLatest {
                settingData.screenAlwaysOn = it
                if (it) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            var settingsManager = UserSettingDataStore(context)
            settingsManager.fixedScreenRotation.collectLatest {
                settingData.fixedScreenRotation = it
                requestedOrientation = if (it) {
                    when (ORIENTATION) {
                        Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            var settingsManager = UserSettingDataStore(context)
            settingsManager.pageColorType.collectLatest {
                settingData.colorType = it
                binding.recyclerview.adapter?.notifyDataSetChanged()
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            var settingsManager = UserSettingDataStore(context)
            settingsManager.systemBrightNessCheck.collectLatest {
                settingData.systemBrightNessCheck = it
                if (!it) {
                    window?.let {
                        settingsManager.appBrightNessValue.first().let { it1 ->
                            CommonUtil.setBrightness(it1, it)
                        }
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            var settingsManager = UserSettingDataStore(context)
            settingsManager.appBrightNessValue?.collectLatest {
                settingData.appBrightNessValue = it
            }
        }
    }

    fun jumpPage(page: Int) {
        setHistoryStack(bookData.currentPage)
        customScrollToPosition(getContentsScrollIndex(page))
    }

    fun popHistoryStack(): Int {
        if (historyStack.isNotEmpty()) {
            var page = historyStack[historyStack.size - 1]
            customScrollToPosition(getContentsScrollIndex(page))
            historyStack.removeAt(historyStack.size - 1)
            if (historyStack.isEmpty()) {
                menuDialogFragment.setVisibleHistoryButton(false)
            }
            return page
        }
        return -1
    }

    private fun setHistoryStack(page: Int) {
        menuDialogFragment.setVisibleHistoryButton(true)
        if (historyStack.size < 3) {
            historyStack.add(page)
        } else {
            historyStack.removeAt(0)
            historyStack.add(page)
        }
    }

    // param 페이지 북마크 존재 여부 판단 및 ui 처리
    private fun isExistBookmarkData(bookmarkPage: Int): BookMark? {
        return bookMarkDataList.find { i ->
            i.bookmark_page == bookmarkPage
        }
    }

    // 북마크 존재 여부 판단 및 ui 처리
    fun getCurrentVisibleBookMark(scrollIndex: Int): Boolean {
        var contentsPage =
            getContentsPage(scrollIndex, isLeft = false, isRight = isTwoPageViewing())
        if (isExistBookmarkData(contentsPage) == null) {
            binding.bookmarkButton.visibility = View.GONE
        } else {
            if (metaDataList.size % 2 != 0 && contentsPage == metaDataList.size) {
                binding.bookmarkButton.visibility = View.GONE
            } else {
                binding.bookmarkButton.visibility = View.VISIBLE
            }
        }
        if (isTwoPageViewing()) {
            contentsPage = getContentsPage(scrollIndex, isLeft = true, isRight = false)
            if (isExistBookmarkData(contentsPage) == null) binding.bookmarkLeftButton.visibility =
                View.GONE else binding.bookmarkLeftButton.visibility = View.VISIBLE
            return binding.bookmarkLeftButton.visibility == View.VISIBLE
        } else {
            return binding.bookmarkButton.visibility == View.VISIBLE
        }
    }

    fun setBookMark(touchArea: TouchArea) {
        var existBookmarkData: BookMark?
        var bookmarkPage = 0
        if (touchArea == TouchArea.BOOKMARK_RIGHT) {
            bookmarkPage = getContentsPage(
                getRecyclerViewItemPosition(),
                isLeft = false,
                isRight = isTwoPageViewing()
            )
        }
        if (isTwoPageViewing() && touchArea == TouchArea.BOOKMARK_LEFT) {
            bookmarkPage =
                getContentsPage(getRecyclerViewItemPosition(), isLeft = true, isRight = false)
        }

        existBookmarkData = bookMarkDataList.find { i ->
            i.bookmark_page == bookmarkPage
        }

        if (existBookmarkData == null) {
            bookMarkDataList.add(
                BookMark(
                    System.currentTimeMillis(),
                    bookmarkPage,
                    ""
                )
            )
            CommonUtil.writeBookMarkFile()
        } else {
            if (existBookmarkData.bookmark_memo.isEmpty()) {
                bookMarkDataList.remove(existBookmarkData)
                CommonUtil.writeBookMarkFile()
            } else {
                CommonUtil.showCustomDialog(
                    this,
                    getString(R.string.dialog_exist_memo_delete),
                    getString(R.string.no),
                    getString(R.string.yes),
                    {
                    },
                    {
                        bookMarkDataList.remove(existBookmarkData)
                        CommonUtil.writeBookMarkFile()
                        getCurrentVisibleBookMark(getRecyclerViewItemPosition())
                    },
                    true
                )
            }
        }
        getCurrentVisibleBookMark(getRecyclerViewItemPosition())
    }

    fun showLastPageDialog() {
        CommonUtil.showCustomDialog(
            ViewerActivity@ this,
            getString(R.string.dialog_last_page),
            getString(R.string.dialog_last_page_left_button),
            getString(R.string.dialog_last_page_right_button),
            {
                customScrollToPosition(getContentsScrollIndex(1))
            },
            {
                ViewerActivity@ this.finish()
            }, true
        )
    }

    // 페이지 이동 및 후처리
    fun customScrollToPosition(scrollIndex: Int) {
        if (scrollIndex < 0) {
            showToastButton(binding.toastButton)
        } else if (scrollIndex > getContentsScrollIndex(metaDataList.size)) {
            showLastPageDialog()
        } else {
            binding.recyclerview.scrollToPosition(scrollIndex)
            bookData.currentPage =
                getContentsPage(scrollIndex, isLeft = isTwoPageViewing(), isRight = false)
            getCurrentVisibleBookMark(scrollIndex)
            CommonUtil.writeBookMarkFile()
        }
    }

    private fun isTwoPageViewing(): Boolean {
        return settingData.pageMode == PageModeSettingType.SLIDE.name &&
                settingData.twoPageModeInOrientation &&
                ORIENTATION == Configuration.ORIENTATION_LANDSCAPE
    }

    private fun getContentsPage(scrollIndex: Int, isLeft: Boolean, isRight: Boolean): Int {
        var currentContentsPage = scrollIndex + 1
        if (isTwoPageViewing()) {
            if (isLeft) {
                currentContentsPage = scrollIndex * 2 + 1
            }
            if (isRight) {
                currentContentsPage = scrollIndex * 2 + 2
                if (currentContentsPage > metaDataList.size) currentContentsPage -= 1
            }
        }
        return currentContentsPage
    }

    // 실제 페이지 정보 -> 컴포넌트 인덱스 변환
    private fun getContentsScrollIndex(contentsPage: Int): Int {
        return if (isTwoPageViewing()) (contentsPage - 1) / 2 else contentsPage - 1
    }

    // 사용자 터치 영역 콜백
    private var onTouchAreaListener = object : OnTouchAreaListener {
        override fun onTouch(touchArea: TouchArea) {
            if (touchArea == TouchArea.LEFT) {
                customScrollToPosition(getRecyclerViewItemPosition() - 1)
            } else if (touchArea == TouchArea.RIGHT) {
                customScrollToPosition(getRecyclerViewItemPosition() + 1)
            } else if (touchArea == TouchArea.CENTER) {
                if (!menuDialogFragment.isAdded && !supportFragmentManager.isDestroyed) {
                    menuDialogFragment.show(supportFragmentManager, null)
                }
            } else if (touchArea == TouchArea.BOOKMARK_LEFT || touchArea == TouchArea.BOOKMARK_RIGHT) {
                setBookMark(touchArea)
            }
        }
    }

    // 스크롤 상태 리스너
    private var onScrollChangedListener = object : onScrollListener {
        override fun onScrollFinish() {
            bookData.currentPage = getRecyclerViewItemPosition() + 1
            getCurrentVisibleBookMark(getRecyclerViewItemPosition())
            CommonUtil.writeBookMarkFile()
        }

        override fun onScrollTop() {
            showToastButton(binding.toastButton)
        }

        override fun onScrollBottom() {
            showLastPageDialog()
        }
    }
}