package com.hongslab.chating_memo.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.ImageActionDialogBinding
import com.hongslab.chating_memo.databinding.TextActionDialogBinding


class ImageActionDialog(
    context: Context,
    private val callBack: (String) -> Unit
) : Dialog(context), View.OnClickListener {
    private lateinit var binding: ImageActionDialogBinding

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_delete -> {
                callBack("delete")
                dismiss()
            }

            R.id.btn_close -> {
                dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ImageActionDialogBinding.inflate(layoutInflater)
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
    }
}