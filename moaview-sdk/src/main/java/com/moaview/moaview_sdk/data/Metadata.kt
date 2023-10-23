package com.moaview.moaview_sdk.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class Entity(  // TODO ssin :: ContentsData로 이름 변경
    var id: Int,
    var title: String,
    var createDate: Date,
    var readDate: Date,
    var contentsType: String,
    var coverImage: String,
    var currentPage: Int,
    var totalPage: Int,
    var fileSize: String,
) : java.io.Serializable

data class MetaListData(
    @get:JsonProperty("META_LIST")
    var META_LIST : ArrayList<MetaData>
)

data class MetaData(
    @get:JsonProperty("title")
    var title: String,
    @get:JsonProperty("width")
    var width: Int,
    @get:JsonProperty("height")
    var height: Int,
    @get:JsonProperty("path")
    var path : String
)



