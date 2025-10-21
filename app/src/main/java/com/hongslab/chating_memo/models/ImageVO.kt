package com.hongslab.chating_memo.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageVO(
    override var uid: Long = System.nanoTime(),
    override var viewType: MyViewType = MyViewType.BODY,
    val url: String = "",
) : Parcelable, GlobalVO()