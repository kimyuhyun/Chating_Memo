package com.hongslab.chating_memo.models


data class EmojiVO(
    override var uid: Long = System.nanoTime(),
    override var viewType: MyViewType = MyViewType.BODY,
    val name1: String,
) : GlobalVO()