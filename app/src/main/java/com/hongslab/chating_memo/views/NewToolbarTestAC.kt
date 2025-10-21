package com.hongslab.chating_memo.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hongslab.chating_memo.R

class NewToolbarTestAC : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_toolbar_test)

        window.apply {
            statusBarColor = ContextCompat.getColor(context, R.color.systemBackground)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = false
        }
    }
}