package com.hongslab.chating_memo.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hongslab.chating_memo.dialog.EmojiMapper
import com.hongslab.chating_memo.manager.CloudinaryDeleter
import com.hongslab.chating_memo.models.ChatMessageVO
import com.hongslab.chating_memo.models.NoticeTextState
import com.hongslab.chating_memo.repository.CudRepository
import com.hongslab.chating_memo.repository.GetDataRepository
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable.isCompleted
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class ChatMessageViewModel @Inject constructor() : ViewModel() {
    private val getDataRepository = GetDataRepository()
    private val cudRepository = CudRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _items = MutableLiveData<ArrayList<ChatMessageVO>>()
    val items: LiveData<ArrayList<ChatMessageVO>> = _items

    private val _noticeTextStateItem = MutableLiveData<NoticeTextState>()
    val noticeTextStateItem: LiveData<NoticeTextState> = _noticeTextStateItem

    private var page = 0
    private var isEnd = false
    var uiScrollPosition = 0

    fun refresh(cateIdx: String) {
        isEnd = false
        page = 0
        _items.postValue(arrayListOf())

        fetch(cateIdx)
    }

    fun fetch(cateIdx: String) {
        if (isLoading.value == true) {
            return
        }

        if (isEnd) {
            return
        }

        try {
            viewModelScope.launch {
                _isLoading.postValue(true)

                val (idx, msg) = getDataRepository.getNotice(cateIdx)
                val item = NoticeTextState(idx, msg, false)
                _noticeTextStateItem.postValue(item)

                val currentList = items.value ?: arrayListOf()

                page += 1
                val updatedList = getDataRepository.getChatMessage(cateIdx, page)
                if (updatedList.size < 100) {
                    isEnd = true
                }

                updatedList.addAll(currentList)

                uiScrollPosition = if (page == 1) {
                    updatedList.size - 1
                } else {
                    -1
                }

                _items.postValue(updatedList)

                _isLoading.postValue(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun sendMessage(cateIdx: String, message: String) {
        try {
            viewModelScope.launch {
                val encodedMessage = EmojiMapper.encodeEmojis(message)

                val hashMap = HashMap<String, String>().apply {
                    put("table", "CHAT_MEMO_MSG_tbl")
                    put("cate_idx", cateIdx)
                    put("message", encodedMessage)
                    put("msg_type", "1")
                }

                val result = cudRepository.write(hashMap)     // insertId 리턴함
                if (result > 0) {
                    val currentList = items.value ?: arrayListOf()
                    val vo = ChatMessageVO(
                        uid = System.nanoTime(), // 고유 ID 생성
                        idx = "$result",
                        msgType = 1,
                        message = message,
                        link = "",
                        created = MyUtils.getCurrentTime()
                    )
                    Dlog.d("Created new message with uid: ${vo.uid}")

                    val newList = ArrayList(currentList)
                    newList.add(vo)

                    uiScrollPosition = newList.size - 1
                    _items.postValue(newList)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateMessage(idx: String, message: String) {
        try {
            viewModelScope.launch {
                val encodedMessage = EmojiMapper.encodeEmojis(message)

                val hashMap = HashMap<String, String>().apply {
                    put("table", "CHAT_MEMO_MSG_tbl")
                    put("idx", idx)
                    put("message", encodedMessage)
                }

                val result = cudRepository.write(hashMap)     // insertId: 0 리턴함
                if (result == 0) {
                    val currentList = items.value ?: arrayListOf()
                    // idx로 실제 위치 찾기 (더 안전)
                    val actualPos = currentList.indexOfFirst { it.idx == idx }
                    if (actualPos == -1) {
                        return@launch
                    }

                    val item = currentList[actualPos]
                    val newItem = item.copy(uid = System.nanoTime(), message = message)
                    val updatedList = ArrayList(currentList)
                    updatedList[actualPos] = newItem
                    val newList = ArrayList(updatedList)
                    _items.postValue(newList)

                    uiScrollPosition = -1
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun sendImageMessage(cateIdx: String, urls: String) {
        try {
            viewModelScope.launch {
                val hashMap = HashMap<String, String>().apply {
                    put("table", "CHAT_MEMO_MSG_tbl")
                    put("cate_idx", cateIdx)
                    put("message", "이미지")
                    put("msg_type", "2")
                    put("link", urls)
                }

                val result = cudRepository.write(hashMap)   // insertId 리턴함.
                if (result > 0) {
                    val currentList = items.value ?: arrayListOf()
                    val vo = ChatMessageVO(
                        uid = System.nanoTime(), // 고유 ID 생성
                        idx = "$result",
                        msgType = 2,
                        link = urls,
                        created = MyUtils.getCurrentTime()
                    )
                    Dlog.d("Created new image message with uid: ${vo.uid}")

                    val updatedList = ArrayList(currentList)
                    updatedList.add(vo)
                    uiScrollPosition = updatedList.size - 1

                    _items.postValue(updatedList)
                }

                // 이미지 업로드 테이블에도 등록!
                val urlsArr = urls.split(",")
                    .filter { it.isNotBlank() }
                    .map { it.trim() } // Remove whitespace
                urlsArr.forEach { url ->
                    val hashMap = HashMap<String, String>().apply {
                        put("table", "CHAT_MEMO_IMG_tbl")
                        put("url", url)
                    }
                    cudRepository.write(hashMap)
                }
                //

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setNotice(cateIdx: String, message: String) {
        viewModelScope.launch {
            try {
                val currentNoticeTextStateItem = noticeTextStateItem.value ?: return@launch


                val hashMap = HashMap<String, String>().apply {
                    if (currentNoticeTextStateItem.idx != "") {
                        put("idx", currentNoticeTextStateItem.idx)
                    }
                    put("cate_idx", cateIdx)
                    put("message", message)
                    put("table", "CHAT_MEMO_NOTICE_tbl")
                }

                val result = cudRepository.write(hashMap) // 0 보다 그면 insertId, 0 은 업데이트

                val updatedItem = if (result > 0) {
                    NoticeTextState("$result", message, false)
                } else {
                    NoticeTextState(currentNoticeTextStateItem.idx, message, false)
                }
                _noticeTextStateItem.postValue(updatedItem)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun delNotice(cateIdx: String) {
        viewModelScope.launch {
            try {
                val currentNoticeTextStateItem = noticeTextStateItem.value ?: return@launch

                val hashMap = HashMap<String, String>().apply {
                    put("idx", currentNoticeTextStateItem.idx)
                    put("cate_idx", cateIdx)
                    put("message", "")
                    put("table", "CHAT_MEMO_NOTICE_tbl")
                }
                cudRepository.write(hashMap) // 0 보다 그면 insertId, 0 은 업데이트
                val updatedItem = NoticeTextState(currentNoticeTextStateItem.idx, "", false)
                _noticeTextStateItem.postValue(updatedItem)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setCompleted(idx: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val currentList = items.value ?: arrayListOf()

            // idx로 실제 위치 찾기 (더 안전)
            val actualPos = currentList.indexOfFirst { it.idx == idx }
            if (actualPos == -1) {
                return@launch
            }

            val item = currentList[actualPos]

            val hashMap = HashMap<String, String>().apply {
                put("idx", idx)
                put("is_completed", if (isCompleted) "1" else "0")
                put("table", "CHAT_MEMO_MSG_tbl")
            }

            val result = cudRepository.write(hashMap)

            if (result == 0) {      // 0 보다 그면 insertId, 0 은 업데이트
                val newItem = item.copy(uid = System.nanoTime(), isCompleted = isCompleted)
                val updatedList = ArrayList(currentList)
                updatedList[actualPos] = newItem

                val newList = ArrayList(updatedList)

                uiScrollPosition = -1

                _items.postValue(newList)

            } else {
                Dlog.e("Failed to delete item from database")
            }
        }
    }

    fun setGotoBottom(idx: String) {
        viewModelScope.launch {
            val currentList = items.value ?: arrayListOf()

            // idx로 실제 위치 찾기 (더 안전)
            val actualPos = currentList.indexOfFirst { it.idx == idx }
            if (actualPos == -1) {
                return@launch
            }

            val item = currentList[actualPos]

            val hashMap = HashMap<String, String>().apply {
                put("idx", idx)
                put("created", MyUtils.getCurrentTime())
                put("table", "CHAT_MEMO_MSG_tbl")
            }

            val result = cudRepository.write(hashMap)

            if (result == 0) {      // 0 보다 그면 insertId, 0 은 업데이트
                val updatedList = ArrayList(currentList)
                updatedList.removeAt(actualPos)

                val newItem = item.copy(uid = System.nanoTime())
                updatedList.add((newItem))

                val newList = ArrayList(updatedList)

                uiScrollPosition = -1

                _items.postValue(newList)

            } else {
                Dlog.e("Failed to delete item from database")
            }
        }
    }

    fun delete(idx: String) {
        viewModelScope.launch {
            val currentList = items.value ?: arrayListOf()

            // idx로 실제 위치 찾기 (더 안전)
            val actualPos = currentList.indexOfFirst { it.idx == idx }
            if (actualPos == -1) {
                return@launch
            }

            val item = currentList[actualPos]

            val hashMap = HashMap<String, String>().apply {
                put("idx", idx)
                put("msg_type", "0")
                put("table", "CHAT_MEMO_MSG_tbl")
            }

            val result = cudRepository.write(hashMap)

            if (result == 0) {
                val newList = ArrayList(currentList)
                newList.removeAt(actualPos)
                _items.postValue(newList)

                // 이미지 삭제는 별도로 (UI 블로킹 방지) 뒤로가기도 계속 살아있다.!
                GlobalScope.launch(Dispatchers.IO) {
                    deleteCloudinaryImages(item)
                }
            } else {
                Dlog.e("Failed to delete item from database")
            }
        }
    }

    private suspend fun deleteCloudinaryImages(item: ChatMessageVO) {
        if (item.msgType == 2 && item.link.isNotBlank()) {
            try {
                val urls = item.link.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                if (urls.isNotEmpty()) {
                    val deletedCount = CloudinaryDeleter.getInstance().deleteImages(urls)
                    Dlog.d("Deleted $deletedCount out of ${urls.size} images")
                }
            } catch (e: Exception) {
                Dlog.e("Failed to delete Cloudinary images: ${e.message}")
            }
        }
    }

    fun setNoticeExpand() {
        val currentItem = noticeTextStateItem.value ?: return
        val updatedItem = currentItem.copy(isExpand = !currentItem.isExpand)
        _noticeTextStateItem.postValue(updatedItem)
    }


    fun mergeAndSendMessages(cateIdx: String) {
        viewModelScope.launch {

            _isLoading.postValue(true)

            try {
                val currentList = items.value ?: arrayListOf()

                // 1. 체크된 항목들 찾기
                val checkedItems = currentList.filter { it.isChecked && it.msgType == 1 }

                if (checkedItems.isEmpty()) {
                    Dlog.d("No checked items to merge")
                    return@launch
                }

                // 2. 메시지 디코딩 & 합치기 (생성 시간 순으로 정렬)
                val mergedMessage = checkedItems
                    .sortedBy { it.created }
                    .map { EmojiMapper.decodeEmojis(it.message) }
                    .joinToString("\n")

                // 3. 합쳐진 메시지 전송
                val encodedMessage = EmojiMapper.encodeEmojis(mergedMessage)

                val hashMap = HashMap<String, String>().apply {
                    put("table", "CHAT_MEMO_MSG_tbl")
                    put("cate_idx", cateIdx)
                    put("message", encodedMessage)
                    put("msg_type", "1")
                }

                val newIdx = cudRepository.write(hashMap)

                if (newIdx > 0) {
                    Dlog.d("Successfully merged ${checkedItems.size} messages into idx: $newIdx")

                    // 4. 체크된 항목들 DB에서 삭제
                    val deletedCount = checkedItems.count { item ->
                        val deleteHashMap = HashMap<String, String>().apply {
                            put("idx", item.idx)
                            put("msg_type", "0")
                            put("table", "CHAT_MEMO_MSG_tbl")
                        }
                        cudRepository.write(deleteHashMap) == 0
                    }

                    Dlog.d("Deleted $deletedCount out of ${checkedItems.size} messages")

                    // 5. UI 업데이트 - 체크된 항목 제거 & 새 항목 추가
                    val newMessage = ChatMessageVO(
                        uid = System.nanoTime(),
                        idx = "$newIdx",
                        msgType = 1,
                        message = mergedMessage,
                        link = "",
                        created = MyUtils.getCurrentTime()
                    )

                    val updatedList = currentList.filterNot { it.isChecked }
                    val newList = ArrayList(updatedList)
                    newList.add(newMessage)

                    uiScrollPosition = newList.size - 1
                    _items.postValue(newList)
                } else {
                    Dlog.e("Failed to merge messages")
                }

            } catch (e: Exception) {
                Dlog.e("Error merging messages: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}

