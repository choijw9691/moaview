package com.moaview.moaview.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "CONTENTS_TABLE")
data class ContentsEntity(
    var title: String,
    var createDate: Date,
    var readDate: Date,
    var contentsType: String,
    var coverImage: String,
    var currentPage: Int,
    var totalPage: Int,
    var fileSize: String
):java.io.Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var downLoad = false
    var opened  = false
}
