package com.moaview.moaview.util

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.moaview.moaview.R
import com.moaview.moaview.db.ContentsEntity
import com.moaview.moaview.common.SortState
import com.moaview.moaview.view.activity.HomeActivity.Companion.rootPath
import com.moaview.moaview.view.fragment.CommonDialogFragment
import com.moaview.moaview.view.fragment.LoadingDialogFragment
import com.moaview.moaview.common.FILE_FORMAT_JPG
import com.moaview.moaview.common.FILE_FORMAT_PNG
import com.moaview.moaview.common.getFileExtension
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.Normalizer
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object CommonUtil {

    private val dialog: CommonDialogFragment by lazy {
        CommonDialogFragment(R.layout.dialog_loading, {}, { viewDialog, View -> })
    }

    fun getFileFromUri(context: Context, uri: Uri?, name: String): String {
        var title = ""
        try {
            val cursor = context.contentResolver.query(uri!!, null, null, null, null)
            if (cursor != null) {
                cursor.moveToNext()
                title = Normalizer.normalize(
                    cursor.getString(cursor.getColumnIndexOrThrow(name)),
                    Normalizer.Form.NFC
                )
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return title
        }
        return title
    }

    fun copyFile(
        context: Context,
        uri: Uri?,
        list: List<ContentsEntity>,
        dialog: LoadingDialogFragment
    ): ContentsEntity {
        var fileName = getFileFromUri(context, uri, OpenableColumns.DISPLAY_NAME)
        val ext = fileName.getFileExtension()
        val title = fileName.split(".${ext}".toRegex()).toTypedArray()[0]
        var path = context.getExternalFilesDir(null)?.absolutePath + File.separator + title
        var targetFile = File(path) // 대상폴더

        // TODO ssin :: 파일을 삭제 안하고 안에 내용 바꾸고 불러오기하면..?
        var tempContent = list.find { it ->
            it.title == title && it.contentsType == ".$ext"
        }

        if (targetFile.exists() && tempContent != null) {
            return tempContent
        } else {
            targetFile.mkdir()
        }

        var imageCount = unZip(context, uri!!, targetFile, dialog)

        var fileNameList = ArrayList<String>()
        for (i in getChildImagePathList(rootPath+title)) {
            fileNameList.add(File(i).path)
        }

        return ContentsEntity(
            title,
            Date(System.currentTimeMillis()),
            Date(System.currentTimeMillis()),
            ".${ext}",
            getCoverImagePath(fileNameList),
            1,
            imageCount,
            getFolderSize(targetFile).toString()
        )
    }

    private fun unZip(
        context: Context,
        uri: Uri,
        unzipRootDirectory: File,
        dialog: LoadingDialogFragment
    ): Int {

        var imageCount = 0
        var loadingCount = 0

        unzipRootDirectory.mkdir()

        val inputStream = context.contentResolver.openInputStream(uri)
        var totalCount =  context.contentResolver.openInputStream(uri)?.let { getFileCount(it) }

        ZipInputStream(inputStream).use { zipInputStream ->
            var zipEntry: ZipEntry = zipInputStream.nextEntry
            while (zipEntry != null) {
                val targetFile = File(unzipRootDirectory, zipEntry.name)
                val ext: String = zipEntry.name.substring(zipEntry.name.lastIndexOf(".") + 1)
                if (zipEntry.name.contains("__MACOSX") || zipEntry.name.contains("DS_Store")) {
                    zipEntry = zipInputStream.nextEntry ?: break
                    continue
                }

                if (totalCount != null) {
                    loadingCount++
                    dialog.updateData(loadingCount,totalCount)
                }

                if (ext.equals(FILE_FORMAT_JPG, true) || ext.equals(FILE_FORMAT_PNG, true)) {
                    imageCount++
                }

                if (zipEntry.isDirectory) {
                    if (!targetFile.exists()) targetFile.mkdir()
                } else {
                    if (!File(
                            unzipRootDirectory.path,
                            zipEntry.name
                        ).parentFile.exists()
                    ) {
                        File(
                            unzipRootDirectory.path,
                            zipEntry.name
                        ).parentFile.mkdir()
                    }
                    FileOutputStream(
                        File(
                            unzipRootDirectory.path,
                            zipEntry.name
                        )
                    ).use { fileOutputStream ->
                        val buffer = ByteArray(8192)
                        var len = zipInputStream.read(buffer)
                        while (len != -1) {
                            fileOutputStream.write(buffer, 0, len)
                            len = zipInputStream.read(buffer)
                        }
                        zipInputStream.closeEntry()
                    }

                }
                zipEntry = zipInputStream.nextEntry ?: break
            }
        }
        return imageCount
    }



/*
    fun getChildImagePathList(path: String): ArrayList<String> {
        var fileList = ArrayList<String>()
        var getDirectory = rootPath + path
        var file = File(getDirectory)
        if (file.exists()) {
            var filelist = file.listFiles()
            Arrays.sort(filelist)
            for (i in filelist.indices) {
                if (filelist[i].name.contains(".")) {
                    val ext = filelist[i].name.getFileExtension()
                    if (ext.equals(FILE_FORMAT_JPG, true) || ext.equals(FILE_FORMAT_PNG, true)) {
                        fileList.add(filelist[i].path)
                    }
                }
            }
        }
        return fileList
    }

*/


   fun getChildImagePathList(path: String): ArrayList<String> {
        var fileList = ArrayList<String>()
        var file = File(path)
        if (file.exists()) {
            var filelist = file.listFiles()
            Arrays.sort(filelist)
            for (i in filelist.indices) {
                if (filelist[i]!!.isFile) {
                    if(filelist[i]!!.name.endsWith(FILE_FORMAT_JPG, true) ||
                        filelist[i]!!.name.endsWith(FILE_FORMAT_PNG, true)) {
                        fileList.add(filelist[i]!!.path)
                    }
                } else if (filelist[i]!!.isDirectory) {
                    fileList += getChildImagePathList(filelist[i]!!.path) // 재귀함수 호출
                }
            }
        }
        return fileList
    }


    fun getCoverImagePath(list: ArrayList<String>): String {
        for (i in list.indices) {
            val ext = list[i].getFileExtension()
            if (ext.equals(FILE_FORMAT_JPG, true) || ext.equals(FILE_FORMAT_PNG, true)) {
                return list[i]
            }
        }
        return ""
    }

    fun convertByte(size_bytes: Long): String {
        val cnt_size: String
        val size_kb = size_bytes / 1024
        val size_mb = size_kb / 1024
        val size_gb = size_mb / 1024

        cnt_size = if (size_gb > 0) {
            "$size_gb GB"
        } else if (size_mb > 0) {
            "$size_mb MB"
        } else {
            "$size_kb KB"
        }
        return cnt_size
    }

    fun getFolderSize(folder: File): Long {
        var length: Long = 0
        val files = folder.listFiles()
        val count = files.size
        for (i in 0 until count) {
            length += if (files[i].isFile) {
                files[i].length()
            } else {
                getFolderSize(files[i])
            }
        }
        return length
    }

    fun deleteDirectory(path: String) {
        val file = File(path)
        try {
            file.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sortList(
        list: MutableList<ContentsEntity>,
        state: SortState
    ): MutableList<ContentsEntity> {
        var isReverse = true
        val comparator: Comparator<ContentsEntity> = when (state) {
            SortState.READ -> {
                isReverse = true
                compareBy {
                    it.readDate
                }
            }
            SortState.TITLE -> {
                isReverse = false
                compareBy { it.title }
            }
            SortState.CREATED -> {
                isReverse = true
                compareBy { it.createDate }
            }
            SortState.FILESIZE -> {
                isReverse = true
                compareBy {
                    it.fileSize.toLong()
                }
            }
        }

        list.sortWith(comparator)
        if (isReverse) {
            list.reverse()
        }
        return list
    }

//    fun searchList(list: List<ContentsEntity>, word: String): List<ContentsEntity> {
//        return list.filter {
//            it.title.contains(word)
//        }
//    }

    fun calculateSpanCount(context: Context, columnDp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return (dpWidth / columnDp).toInt()
    }

    fun setFileRename(orgFile: File, updateFile: File) {
        try {
            if (!orgFile.exists()) {
                throw NoSuchFileException(orgFile, null, "Source file doesn't exist")
            }

            if (updateFile.exists()) {
                throw FileAlreadyExistsException(orgFile, null, "Destination file already exist")
            }

            val success = orgFile.renameTo(updateFile)
            if (success) {
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getFileCount(inputStream: InputStream):Int {
        var count = 0
        ZipInputStream(inputStream).use { zipInputStream ->
            var zipEntry: ZipEntry = zipInputStream.nextEntry
            while (zipEntry != null) {
                if (!zipEntry.name.contains("__MACOSX") && !zipEntry.name.contains("DS_Store")) {
                    val ext = zipEntry.name.getFileExtension()
                    if (ext.equals(FILE_FORMAT_JPG, true) || ext.equals(FILE_FORMAT_PNG, true)) {
                        count++
                    }
                }
                zipEntry = zipInputStream.nextEntry ?: break
            }
        }
        return count
    }

    fun hideKeyBoard(activity : Activity,view : View){
        val imm: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}