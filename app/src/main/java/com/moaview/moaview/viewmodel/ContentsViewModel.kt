package com.moaview.moaview.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.moaview.moaview.db.ContentsEntity
import com.moaview.moaview.db.ContentsRepository
import com.moaview.moaview.common.SortState
import com.moaview.moaview.common.State
import com.moaview.moaview.view.activity.HomeActivity
import com.moaview.moaview.util.CommonUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.HashMap
import javax.inject.Inject

@HiltViewModel
class ContentsViewModel @Inject constructor(private val contentsRepository: ContentsRepository) : ViewModel() {
    private val _allContents = MutableLiveData<List<ContentsEntity>>()
    val allContents: LiveData<List<ContentsEntity>> get() = _allContents

    val test: LiveData<List<ContentsEntity>> get() = contentsRepository.allContents // TODO :: ??

    private val _searchResultContents = MutableLiveData<List<ContentsEntity>>()
    val searchResultContents: LiveData<List<ContentsEntity>> get() = _searchResultContents

    private val _updateContent = MutableLiveData<ContentsEntity>()
    val updateContent: LiveData<ContentsEntity> get() = _updateContent

    private var inputText: MutableLiveData<String> = MutableLiveData()

    fun insert(entity: ContentsEntity) = viewModelScope.launch(Dispatchers.IO) {
        var rowId = contentsRepository.insert(entity)
        if (rowId > 0) {
            withContext(Main) {
                entity.id = rowId.toInt()
                var allContents = _allContents.value?.plus(entity)
                allContents?.let {
                    bookListFragmentSort(
                        it.toMutableList(),
                        HomeActivity.bookListFragmentSortState
                    )
                }
            }
        }
    }

    fun setUpdateContent(entity: ContentsEntity) = viewModelScope.launch {
        withContext(Main) {
            _updateContent.value = entity
        }
    }

    fun getSearchList(word: String): LiveData<List<ContentsEntity>> {
        return contentsRepository.searchResults(word)
    }

    fun update(entity: ContentsEntity) = viewModelScope.launch(Dispatchers.IO) {
        contentsRepository.update(entity)
        withContext(Main) {
            _allContents.value?.let {
                bookListFragmentSort(
                    it.toMutableList(),
                    HomeActivity.bookListFragmentSortState
                )
            }
        }
    }

    fun deleteContents(hashMap: HashMap<Int, Boolean>) = viewModelScope.launch(Dispatchers.IO) {
        var contentsList = ArrayList<ContentsEntity>()
        var idList = ArrayList<Int>()

        for (i in hashMap) {
            if (i.value) {
                var content = allContents.value?.find { it.id == i.key }
                contentsList.add(content!!)
                idList.add(content.id)
            }
        }
        if (contentsList.size > 0) {
            var state = contentsRepository.deleteContents(idList)
            when (state) {
                State.SUCCESS -> {
                    for (i in contentsList) {
                        CommonUtil.deleteDirectory(HomeActivity.rootPath + i.title)
                    }
                    withContext(Main) {
                        _allContents.value = _allContents.value?.minus(contentsList)
                    }
                }
                State.ERROR, State.EXCEPTION -> Log.d("JIWOUNG", "deleteContents " + "error")
            }
        }

    }

    fun deleteAll(entity: ContentsEntity) = viewModelScope.launch(Dispatchers.IO) {
        contentsRepository.delete(entity)
    }


    fun getAll(): LiveData<List<ContentsEntity>> {
        return allContents
    }

    fun bookListFragmentSort(
        list: MutableList<ContentsEntity>,
        state: SortState
    ) = viewModelScope.launch {
        HomeActivity.bookListFragmentSortState = state

        _allContents.value = CommonUtil.sortList(list, state)
    }
}