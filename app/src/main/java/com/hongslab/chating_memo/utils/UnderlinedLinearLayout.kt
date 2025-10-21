package com.hongslab.chating_memo.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.hongslab.chating_memo.R


class UnderlinedLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var leftMargin = 0f
    private var rightMargin = 0f
    private val strokePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.systemGray3)  // 선 색상
        style = Paint.Style.STROKE  // 선 스타일
        strokeWidth = 2f  // 선 두께
    }

    init {
        // 배경이 없을 경우 onDraw가 호출되지 않기 때문에 배경을 투명하게 설정
        setWillNotDraw(false)

        // XML 속성 정의
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.UnderlinedLinearLayout)
        leftMargin = typedArray.getDimension(R.styleable.UnderlinedLinearLayout_lineLeftMargin, 0f)
        rightMargin = typedArray.getDimension(R.styleable.UnderlinedLinearLayout_lineRightMargin, 0f)
        strokePaint.color = typedArray.getColor(
            R.styleable.UnderlinedLinearLayout_underlineColor,
            ContextCompat.getColor(context, R.color.systemGray3)
        )
        typedArray.recycle()
    }

    fun setLineLeftMargin(dp: Int) {
        this.leftMargin = dpToPx(dp)
        invalidate()
    }

    fun setLineRightMargin(dp: Int) {
        this.rightMargin = dpToPx(dp)
        invalidate()
    }

    fun setLineColor(colorResId: Int) {
        strokePaint.color = ContextCompat.getColor(context, colorResId)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawLine(
            leftMargin,
            height.toFloat(),
            width.toFloat() - rightMargin,
            height.toFloat(),
            strokePaint
        )
    }

    private fun dpToPx(dp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics)
    }
}