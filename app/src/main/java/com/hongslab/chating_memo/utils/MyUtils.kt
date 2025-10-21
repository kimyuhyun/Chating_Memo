package com.hongslab.chating_memo.utils

import android.app.Activity
import android.app.Application
import android.app.UiModeManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.util.Base64
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.hongslab.chating_memo.BuildConfig

import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.R

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class MyUtils {
    companion object {
        fun encrypt(text: String): String {
            return try {
                val secretKey = BuildConfig.KEY.toString().toByteArray()
                val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
                val keySpec = SecretKeySpec(secretKey, "AES")
                val ivSpec = IvParameterSpec(ByteArray(16))
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
                val encrypted = cipher.doFinal(text.toByteArray())
                Base64.encodeToString(encrypted, Base64.NO_WRAP)
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

        fun decrypt(text: String): String {
            try {
                val secretKey = BuildConfig.KEY.toString().toByteArray()
                val cipherTextData: ByteArray = text.toByteArray()
                val encrypted = Base64.decode(cipherTextData, 0)
                val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
                val keySpec = SecretKeySpec(secretKey, "AES")
                val ivSpec = IvParameterSpec(byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
                val original = cipher.doFinal(encrypted)
                return String(original)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        fun getWidthPixel(): Int {
            val displayMetrics = Resources.getSystem().displayMetrics
            return displayMetrics.widthPixels
        }

        fun setKeyboard(flag: Boolean, editText: EditText) {
            Handler(Looper.getMainLooper()).postDelayed({
                val imm = MyApplication.INSTANCE?.getSystemService(Application.INPUT_METHOD_SERVICE) as InputMethodManager
                if (flag) {
                    //키보드 올리기
                    editText.requestFocus()
                    imm.showSoftInput(editText, 0)
                } else {
                    //키보드 내리기
                    imm.hideSoftInputFromWindow(editText.windowToken, 0)
                }
            }, 200)
        }

        fun dpToPx(dp: Int): Int {
            val dm = MyApplication.INSTANCE?.let {
                it.resources.displayMetrics
            }
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), dm).toInt()
        }

        fun pxToDp(px: Int): Int {
            var density: Float? = MyApplication.INSTANCE?.let {
                it.resources.displayMetrics.density
            } ?: return 0

            if (density?.toDouble() == 1.0) {
                density *= 4.0.toFloat()
            } else if (density?.toDouble() == 1.5) {
                density *= (8 / 3).toFloat()
            } else if (density?.toDouble() == 2.0) {
                density *= 2.0.toFloat()
            }
            return (px / density!!).toInt()
        }

        fun myToast(msg: String?) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(MyApplication.INSTANCE, msg, Toast.LENGTH_SHORT).show()
            }
        }


        fun getTimeAgo(input: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

            val date: Date = inputFormat.parse(input) ?: return ""
            val current = Calendar.getInstance()
            val diffInMillis = current.timeInMillis - date.time

            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
            val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
            val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
            val weeks = days / 7
            val months = days / 30

            return when {
                minutes < 1 -> "방금"
                minutes < 60 -> "${minutes}분 전"
                hours < 24 -> "${hours}시간 전"
                days == 1L -> "어제"
                days < 7 -> "${days}일 전"
                weeks == 1L -> "1주 전"
                weeks < 4 -> "${weeks}주 전"
                months == 1L -> "1달 전"
                months < 12 -> "${months}달 전"
                else -> SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(date)
            }
        }

        fun formatToYMDWithDayOfWeek(dateStr: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy년 M월 d일 E요일", Locale.KOREA)

            return try {
                val date = inputFormat.parse(dateStr)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                dateStr // 파싱 실패 시 원본 반환
            }
        }

        fun formatTimeWithAmPm(dateTimeStr: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("a h:mm", Locale.KOREA) // 오전/오후 표시 + 시간:분

            return try {
                val date = inputFormat.parse(dateTimeStr)
                outputFormat.format(date!!) // 결과 예: "오전 2:05"
            } catch (e: Exception) {
                ""
            }
        }

        fun getCurrentTime(): String {
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            return currentTime.toString()
        }

        fun copyText(context: Context, text: String) {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", text)
            clipboardManager.setPrimaryClip(clipData)
            myToast("복사되었습니다.")
        }


        fun getCurrentSystemTheme(context: Context): String {
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager

            return when (uiModeManager.nightMode) {
                UiModeManager.MODE_NIGHT_YES -> "dark"
                UiModeManager.MODE_NIGHT_NO -> "light"
                else -> ""
            }
        }

        fun isDarkModeApply(context: Context): Boolean {
            Dlog.d("${SPre.get(SCol.THEME.name)}")
            return if (SPre.get(SCol.THEME.name) == "c_light") {
                false
            } else if (SPre.get(SCol.THEME.name) == "c_dark") {
                true
            } else {
                getCurrentSystemTheme(context) == "dark"
            }
        }

        fun comma(number: Long): String {
            val numberFormat = NumberFormat.getNumberInstance()
            return numberFormat.format(number)
        }

        fun highlightSearchText(text: String, searchText: String): SpannableStringBuilder {
            val spannableBuilder = SpannableStringBuilder(text)

            if (searchText.isNotEmpty()) {
                // 공백으로 구분된 여러 검색어 지원
                val searchWords = searchText.split(" ").filter { it.isNotEmpty() }

                searchWords.forEach { word ->
                    val wordLower = word.lowercase()
                    val textLower = text.lowercase()

                    var startIndex = 0
                    while (startIndex < textLower.length) {
                        val index = textLower.indexOf(wordLower, startIndex)
                        if (index == -1) break

                        val backgroundColorSpan = BackgroundColorSpan(
                            ContextCompat.getColor(MyApplication.INSTANCE!!, R.color.systemMint)
                        )
                        spannableBuilder.setSpan(
                            backgroundColorSpan,
                            index,
                            index + word.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        startIndex = index + word.length
                    }
                }
            }

            return spannableBuilder
        }


        inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableExtra(key, T::class.java)
            } else {
                @Suppress("DEPRECATION")
                getParcelableExtra(key)
            }
        }

        inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableArrayListExtra(key, T::class.java)
            } else {
                @Suppress("DEPRECATION")
                getParcelableArrayListExtra(key)
            }
        }
    }
}