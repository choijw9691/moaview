package com.moaview.moaview.data

import com.moaview.moaview.common.ListModeEnum
import com.moaview.moaview.common.SortState

data class SettingData(
    var listMode : String,
    var sortState : String,
    var tutorialState : Boolean
){
    constructor() : this(
        ListModeEnum.COVERVIEW.toString(),
        SortState.READ.toString(),
        false
    )
}
