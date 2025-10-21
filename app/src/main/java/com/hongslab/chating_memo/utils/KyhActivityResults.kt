package com.hongslab.chating_memo.utils

import android.content.Intent
import android.util.SparseArray
import androidx.core.util.containsKey
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "KyhActivityResult"
suspend fun FragmentActivity.startActivityForResult2(intent: Intent): KyhActivityResult {
    val fragment = this.supportFragmentManager.findFragmentByTag(TAG) as? ActivityResultFragment
        ?: ActivityResultFragment().also {
            this.supportFragmentManager.beginTransaction()
                .add(it, TAG)
                .commitNow()
        }
    return fragment.startActivityForResult(intent)
}

suspend fun Fragment.startActivityForResult2(intent: Intent): KyhActivityResult {
    val fragment = this.childFragmentManager.findFragmentByTag(TAG) as? ActivityResultFragment
        ?: ActivityResultFragment().also {
            this.childFragmentManager.beginTransaction()
                .add(it, TAG)
                .commitNow()
        }
    return fragment.startActivityForResult(intent)
}

/**
 * 액티비티 실행 결과
 * @param resultCode 결과 코드
 * @param data 추가 데이터
 */
data class KyhActivityResult(
    val resultCode: Int,
    val data: Intent?
)

internal class ActivityResultFragment : Fragment() {
    private val viewModel by lazy {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = modelClass.newInstance()
        }
        ViewModelProvider(this, factory)[InnerViewModel::class.java]
    }

    suspend fun startActivityForResult(
        intent: Intent
    ): KyhActivityResult = suspendCoroutine { cont: Continuation<KyhActivityResult> ->
        var unusedReqCode = 0
        while (this.viewModel.activityResultMap.containsKey(unusedReqCode)) {
            unusedReqCode++
        }
        this.viewModel.activityResultMap[unusedReqCode] = cont
        this.startActivityForResult(intent, unusedReqCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        this.viewModel.activityResultMap[requestCode]?.resume(KyhActivityResult(resultCode, data))
        this.viewModel.activityResultMap.remove(requestCode)
    }

    private class InnerViewModel : ViewModel() {
        val activityResultMap = SparseArray<Continuation<KyhActivityResult>>()
    }
}