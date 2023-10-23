package com.moaview.moaview.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.moaview.moaview.db.ContentsEntity

@Dao
interface ContentsDao {

    @Query("SELECT * from CONTENTS_TABLE ORDER BY id ASC")
    fun getAll(): LiveData<List<ContentsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ContentsEntity): Long

    @Query("DELETE FROM CONTENTS_TABLE")
    fun deleteAll()

    @Update
    fun update(entity: ContentsEntity)

    @Delete
    fun delete(entity: ContentsEntity)

    @Query("delete from CONTENTS_TABLE where id in (:idList)")
    fun deleteContents(idList: List<Int>): Int

    @Query("SELECT * FROM CONTENTS_TABLE WHERE id == :key")
    suspend fun selectContents(key : Int): ContentsEntity

    @Query("SELECT * FROM CONTENTS_TABLE WHERE title LIKE :word")
     fun selectSearchResult(word : String?): LiveData<List<ContentsEntity>>
}