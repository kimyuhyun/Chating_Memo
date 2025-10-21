package com.hongslab.chating_memo.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hongslab.chating_memo.dialog.EmojiMapper
import com.hongslab.chating_memo.models.ChatMessageVO
import com.hongslab.chating_memo.models.DevVO
import com.hongslab.chating_memo.repository.CudRepository
import com.hongslab.chating_memo.repository.GetDataRepository
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {
    private val getDataRepository = GetDataRepository()
    private val cudRepository = CudRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _items = MutableLiveData<ArrayList<ChatMessageVO>>()
    val items: LiveData<ArrayList<ChatMessageVO>> = _items

    var uiScrollPosition = 0

    fun fetch(cateIdx: String, query: String) {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)

                val list = getDataRepository.getSearchKeyword(cateIdx, query)
                _items.postValue(list)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
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
            } else {
                Dlog.e("Failed to delete item from database")
            }
        }
    }

    fun mergeAndSendMessages(cateIdx: String) {
        viewModelScope.launch {
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
            }
        }
    }
}

