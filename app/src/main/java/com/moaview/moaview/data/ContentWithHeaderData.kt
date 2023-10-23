package com.moaview.moaview.data

import com.moaview.moaview.db.ContentsEntity

sealed class ContentWithHeaderData{

    abstract val id: Long

    data class Item(val content: ContentsEntity) : ContentWithHeaderData() {
        override val id: Long = content.id.toLong()
    }

    object Header : ContentWithHeaderData() {
        override val id = Long.MIN_VALUE
    }

    object Empty : ContentWithHeaderData() {
        override val id = Long.MAX_VALUE
    }
}

