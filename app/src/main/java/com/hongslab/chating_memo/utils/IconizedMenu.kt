package com.hongslab.chating_memo.utils

import android.content.Context
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu

class IconizedMenu(
    context: Context,
    anchor: View,
    isUpDirection: Boolean
) {

    private val mContext: Context = context
    private val mAnchor: View = anchor
    private val mPopupMenu: PopupMenu = PopupMenu(context, anchor)
    private var mMenuItemClickListener: OnMenuItemClickListener? = null
    private var mDismissListener: OnDismissListener? = null

    init {
        if (isUpDirection) {
            mPopupMenu.gravity = View.LAYOUT_DIRECTION_RTL // 상단 방향을 설정 (추가적인 설정 필요할 수 있음)
        }

        // 메뉴 아이템 클릭 리스너 설정
        mPopupMenu.setOnMenuItemClickListener { item ->
            mMenuItemClickListener?.onMenuItemClick(item) ?: false
        }

        // dismiss 리스너 설정 (안드로이드에서 기본 dismiss 제공)
        mPopupMenu.setOnDismissListener {
            mDismissListener?.onDismiss(this)
        }
    }

    private fun getMenuInflater(): MenuInflater = mPopupMenu.menuInflater

    fun inflate(menuRes: Int) {
        getMenuInflater().inflate(menuRes, mPopupMenu.menu)
    }

    fun show() {
        // PopupMenu의 내부 필드를 사용하여 show()
        // Anchor view의 화면 상 좌표 가져오기
        val location = IntArray(2)
        mAnchor.getLocationOnScreen(location)

        // 현재 위치에 오프셋 적용
        val anchorX = location[0]
        val anchorY = location[1]

        try {
            val popupHelper = PopupMenu::class.java.getDeclaredField("mPopup")
            popupHelper.isAccessible = true
            val menuPopupHelper = popupHelper.get(mPopupMenu)
            menuPopupHelper.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menuPopupHelper, true)

            menuPopupHelper.javaClass
                .getDeclaredMethod("show", Int::class.java, Int::class.java)
                .invoke(menuPopupHelper, anchorX, anchorY)
        } catch (e: Exception) {
            e.printStackTrace()
            // 기본적인 show() 호출
            mPopupMenu.show()
        }
    }


    fun dismiss() {
        mPopupMenu.dismiss()
    }

    // 기존 인터페이스 타입이 아니라, 람다 형태로 변경
    fun setOnMenuItemClickListener(listener: (MenuItem) -> Unit) {
        mMenuItemClickListener = object : OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                listener(item ?: return false)
                return true // 항상 true 반환
            }
        }
    }

    fun setOnDismissListener(listener: OnDismissListener) {
        mDismissListener = listener
    }

    /**
     * Dismiss listener interface
     */
    interface OnDismissListener {
        fun onDismiss(menu: IconizedMenu?)
    }

    /**
     * Menu item click listener interface
     */
    interface OnMenuItemClickListener {
        fun onMenuItemClick(item: MenuItem?): Boolean
    }
}