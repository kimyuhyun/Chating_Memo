package com.hongslab.chating_memo.views

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.hongslab.chating_memo.R
import com.hongslab.chating_memo.utils.Dlog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseAC(private val transitionMode: TransitionMode = TransitionMode.NONE) : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (transitionMode) {
            TransitionMode.HORIZON -> overridePendingTransition(R.anim.horizon_enter1, R.anim.horizon_enter2)
            TransitionMode.VERTICAL -> overridePendingTransition(R.anim.vertical_enter1, R.anim.vertical_enter2)
            TransitionMode.MENU -> overridePendingTransition(R.anim.left_to_right1, R.anim.left_to_right2)
            else -> Unit
        }


    }

    override fun onResume() {
        super.onResume()
        window.apply {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    Dlog.d("다크 모드")
                    // 다크 모드
                    WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = false

                    // 네비바 아이콘(true: 검정 / false: 흰색)
                    WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightNavigationBars = false
                }

                Configuration.UI_MODE_NIGHT_NO -> {
                    Dlog.d("라이트 모드")
                    // 라이트 모드
                    WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true

                    // 네비바 아이콘(true: 검정 / false: 흰색)
                    WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightNavigationBars = true
                }

                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    Dlog.d("모드가 정의되지 않음")
                    // 모드가 정의되지 않음
                    WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
//
//                    // 네비바 아이콘(true: 검정 / false: 흰색)
                    WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        when (transitionMode) {
            TransitionMode.HORIZON -> overridePendingTransition(R.anim.horizon_exit1, R.anim.horizon_exit2)
            TransitionMode.VERTICAL -> overridePendingTransition(R.anim.vertical_exit1, R.anim.vertical_exit2)
            TransitionMode.MENU -> overridePendingTransition(R.anim.right_to_left1, R.anim.right_to_left2)
            else -> Unit
        }
    }

    enum class TransitionMode {
        NONE, HORIZON, VERTICAL, MENU
    }

    fun setStatusBarTransparent() {
        //노치홀 무시!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

    }

//    fun setStatusBarTransparent(view: View) {
////        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.statusBarColor = 0x00000000
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//
//        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//            topMargin = getStatusBarHeight() * -1
//        }
//        WindowInsetsCompat.CONSUMED
//
////        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
////        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
//
//        if (Build.VERSION.SDK_INT >= 30) {
////            WindowCompat.setDecorFitsSystemWindows(window, false)
//        }
//    }

    fun getToolBarHeight(): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
        val actionBarHeightPixel = TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        return actionBarHeightPixel + getStatusBarHeight()
    }

    fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }
}