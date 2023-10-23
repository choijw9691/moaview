package com.moaview.moaview.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.moaview.ViewerApplication
import com.moaview.moaview.R
import com.moaview.moaview.data.BookMarkList
import com.moaview.moaview.data.SettingData
import com.moaview.moaview.datastore.SettingDataStore
import com.moaview.moaview.common.SortState
import com.moaview.moaview.databinding.ActivityHomeBinding
import com.moaview.moaview.viewmodel.ContentsViewModel
import com.moaview.moaview.viewmodel.ContentsViewModelFactory
import com.moaview.moaview.view.fragment.BookListFragment
import com.moaview.moaview.view.fragment.GuideDialogFragment
import com.moaview.moaview.view.fragment.OptionFragment
import com.moaview.moaview.view.fragment.SearchFragment
import com.moaview.moaview.view.fragment.UpdateFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val viewModel: ContentsViewModel by viewModels()

    private var isStart = false

    companion object {
        var rootPath = ""
        var bookListFragmentSortState = SortState.READ
        lateinit var settingData: SettingData
        val CURRENT_PAGE = 2003103

    }
    override fun onBackPressed() {
        super.onBackPressed()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        rootPath = getExternalFilesDir(null)?.absolutePath + File.separator

        viewModel.test.observe(this) {

            if (savedInstanceState == null) {

                if (!isStart) {
                    isStart = true

                    val mapper = jacksonObjectMapper()

                    for (i in it) {
                        var file = File(rootPath + i.title, "bookmark.json")
                        if (file.exists()) {
                            var currentPage = mapper.readValue<BookMarkList>(file).CURRENT_PAGE

                            if (i.currentPage != currentPage) {
                                i.currentPage = currentPage
                            }
                        }
                    }
                    runBlocking {
                        var settingsManager = SettingDataStore(applicationContext)
                        settingData = SettingData(
                            settingsManager.listMode.first(),
                            settingsManager.sortState.first(),
                            settingsManager.tutorialState.first()
                        )
                        viewModel.bookListFragmentSort(
                            it.toMutableList(),
                            enumValueOf(settingData.sortState)
                        )

                        getSettingData()
                        if (!settingData.tutorialState){
                            GuideDialogFragment().show(supportFragmentManager, null)
                        }
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, BookListFragment()).commit();
                    }
                }
            }
        }
    }

    private val authorizationCheckResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                }
            }
        }

    fun authorizationCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    var intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.setData(
                        Uri.parse(
                            String.format(
                                "package:%s",
                                applicationContext.packageName
                            )
                        )
                    )
                    authorizationCheckResult.launch(intent)
                } catch (e: java.lang.Exception) {
                    Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1
                    )
                }
            }
        }
    }

    fun changeFragment(index: Int) {

        var targetFragment: Fragment? = null

        when (index) {
            0 -> {
                targetFragment = BookListFragment()
            }
            1 -> {
                targetFragment = SearchFragment()
            }
            2 -> {
                targetFragment = OptionFragment()
            }
            3 -> {
                targetFragment = UpdateFragment()
            }
        }

        if (targetFragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, targetFragment)
                .addToBackStack(null).commit()
        }
    }

    fun appFinish() {
        finish()
    }

    fun getSettingData() {
        CoroutineScope(Dispatchers.Main).launch {
            var settingsManager = SettingDataStore(applicationContext)
            settingsManager?.listMode?.collectLatest {
                settingData.listMode = it
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            var settingsManager = SettingDataStore(applicationContext)
            settingsManager?.sortState?.collectLatest {
                settingData.sortState = it
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            var settingsManager = SettingDataStore(applicationContext)
            settingsManager?.tutorialState?.collectLatest {
                settingData.tutorialState = it
            }
        }
    }
}