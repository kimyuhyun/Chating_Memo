package com.hongslab.chating_memo.models


data class NoticeTextState(
    val idx: String = "",
    val message: String = "",
    var isExpand: Boolean = false,
)