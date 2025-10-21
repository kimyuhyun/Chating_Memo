package com.hongslab.chating_memo.repository

import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup

class CudRepository {

    suspend fun addReply(hashMap: HashMap<String, String>): Int = withContext(Dispatchers.IO) {
        try {
            val accessToken = SPre.get(SCol.ACCESS_TOKEN.name)

            val server = MyApplication.SERVER
            val url = "${server}/add_reply"
            Dlog.d(url)
            Dlog.d("$hashMap")

            val s = Jsoup
                .connect(url)
                .header("Authorization", "Bearer $accessToken")
                .ignoreContentType(true)
                .data(hashMap)
                .method(Connection.Method.POST)
                .execute()
                .body()

            Dlog.d(s)

            val obj = JSONObject(s)

            // 토큰 재발급 로직 추가.
            if (obj.getInt("code") == -9) {
                val authRepository = AuthRepository()
                val newAccessToken = authRepository.getNewAccessToken()
                if (newAccessToken.isNotBlank() && newAccessToken != accessToken) {
                    return@withContext addReply(hashMap)
                } else {
                    return@withContext -9
                }
            }
            //


            val msg = obj.getString("msg")
            val code = obj.getInt("code")

            Dlog.d(msg)

            return@withContext code

        } catch (e: Exception) {
            e.printStackTrace()
            MyUtils.myToast(e.message ?: "에러")
            return@withContext -1
        }
    }

    suspend fun write(hashMap: HashMap<String, String>): Int = withContext(Dispatchers.IO) {
        try {
            val accessToken = SPre.get(SCol.ACCESS_TOKEN.name)

            val server = MyApplication.SERVER
            val url = "${server}/write"
            Dlog.d(url)
            Dlog.d("$hashMap")

            val s = Jsoup
                .connect(url)
                .header("Authorization", "Bearer $accessToken")
                .ignoreContentType(true)
                .data(hashMap)
                .method(Connection.Method.POST)
                .execute()
                .body()

            Dlog.d(s)

            val obj = JSONObject(s)

            // 토큰 재발급 로직 추가.
            if (obj.getInt("code") == -9) {
                val authRepository = AuthRepository()
                val newAccessToken = authRepository.getNewAccessToken()
                if (newAccessToken.isNotBlank() && newAccessToken != accessToken) {
                    return@withContext write(hashMap)
                } else {
                    return@withContext -9
                }
            }
            //

            val msg = obj.getString("msg")
            val insertId = obj.getInt("insert_id")
            val code = obj.getInt("code")

            return@withContext if (code == 1) insertId else {
                MyUtils.myToast(msg)
                -1
            }

        } catch (e: Exception) {
            e.printStackTrace()
            MyUtils.myToast(e.message ?: "에러")
            return@withContext -1
        }
    }

    suspend fun delete(hashMap: HashMap<String, String>): Int? = withContext(Dispatchers.IO) {
        try {
            val accessToken = SPre.get(SCol.ACCESS_TOKEN.name)

            val server = MyApplication.SERVER
            val url = "${server}/delete"
            Dlog.d(url)
            val s = Jsoup
                .connect(url)
                .header("Authorization", "Bearer $accessToken")
                .ignoreContentType(true)
                .data(hashMap)
                .method(Connection.Method.POST)
                .execute()
                .body()
            Dlog.d(s)
            // JSON 파싱
            val obj = JSONObject(s)

            // 토큰 재발급 로직 추가.
            if (obj.getInt("code") == -9) {
                val authRepository = AuthRepository()
                val newAccessToken = authRepository.getNewAccessToken()
                if (newAccessToken.isNotBlank() && newAccessToken != accessToken) {
                    return@withContext delete(hashMap)
                } else {
                    return@withContext -9
                }
            }
            //



            val msg = obj.getString("msg")
            val code = obj.getInt("code")

            return@withContext if (code == 1) 1 else {
                MyUtils.myToast(msg)
                null
            }


        } catch (e: Exception) {
            e.printStackTrace()
            MyUtils.myToast(e.message ?: "에러")
            return@withContext null
        }

    }
}