package com.hongslab.chating_memo.views

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.adapter.OnGlobalAdapterClickListener
import com.hongslab.chating_memo.databinding.ActivityFullScreenImageViewerBinding
import com.hongslab.chating_memo.manager.SystemImageDownloader
import com.hongslab.chating_memo.models.GlobalVO
import com.hongslab.chating_memo.models.ImageVO
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.MyUtils.Companion.getParcelableArrayListExtraCompat
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder

class FullScreenImageViewerAC : BaseAC(TransitionMode.HORIZON), View.OnClickListener {
    private lateinit var binding: ActivityFullScreenImageViewerBinding
    var items = arrayListOf<ImageVO>()
    var currentPosition: Int = 0

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_close -> finish()
            R.id.btn_download -> {
                val url = items[currentPosition].url
                val fileName = url.substringAfterLast("/").substringBefore("?").ifBlank {
                    "image_${System.currentTimeMillis()}.jpg"
                }
                SystemImageDownloader(this@FullScreenImageViewerAC).downloadImage(url, fileName)
                MyUtils.myToast("갤러리에 저장 되었습니다.")
            }
        }

    }

    private val onGlobalAdapterClickListener = object : OnGlobalAdapterClickListener {
        override fun onGlobalAdapterItemClick(v: View, item2: Any, pos: Int) {
            val item = item2 as GlobalVO

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.click = this
        binding.lifecycleOwner = this
        binding.ac = this
        binding.itemClick = onGlobalAdapterClickListener

        window.decorView.post {
            window.apply {
                statusBarColor = ContextCompat.getColor(context, R.color.black)
                WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = false

                navigationBarColor = ContextCompat.getColor(context, R.color.black)
                WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightNavigationBars = false
            }
        }

        items = intent.getParcelableArrayListExtraCompat<ImageVO>("urls") ?: arrayListOf<ImageVO>()

        // 뒤로가기 버튼 처리!
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

        binding.tvCurrentIndex.text = "1 / ${items.size}"

        setupListener()
    }

    private fun setupListener() {
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position < 0 || position >= items.size) return

                currentPosition = position

                binding.tvCurrentIndex.text = "${position + 1} / ${items.size}"

            }
        })
    }

}