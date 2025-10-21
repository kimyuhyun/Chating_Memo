package com.hongslab.chating_memo.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hongslab.chating_memo.dialog.EmojiMapper.decodeEmojis
import com.hongslab.chating_memo.models.ChatRoomVO
import com.hongslab.chating_memo.models.DevVO
import com.hongslab.chating_memo.models.MyViewType
import com.hongslab.chating_memo.repository.GetDataRepository
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor() : ViewModel() {
    private val getDataRepository = GetDataRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _items = MutableLiveData<ArrayList<ChatRoomVO>>()
    val items: LiveData<ArrayList<ChatRoomVO>> = _items


    fun fetch() {
        try {
            viewModelScope.launch {
                val currentList = items.value ?: arrayListOf()

                val list = getDataRepository.getChatRooms()
                if (list.size > 0) {
                    list.add(0, ChatRoomVO(viewType = MyViewType.HEADER))
                    list.add(ChatRoomVO(viewType = MyViewType.FOOTER))
                }
                _items.postValue(list)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateLastMessage(pos: Int, lastMessage: String, isCompleted: Boolean) {
        val currentList = items.value ?: return
        val updatedList = ArrayList(currentList)
        updatedList[pos] = updatedList[pos].copy(uid = System.nanoTime(), message = decodeEmojis(lastMessage), isCompleted = isCompleted)
        _items.postValue(updatedList)
    }
}

