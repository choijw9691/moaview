package com.moaview.moaview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moaview.moaview.db.ContentsRepository

class ContentsViewModelFactory(private val contentsRepository: ContentsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContentsViewModel(contentsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
