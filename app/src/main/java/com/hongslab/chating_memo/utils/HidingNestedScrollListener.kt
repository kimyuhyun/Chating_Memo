package com.hongslab.chating_memo.utils

import androidx.core.widget.NestedScrollView

abstract class HidingNestedScrollListener : NestedScrollView.OnScrollChangeListener {
    private val HIDE_THRESHOLD = 20
    private var scrolledDistance = 0
    private var controlsVisible = true
    private var oldY = 0


    override fun onScrollChange(v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        val dy = scrollY - oldScrollY

        if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
            onHide()
            controlsVisible = false;
            scrolledDistance = 0;
        } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            onShow();
            controlsVisible = true;
            scrolledDistance = 0;
        }

        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            scrolledDistance += dy
        }

        if (scrollY == 0) {
            onTop()
        }

        if (v.getChildAt(0).bottom <= v.height + scrollY) {
            onBottom()
        }
    }

    abstract fun onHide()
    abstract fun onShow()
    abstract fun onTop()
    abstract fun onBottom()
}