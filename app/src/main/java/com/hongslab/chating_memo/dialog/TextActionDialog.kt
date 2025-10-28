package com.hongslab.chating_memo.dialog


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.TextActionDialogBinding
import com.hongslab.chating_memo.models.ChatMessageVO
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.views.ChatMessageAC
import com.hongslab.chating_memo.views.SearchAC
import androidx.core.graphics.drawable.toDrawable


class TextActionDialog(
    private val activity: Activity,
    private val item: ChatMessageVO,
    private val callBack: (String) -> Unit
) : Dialog(activity), View.OnClickListener {
    private lateinit var binding: TextActionDialogBinding

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_notice -> {
                callBack("notice")
                dismiss()
            }

            R.id.btn_copy -> {
                callBack("copy")
                dismiss()
            }

            R.id.btn_select_copy -> {
                callBack("select_copy")
                dismiss()
            }

            R.id.btn_completed -> {
                callBack("completed")
                dismiss()
            }

            R.id.btn_completed_cancel -> {
                callBack("completed_cancel")
                dismiss()
            }

            R.id.btn_goto_bottom -> {
                callBack("goto_bottom")
                dismiss()
            }

            R.id.btn_merge_memo -> {
                callBack("merge_memo")
                dismiss()
            }

            R.id.btn_modify -> {
                callBack("modify")
                dismiss()
            }

            R.id.btn_delete -> {
                callBack("delete")
                dismiss()
            }

            R.id.ll_box -> {
                dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TextActionDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.click = this

        window?.apply {
            attributes = attributes?.apply {
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setWindowAnimations(R.style.DialogAnimation)  // 애니메이션 스타일 적용
        }

        // 뒤로가기 버튼, 빈 화면 터치를 통해 dialog가 사라지도록
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        if (item.isCompleted) {
            binding.btnCompleted.visibility = View.GONE
            binding.btnCompletedCancel.visibility = View.VISIBLE
        } else {
            binding.btnCompleted.visibility = View.VISIBLE
            binding.btnCompletedCancel.visibility = View.GONE
        }


        if (activity is SearchAC) {
            Dlog.d("SearchAC 분기 진입")
            binding.btnNotice.visibility = View.GONE
            binding.btnCompleted.visibility = View.GONE
            binding.btnCompletedCancel.visibility = View.GONE
            binding.btnModify.visibility = View.GONE
//            binding.btnDelete.visibility = View.GONE
        } else if (context is ChatMessageAC) {
            Dlog.d("ChatMessageAC 분기 진입")
        }

    }
}