package com.hongslab.chating_memo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.IosMenuDialogBinding


class IosMenuDialog(
    private val context: Context,
    private val xx: Int,
    private val yy: Int,
    private val gg: Int,
    private val list: ArrayList<String>,
    private val callBack: (Int) -> Unit
) : Dialog(context) {
    private lateinit var binding: IosMenuDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = IosMenuDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.apply {
            attributes = attributes?.apply {
                gravity = gg
                x = xx
                y = yy
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            setWindowAnimations(R.style.DialogAnimation)  // 애니메이션 스타일 적용
        }
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        for ((i, text) in list.withIndex()) {
            if (i > 0) {
                val line = LinearLayout(context)
                line.setBackgroundColor(Color.parseColor("#c6c6c8"))
                line.setPadding(0, 1, 0, 0)
                binding.llBox.addView(line)
            }

            val textView = TextView(context)
            textView.text = text
            textView.setTextColor(Color.parseColor("#000000"))
            textView.textSize = 16f
            val paddingH = 60
            val paddingV = 30
            textView.setPadding(paddingH, paddingV, paddingH, paddingV)
//            textView.minWidth = 300
            textView.setOnClickListener {
                callBack(i)
                dismiss()
            }
            binding.llBox.addView(textView)
        }
    }
}