package com.hongslab.chating_memo.repository

import android.content.Intent
import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.dialog.EmojiMapper
import com.hongslab.chating_memo.models.CateVO
import com.hongslab.chating_memo.models.ChatMessageVO
import com.hongslab.chating_memo.models.ChatRoomVO
import com.hongslab.chating_memo.models.MyViewType
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre
import com.hongslab.chating_memo.views.LoginAC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup


class AuthRepository {
    suspend fun getTokens(email: String, serverAuthCode: String): Pair<String, String> = withContext(Dispatchers.IO) {
        try {
            val server = MyApplication.SERVER
            val url = "${server}/login?email=${email}&auth_code=${serverAuthCode}"
            Dlog.d(url)
            val s = Jsoup
                .connect(url)
                .ignoreContentType(true)
                .execute()
                .body()
            Dlog.d("S: $s")
            val obj = JSONObject(s)
            if (obj.getInt("code") == 1) {
                val accessToken = obj.getString("access_token")
                val refreshToken = obj.getString("refresh_token")
                return@withContext Pair(accessToken, refreshToken)
            } else {
                MyUtils.myToast(obj.getString("msg"))
                return@withContext Pair("", "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Pair("", "")
        }
    }


    suspend fun getNewAccessToken(): String = withContext(Dispatchers.IO) {
        try {
            val refreshToken = SPre.get(SCol.REFRESH_TOKEN.name)
            val server = MyApplication.SERVER
            val url = "${server}/refresh_token"
            Dlog.d(url)
            val s = Jsoup
                .connect(url)
                .header("Authorization", "Bearer $refreshToken")
                .ignoreContentType(true)
                .execute()
                .body()
            Dlog.d(s)
            val obj = JSONObject(s)
            if (obj.getInt("code") == 1) {
                val newAccessToken = obj.getString("access_token")
                SPre.set(SCol.ACCESS_TOKEN.name, newAccessToken)
                return@withContext newAccessToken
            } else {
                MyUtils.myToast(obj.getString("msg"))
                // 메인 스레드로 전환해서 액티비티 실행
                withContext(Dispatchers.Main) {
                    val context = MyApplication.INSTANCE!!.applicationContext
                    val intent = Intent(context, LoginAC::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                return@withContext ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext ""
        }
    }

    suspend fun memberLeave(): String = withContext(Dispatchers.IO) {
        try {
            val accessToken = SPre.get(SCol.ACCESS_TOKEN.name)

            val server = MyApplication.SERVER
            val url = "${server}/memb_leave"
            Dlog.d(url)
            val s = Jsoup
                .connect(url)
                .header("Authorization", "Bearer $accessToken")
                .ignoreContentType(true)
                .execute()
                .body()
            Dlog.d(s)
            val obj = JSONObject(s)

            // 토큰 재발급 로직 추가.
            if (obj.getInt("code") == -9) {
                val authRepository = AuthRepository()
                val newAccessToken = authRepository.getNewAccessToken()
                if (newAccessToken.isNotBlank() && newAccessToken != accessToken) {
                    return@withContext memberLeave()
                } else {
                    return@withContext "토큰 재발급 실패"
                }
            }
            //
            return@withContext "성공"
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "서버에러"
        }
    }
}