package com.hongslab.chating_memo.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import com.hongslab.chating_memo.R

class KyhImageButton : androidx.appcompat.widget.AppCompatImageView {
    private var path: Path = Path()
    private var radius: Int = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.KyhButton)
        radius = convertRadius(a.getInt(R.styleable.KyhButton_radius, 99))
        a.recycle()
    }

    init {
        isClickable = true
        isFocusable = true

        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
        foreground = resources.getDrawable(outValue.resourceId, context.theme)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setNewPath(w, h)
    }

    override fun draw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(path)
        super.draw(canvas)
        canvas.restoreToCount(save)
    }

    private fun setNewPath(w: Int, h: Int) {
        path.reset()
        val rect = RectF()
        rect.set(0f, 0f, w.toFloat(), h.toFloat())
        path.addRoundRect(rect, radius.toFloat(), radius.toFloat(), Path.Direction.CW)
        path.close()
    }

    private fun convertRadius(radius: Int): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius.toFloat(), displayMetrics).toInt()
    }
}