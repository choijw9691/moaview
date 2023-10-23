package com.moaview.moaview_sdk.data

import com.moaview.moaview_sdk.common.PageModeSettingType
import com.moaview.moaview_sdk.common.PageTurnSettingType

data class ViewerSettingData(
    var pageMode: String,
    var pageTurnSettingType: String,
    var twoPageModeInOrientation: Boolean,
    var pageTurnVolumeKey: Boolean,
    var twoPageModeInFirstPage: Boolean,
    var hideSoftKey: Boolean,
    var screenAlwaysOn: Boolean,
    var fixedScreenRotation: Boolean,
    var colorType : Int,
    var systemBrightNessCheck : Boolean,
    var appBrightNessValue : Float
) {
    constructor() : this(
        PageModeSettingType.SLIDE.toString(),
        PageTurnSettingType.BEFORE_NEXT.toString(),
        false,
        true,
        false,
        true,
        false,
        false,
        0,
        true,
        50F
    )
}
