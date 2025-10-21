package com.hongslab.chating_memo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.IosInputDialogBinding
import com.hongslab.chating_memo.utils.MyUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class IosInputDialog(
    context: Context,
    private val title: String,
    private val isTitleCenter: Boolean,
    private val defaultValue: String,
    private val callBack: (String) -> Unit
) : Dialog(context), View.OnClickListener {
    private lateinit var binding: IosInputDialogBinding

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_yes -> {
                callBack(binding.etTxt.text.toString().trim())
                dismiss()
            }

            R.id.btn_no -> {
                callBack("")
                dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = IosInputDialogBinding.inflate(layoutInflater)
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

        binding.tvTitle.text = title
        if (isTitleCenter) {
            binding.tvTitle.gravity = Gravity.CENTER
        }

        binding.etTxt.setText(defaultValue)

        if (defaultValue != "") {
            binding.etTxt.setSelection(defaultValue.length)
        }

        MyUtils.setKeyboard(true, binding.etTxt)
    }
}