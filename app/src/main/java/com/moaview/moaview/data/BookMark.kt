package com.moaview.moaview.data

import com.fasterxml.jackson.annotation.JsonProperty

data class BookMark(
   @get:JsonProperty("bookmark_date")
   var bookmark_date : Long,
   @get:JsonProperty("bookmark_page")
   var bookmark_page : Int,
   @get:JsonProperty("bookmark_memo")
   var bookmark_memo : String
):java.io.Serializable

data class BookMarkList(
   @get:JsonProperty("BOOKMARK_LIST")
   var BOOKMARK_LIST : ArrayList<BookMark>,
   var CURRENT_PAGE : Int
)
