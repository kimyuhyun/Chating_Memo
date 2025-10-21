package com.hongslab.chating_memo.repository

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup


class ImageRepository {
    suspend fun getExpiredImages(isLeave: Int): ArrayList<String> = withContext(Dispatchers.IO) {
        try {
            val accessToken = SPre.get(SCol.ACCESS_TOKEN.name)

            val server = MyApplication.SERVER
            val url = "${server}/get_expired_images?is_leave=${isLeave}"
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
                    return@withContext getExpiredImages(isLeave)
                } else {
                    return@withContext arrayListOf()
                }
            }
            //

            if (obj.getInt("code") == 1) {
                val list = arrayListOf<String>()
                val array = JSONArray(obj.getString("data"))
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val url = obj.getString("url")
                    list.add(url)
                }
                return@withContext list
            } else {
                return@withContext arrayListOf<String>()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext arrayListOf<String>()
        }
    }

    suspend fun deleteExpiredImagesTable(): String = withContext(Dispatchers.IO) {
        try {
            val accessToken = SPre.get(SCol.ACCESS_TOKEN.name)

            val server = MyApplication.SERVER
            val url = "${server}/delete_expired_images_table"
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
                    return@withContext deleteExpiredImagesTable()
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