package com.hongslab.chating_memo.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.ActivityCoodiTestBinding

class CoodiTestAC : AppCompatActivity() {
    private lateinit var binding: ActivityCoodiTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoodiTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.apply {
            // CoordinatorLayout 에서 UI 가 상태바를 침범 한다.!
            // themes.xml 에서 colorPrimaryDark 색상을 상태바 색상으로 변경해야한다.!

            statusBarColor = ContextCompat.getColor(context, android.R.color.transparent)

            // 상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true

            navigationBarColor = ContextCompat.getColor(context, android.R.color.black)

            // 네비바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightNavigationBars = false


        }

    }
}