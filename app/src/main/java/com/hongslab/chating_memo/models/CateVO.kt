package com.hongslab.chating_memo.models


data class CateVO(
    override var uid: Long = System.nanoTime(),
    override var viewType: MyViewType = MyViewType.BODY,
    val idx: String = "",
    val name1: String = "",
    val color: String = "",
) : GlobalVO()