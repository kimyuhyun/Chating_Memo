package com.hongslab.chating_memo.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.ColorSelectDialogBinding
import com.hongslab.chating_memo.databinding.IosAlertDialogBinding
import com.hongslab.chating_memo.utils.IndentLeadingMarginSpan


class ColorSelectDialog(
    context: Context,
    private val callBack: (String) -> Unit
) : Dialog(context), View.OnClickListener {
    private lateinit var binding: ColorSelectDialogBinding

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_close -> {
                dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ColorSelectDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.click = this

        window?.apply {
            attributes = attributes?.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setWindowAnimations(R.style.DialogAnimation)  // 애니메이션 스타일 적용
        }

        // 뒤로가기 버튼, 빈 화면 터치를 통해 dialog가 사라지지 않도록
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        setupColorButtonsGrid()
    }

    private fun setupColorButtonsGrid() {
        val gridLayout = binding.colorGrid
        gridLayout.columnCount = 5 // 한 줄에 5개

        MyApplication.COLOR_CODE.forEachIndexed { index, colorCode ->
            val cardView = createColorButton(colorCode, index)

            val params = GridLayout.LayoutParams().apply {
                width = dpToPx(50)
                height = dpToPx(50)
                setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5))
            }

            cardView.layoutParams = params
            gridLayout.addView(cardView)
        }
    }

    private fun createColorButton(colorCode: String, index: Int): MaterialCardView {
        val cardView = MaterialCardView(context).apply { // context로 수정
            setCardBackgroundColor(Color.parseColor(colorCode))
            radius = dpToPx(25).toFloat()
            cardElevation = 0f
            strokeColor = ContextCompat.getColor(context, R.color.systemGray) // context로 수정
            strokeWidth = dpToPx(1)
            setOnClickListener {
                callBack(colorCode)
                dismiss()
            }
        }

        return cardView
    }


    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}