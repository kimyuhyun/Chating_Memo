package com.hongslab.chating_memo.models

import com.hongslab.chating_memo.utils.MyUtils


data class ChatRoomVO(
    override var uid: Long = System.nanoTime(),
    override var viewType: MyViewType = MyViewType.BODY,
    val cateIdx: String = "",
    val cateName: String = "",
    val color: String = "",
    val message: String = "",
    val isCompleted: Boolean = false,
    val msgType: Int = 0,
    val created: String = "",
) : GlobalVO() {

    fun getDiffDate(): String {
        return MyUtils.getTimeAgo(created)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChatRoomVO) return false
        return created == other.created && message == other.message
    }

    override fun hashCode(): Int {
        var result = created.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }
}