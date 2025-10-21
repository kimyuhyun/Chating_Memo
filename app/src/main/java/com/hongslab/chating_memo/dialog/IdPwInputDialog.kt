package com.hongslab.chating_memo.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.IdPwInputDialogBinding


class IdPwInputDialog(
    context: Context,
    private val callBack: (Boolean, String, String) -> Unit
) : Dialog(context), View.OnClickListener {
    private lateinit var binding: IdPwInputDialogBinding

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ll_root -> {
                dismiss()
            }
            R.id.btn_ok -> {
                val id = binding.etId.text.toString().trim()
                val pw = binding.etPw.text.toString().trim()

                callBack(true, id, pw)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = IdPwInputDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.click = this

        // 뒤로가기 버튼, 빈 화면 터치를 통해 dialog가 사라지도록
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        val params: WindowManager.LayoutParams? = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = params
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }
}