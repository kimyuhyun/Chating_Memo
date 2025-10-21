package com.hongslab.chating_memo.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatMessageVO(
    var uid: Long = System.nanoTime(),
    val idx: String = "",
    val msgType: Int = 1,
    val message: String = "",
    val link: String = "",
    val isCompleted: Boolean = false,
    var isChecked: Boolean = false,
    val created: String = "",
) : Parcelable