package com.hongslab.chating_memo.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.models.MyViewType

class IosStyleAddLineDecoration(
    context: Context,
    private val height: Float,
    padding: Float
) : RecyclerView.ItemDecoration() {

    private val paddingPx = dpToPx(context, padding)
    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.systemGray3)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val left = parent.paddingStart.toFloat()
        val paddingApplyLeft = parent.paddingStart + paddingPx
        val right = (parent.width - parent.paddingEnd).toFloat()

        val validChildren = mutableListOf<Pair<Int, android.view.View>>()

        // HEADER, FOOTER가 아닌 아이템들만 필터링
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val viewHolder = parent.getChildViewHolder(child)
            val itemViewType = viewHolder.itemViewType

            // MyViewType.HEADER.ordinal, MyViewType.FOOTER.ordinal과 비교
            if (itemViewType != MyViewType.HEADER.ordinal &&
                itemViewType != MyViewType.FOOTER.ordinal
            ) {
                validChildren.add(Pair(i, child))
            }
        }

        // 유효한 아이템들에 대해서만 라인 그리기
        validChildren.forEachIndexed { validIndex, (originalIndex, child) ->
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = (child.bottom + params.bottomMargin).toFloat()
            val bottom = top + height

            when (validIndex) {
                0 -> {
                    // 첫번째 유효한 아이템 위에 라인
                    val childTop = (child.top - params.topMargin).toFloat()
                    c.drawRect(left, childTop - height, right, childTop, paint)
                    // 아래쪽 라인
                    if (validChildren.size > 1) {
                        c.drawRect(paddingApplyLeft, top, right, bottom, paint)
                    } else {
                        c.drawRect(left, top, right, bottom, paint)
                    }
                }

                validChildren.size - 1 -> {
                    // 마지막 유효한 아이템은 패딩없이
                    c.drawRect(left, top, right, bottom, paint)
                }

                else -> {
                    // 중간 아이템들은 패딩 적용
                    c.drawRect(paddingApplyLeft, top, right, bottom, paint)
                }
            }
        }
    }

    private fun dpToPx(context: Context, dp: Float): Float {
        val dm = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm)
    }
}