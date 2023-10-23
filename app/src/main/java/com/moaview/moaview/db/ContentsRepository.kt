package com.moaview.moaview.db

import androidx.lifecycle.LiveData
import com.moaview.moaview.dao.ContentsDao
import com.moaview.moaview.common.State
import com.moaview.moaview.db.ContentsEntity

class ContentsRepository(private val contentsDao: ContentsDao) {

    val allContents: LiveData<List<ContentsEntity>> = contentsDao.getAll()

    fun insert(entity: ContentsEntity):Long {
        return contentsDao.insert(entity)
    }

    suspend fun selectContents(id: Int) : ContentsEntity {
        return contentsDao.selectContents(id)
    }

    fun update(entity: ContentsEntity) {
        contentsDao.update(entity)
    }

    fun delete(entity: ContentsEntity) {
        contentsDao.delete(entity)
    }

    fun deleteContents(idList: List<Int>): State {
        return try { if(contentsDao.deleteContents(idList) >0) State.SUCCESS else State.ERROR } catch(e:java.lang.Exception){ State.EXCEPTION }
    }

    fun searchResults(word : String):LiveData<List<ContentsEntity>>{
        return contentsDao.selectSearchResult("%${word}%")
    }
}