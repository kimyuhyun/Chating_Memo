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
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.IosAlertDialogBinding
import com.hongslab.chating_memo.utils.IndentLeadingMarginSpan


class IosAlertDialog(
    context: Context,
    private val title: String,
    private val isTitleCenter: Boolean,
    private val callBack: () -> Unit
) : Dialog(context), View.OnClickListener {
    private lateinit var binding: IosAlertDialogBinding

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_ok -> {
                callBack()
                dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = IosAlertDialogBinding.inflate(layoutInflater)
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

        binding.tvTitle.text = SpannableStringBuilder(title).apply {
            setSpan(IndentLeadingMarginSpan(), 0, length, 0)
        }

        if (isTitleCenter) {
            binding.tvTitle.gravity = Gravity.CENTER
        }
    }
}