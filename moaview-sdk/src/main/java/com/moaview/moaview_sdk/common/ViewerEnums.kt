package com.moaview.moaview_sdk.common


enum class TouchArea{
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
    CENTER,
    BOOKMARK_RIGHT,
    BOOKMARK_LEFT
}

enum class PageTurnSettingType {
    BEFORE_NEXT,
    NEXT_NEXT,
    TOP_DOWN_BEFORE_NEXT,
    TOP_DOWN_NEXT_NEXT,
    NEXT_BEFORE
}

// TODO ssin :: 대문자 기준으로 하지만 소문자 혹시 필요할까 싶어 넣음
enum class PageModeSettingType(
    val pageModeName: String
){
    SLIDE("slide"),
    SCROLL("scroll")
}
