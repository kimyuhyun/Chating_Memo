package com.hongslab.chating_memo

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.hongslab.chating_memo.utils.MyUtils

import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp()
class MyApplication : MultiDexApplication() {
    companion object {
        var DEBUG = true
        var INSTANCE: MyApplication? = null
        var AD_SIZE: AdSize? = null
        var GOOGLE_SIGN_IN_CLIENT: GoogleSignInClient? = null
        val COLOR_CODE = arrayOf(
            "#FFEB33", "#81D4FA", "#A5D6A7", "#FFCDD2", "#CE93D8",
            "#FFD54F", "#80CBC4", "#FFAB91", "#B39DDB", "#F8BBD9",
            "#FFF176", "#90CAF9", "#BCAAA4", "#FFCC80", "#C5E1A5"
        )

        const val USE_TERMS_KR = "https://hongslab.blogspot.com/2024/10/blog-post_16.html"
        const val POLICY_KR = "https://hongslab.blogspot.com/2024/10/blog-post.html"

        const val USE_TERMS = "https://hongslab.blogspot.com/2024/10/terms-and-conditions-of-use.html"
        const val POLICY = "https://hongslab.blogspot.com/2024/10/personal-information-processing-policy.html"

        const val SERVER = "http://ncv.hongslab.shop/ChatingMemo/model"

        fun isDebuggable(context: Context): Boolean {
            var debuggable = false
            val pm = context.packageManager
            try {
                val appInfo = pm.getApplicationInfo(context.packageName, 0)
                debuggable = 0 != appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return debuggable
        }
    }

    override fun onCreate() {
        super.onCreate()
        DEBUG = isDebuggable(this)
        INSTANCE = this

        val dm = resources.displayMetrics
        val adWidth = (dm.widthPixels / dm.density).toInt()
        AD_SIZE = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)

        // Google Mobile Ads SDK 초기화 추가
        MobileAds.initialize(this) {}

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (MyUtils.isDarkModeApply(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}