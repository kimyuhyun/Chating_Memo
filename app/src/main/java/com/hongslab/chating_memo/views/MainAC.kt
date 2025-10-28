package com.hongslab.chating_memo.views

import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdSize
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.hongslab.chating_memo.BuildConfig
import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.databinding.ActivityMainBinding
import com.hongslab.chating_memo.manager.CloudinaryDeleter
import com.hongslab.chating_memo.repository.GetDataRepository
import com.hongslab.chating_memo.repository.ImageRepository
import com.hongslab.chating_memo.utils.AdUtils
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre
import com.hongslab.chating_memo.utils.startActivityForResult2
import com.hongslab.chating_memo.views.fragments.Cate
import com.hongslab.chating_memo.views.fragments.ChatRoom
import com.hongslab.chating_memo.views.fragments.More
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainAC : BaseAC(TransitionMode.HORIZON), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private val imageRepository = ImageRepository()

    private lateinit var fragment: Fragment
    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentTransaction: FragmentTransaction

    var isCateLoaded = false
    var isChatRoomLoaded = false

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_exit -> {
                moveTaskToBack(true) // 태스크를 백그라운드로 이동

                binding.adExitLayout.closePopup.visibility = View.GONE
                setupAdExit()
            }

            R.id.btn_cancel -> {
                binding.adExitLayout.closePopup.visibility = View.GONE
                setupAdExit()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val secret = MyUtils.decrypt(BuildConfig.API_SECRET)
//        val key = MyUtils.decrypt(BuildConfig.API_KEY)
//        Dlog.d(secret)
//        Dlog.d(key)

        binding.adExitLayout.click = this

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode("1006069588078-nir414jsvg277s5cj5isunrvr2jafh8k.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()
        MyApplication.GOOGLE_SIGN_IN_CLIENT = GoogleSignIn.getClient(this, gso)


        val accessToken = SPre.get(SCol.ACCESS_TOKEN.name)
        if (accessToken == "") {
            startActivity(Intent(this, LoginAC::class.java))
            finish()
            return
        }

        setupDefaultSetting()
        setupListener()
        setupCurrentTab()
        setupAdExit()


        lifecycleScope.launch {
            deleteOldImages()
        }

        // 뒤로가기 처리
        onBackPressedDispatcher.addCallback(this) {
            binding.adExitLayout.closePopup.visibility = View.VISIBLE
        }

    }

    private fun setupDefaultSetting() {
        if (SPre.get(SCol.MESSAGE_FONT_SIZE.name) == "") {
            SPre.set(SCol.MESSAGE_FONT_SIZE.name, "15")
        }
    }

    private fun setupListener() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_cate -> {
                    moveFragment(Cate())
                    true
                }

                R.id.action_chat -> {
                    moveFragment(ChatRoom())
                    true
                }

                R.id.action_more -> {
                    moveFragment(More())
                    true
                }

                else -> false
            }
        }
    }

    private fun setupCurrentTab() {
        val currentTab = SPre.get(SCol.CURRENT_TAB.name)

        // 저장된 탭에 따라 bottomNavigation 선택
        val selectedItemId = when (currentTab) {
            "0" -> R.id.action_cate
            "1" -> R.id.action_chat
            "2" -> R.id.action_more
            else -> R.id.action_cate
        }

        // bottomNavigation 선택 (리스너 호출 방지)
        binding.bottomNavigation.selectedItemId = selectedItemId
    }

    private fun moveFragment(fragment: Fragment) {
        fragmentManager = supportFragmentManager
        fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment, fragment.javaClass.simpleName)
        fragmentTransaction.commit()
    }


    private suspend fun deleteOldImages() {
        // 오래된 이미지 삭제 로직!
        GlobalScope.launch {
            val urls = imageRepository.getExpiredImages(isLeave = 0)
            if (urls.isNotEmpty()) {
                val deletedCount = CloudinaryDeleter.getInstance().deleteImages(urls)
                val result = imageRepository.deleteExpiredImagesTable()
                Dlog.d("Deleted $deletedCount out of ${urls.size} images, result: $result")
            }
        }
    }

    private fun setupAdExit() {
        try {
            val adExitView = AdUtils(this).getExitBanner()
            binding.adExitLayout.adExitContainer.removeAllViews()
            binding.adExitLayout.adExitContainer.addView(adExitView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}