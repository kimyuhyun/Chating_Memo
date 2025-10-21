package com.hongslab.chating_memo.repository

import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.dialog.EmojiMapper
import com.hongslab.chating_memo.models.CateVO
import com.hongslab.chating_memo.models.ChatMessageVO
import com.hongslab.chating_memo.models.ChatRoomVO
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup

class GetDataRepository {
    suspend fun getCate(): ArrayList<CateVO> = withContext(Dispatchers.IO) {
        try {
            val accessToken = SPre.get(SCol.ACCESS_TOKEN.name)

            val server = MyApplication.SERVER
            val url = "${server}/get_cate"
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
                    return@withContext getCate()
                } else {
                    return@withContext arrayListOf()
                }
            }
            //

            val list = arrayListOf<CateVO>()
            val array = JSONArray(obj.getString("data"))
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val vo = CateVO(
                    idx = obj.getString("idx"),
                    name1 = obj.getString("name1"),
                    color = obj.getString("color"),
                )
                list.add(vo)
            }
            return@withContext list
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext arrayListOf<CateVO>()
        }
    }

    suspend fun getChatRooms(): ArrayList<ChatRoomVO> = withContext(Dispatchers.IO) {
        try {
            val accessToken = SPre.get(SCol.ACCESS_TOKEN.name)
            val server = MyApplication.SERVER
            val url = "${server}/get_chat_rooms"
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
                    return@withContext getChatRooms()
                } else {
                    return@withContext arrayListOf()
                }
            }
            //

            val list = arrayListOf<ChatRoomVO>()
            val array = JSONArray(obj.getString("data"))
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val decodeMessage = EmojiMapper.decodeEmojis(obj.getString("message"))
                val vo = ChatRoomVO(
                    cateIdx = obj.getString("cate_idx"),
                    cateName = obj.getString("cate_name"),
                    color = obj.getString("color"),
                    message = decodeMessage,
                    msgType = obj.getInt("msg_type"),
                    isCompleted = obj.getInt("is_completed") == 1,
                    created = obj.getString("created")
                )
                list.add(vo)
            }
            return@withContext list
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext arrayListOf<ChatRoomVO>()
        }
    }

    suspend fun getChatMessage(cateIdx: String, page: Int): ArrayList<ChatMessageVO> = withContext(Dispatchers.IO) {
        try {
            val accessToken = SPre.get(SCol.ACCESS_TOKEN.name)
            val server = MyApplication.SERVER
            val url = "${server}/get_chat_messages?cate_idx=${cateIdx}&page=${page}"
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
                    return@withContext getChatMessage(cateIdx, page)
                } else {
                    return@withContext arrayListOf()
                }
            }
            //

            val list = arrayListOf<ChatMessageVO>()
            val array = JSONArray(obj.getString("data"))
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val vo = ChatMessageVO(
                    msgType = obj.getInt("msg_type"),
                    idx = obj.getString("idx"),
                    message = obj.getString("message"),
                    link = obj.getString("link"),
                    isCompleted = obj.getInt("is_completed") == 1,
                    created = obj.getString("created"),
                )
                list.add(vo)
            }
            return@withContext list
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext arrayListOf<ChatMessageVO>()
        }
    }

    suspend fun getNotice(cateIdx: String): Pair<String, String> = withContext(Dispatchers.IO) {
        try {
            val accessToken = SPre.get(SCol.ACCESS_TOKEN.name)
            val server = MyApplication.SERVER
            val url = "${server}/get_notice?cate_idx=${cateIdx}"
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
                    return@withContext getNotice(cateIdx)
                } else {
                    return@withContext Pair("", "")
                }
            }
            //

            if (obj.isNull("message")) {
                return@withContext Pair("", "")
            } else {
                return@withContext Pair(obj.getString("idx"), obj.getString("message"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Pair("", "")
        }
    }

    suspend fun getSearchKeyword(cateIdx: String, query: String): ArrayList<ChatMessageVO> = withContext(Dispatchers.IO) {
        try {
            val accessToken = SPre.get(SCol.ACCESS_TOKEN.name)

            val server = MyApplication.SERVER
            val url = "${server}/get_search_keyword?cate_idx=${cateIdx}&query=${query}"
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
                    return@withContext getSearchKeyword(cateIdx, query)
                } else {
                    return@withContext arrayListOf()
                }
            }
            //

            val list = arrayListOf<ChatMessageVO>()
            val array = JSONArray(obj.getString("data"))
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                val vo = ChatMessageVO(
                    msgType = obj.getInt("msg_type"),
                    idx = obj.getString("idx"),
                    message = obj.getString("message"),
                    link = obj.getString("link"),
                    isCompleted = obj.getInt("is_completed") == 1,
                    created = obj.getString("created"),
                )
                list.add(vo)
            }
            return@withContext list
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext arrayListOf<ChatMessageVO>()
        }
    }


}