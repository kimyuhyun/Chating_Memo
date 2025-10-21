package com.hongslab.chating_memo.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil.setContentView
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.ActivitySelectCopyBinding
import com.hongslab.chating_memo.dialog.EmojiMapper.decodeEmojis
import com.hongslab.chating_memo.models.ChatMessageVO
import com.hongslab.chating_memo.utils.MyUtils.Companion.getParcelableExtraCompat

class SelectCopyAC : BaseAC(TransitionMode.HORIZON), View.OnClickListener {
    private lateinit var binding: ActivitySelectCopyBinding

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_back -> finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectCopyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.click = this


        intent.getParcelableExtraCompat<ChatMessageVO>("item")?.let { item ->
            val tmp = decodeEmojis(item.message)
            binding.tvMessage.text = tmp
        }

        // 뒤로가기 버튼 처리!
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
    }
}