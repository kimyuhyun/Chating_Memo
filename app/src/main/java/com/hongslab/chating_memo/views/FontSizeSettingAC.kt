package com.hongslab.chating_memo.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.activity.addCallback
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.ActivityFontSizeSettingBinding
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre

class FontSizeSettingAC : BaseAC(TransitionMode.HORIZON), View.OnClickListener {
    private lateinit var binding: ActivityFontSizeSettingBinding

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_back -> finish()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFontSizeSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.click = this

        val saved = SPre.get(SCol.MESSAGE_FONT_SIZE.name)?.toFloatOrNull() ?: 15f
        binding.slider.value = saved
        binding.tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, saved)

        binding.slider.addOnChangeListener { slider, _, _ ->
            val selectedValue = slider.value.toInt()
            SPre.set(SCol.MESSAGE_FONT_SIZE.name, selectedValue.toString())

            binding.tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, selectedValue.toFloat())
        }

        // 뒤로가기 버튼 처리!
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
    }
}