package com.hongslab.chating_memo.models

open class GlobalVO {
    open var uid: Long = System.nanoTime()
    open var viewType: MyViewType = MyViewType.BODY
}
