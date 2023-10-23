package com.moaview.moaview_sdk.common

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.*

fun View.doOnApplyWindowInsets(
    windowInsetsListener: (
        insetView: View,
        windowInsets: WindowInsetsCompat,
        initialPadding: Insets,
        initialMargins: Insets
    ) -> Unit
) {
    val initialPadding = Insets.of(paddingLeft, paddingTop, paddingRight, paddingBottom)
    val initialMargins = Insets.of(marginLeft, marginTop, marginRight, marginBottom)

    ViewCompat.setOnApplyWindowInsetsListener(this) { insetView, windowInsets ->
        windowInsets.also {
            windowInsetsListener(insetView, windowInsets, initialPadding, initialMargins)
        }
    }

    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            v.requestApplyInsets()
        }
        override fun onViewDetachedFromWindow(v: View) = Unit
    })

    if (isAttachedToWindow) {
        requestApplyInsets()
    }
}
