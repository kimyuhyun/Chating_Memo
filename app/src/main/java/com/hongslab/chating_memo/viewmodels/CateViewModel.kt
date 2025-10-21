package com.hongslab.chating_memo.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hongslab.chating_memo.MyApplication
import com.hongslab.chating_memo.models.CateVO
import com.hongslab.chating_memo.models.DevVO
import com.hongslab.chating_memo.models.GlobalVO
import com.hongslab.chating_memo.models.MyViewType
import com.hongslab.chating_memo.repository.CudRepository
import com.hongslab.chating_memo.repository.GetDataRepository
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import com.hongslab.chating_memo.utils.SCol
import com.hongslab.chating_memo.utils.SPre
import com.hongslab.chating_memo.views.fragments.Cate

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.jsoup.Jsoup
import javax.inject.Inject

@HiltViewModel
class CateViewModel @Inject constructor() : ViewModel() {
    private val getDataRepository = GetDataRepository()
    private val cudRepository = CudRepository()

    private val _items = MutableLiveData<ArrayList<CateVO>>()
    val items: LiveData<ArrayList<CateVO>> = _items


    fun fetch() {
        viewModelScope.launch {
            try {
                val list = getDataRepository.getCate()
                if (list.size > 0) {
                    list.add(0, CateVO(viewType = MyViewType.HEADER))
                    list.add(CateVO(viewType = MyViewType.FOOTER))
                }
                _items.postValue(list)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun add(name1: String) {
        viewModelScope.launch {
            try {
                val color = MyApplication.COLOR_CODE.random()
                val hashMap = HashMap<String, String>().apply {
                    put("color", color)
                    put("name1", name1)
                    put("table", "CHAT_MEMO_CATE_tbl")
                }
                val insertId = cudRepository.write(hashMap)
                if (insertId > 0) {
                    val currentList = items.value ?: arrayListOf()
                    val vo = CateVO(idx = "$insertId", name1 = name1, color = color)
                    val newList = ArrayList(currentList)
                    if (newList.size == 0) {
                        newList.add(0, CateVO(viewType = MyViewType.HEADER))
                        newList.add(CateVO(viewType = MyViewType.FOOTER))
                    }
                    newList.add(1, vo)
                    _items.postValue(newList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun update(idx: String, name1: String) {
        viewModelScope.launch {
            try {
                val hashMap = HashMap<String, String>().apply {
                    put("idx", idx)
                    put("name1", name1)
                    put("table", "CHAT_MEMO_CATE_tbl")
                }
                val insertId = cudRepository.write(hashMap)
                if (insertId == 0) {
                    val currentList = items.value ?: return@launch
                    val index = currentList.indexOfFirst { it.idx == idx }
                    val updatedList = ArrayList(currentList)

                    updatedList[index] = updatedList[index].copy(name1 = name1)
                    _items.postValue(updatedList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateColor(idx: String, color: String) {
        viewModelScope.launch {
            try {
                val hashMap = HashMap<String, String>().apply {
                    put("idx", idx)
                    put("color", color)
                    put("table", "CHAT_MEMO_CATE_tbl")
                }
                val insertId = cudRepository.write(hashMap)
                if (insertId == 0) {
                    val currentList = items.value ?: return@launch
                    val index = currentList.indexOfFirst { it.idx == idx }
                    val updatedList = ArrayList(currentList)
                    updatedList[index] = updatedList[index].copy(color = color)
                    _items.postValue(updatedList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateToTop(idx: String) {
        viewModelScope.launch {
            try {
                val hashMap = HashMap<String, String>()
                hashMap["idx"] = idx
                hashMap["modified"] = MyUtils.getCurrentTime()
                hashMap["table"] = "CHAT_MEMO_CATE_tbl"

                val insertId = cudRepository.write(hashMap)
                if (insertId == 0) {
                    val currentList = items.value ?: return@launch
                    val index = currentList.indexOfFirst { it.idx == idx }
                    val updatedList = ArrayList(currentList)

                    val copyObj = updatedList[index]

                    updatedList.removeAt(index)

                    val vo = CateVO(idx = idx, name1 = copyObj.name1, color = copyObj.color)
                    updatedList.add(1, vo)

                    _items.postValue(updatedList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun delete(idx: String) {
        viewModelScope.launch {
            try {
                val hashMap = HashMap<String, String>()
                hashMap["idx"] = idx
                hashMap["table"] = "CHAT_MEMO_CATE_tbl"

                val code = cudRepository.delete(hashMap)
                if (code == 1) {
                    val currentList = items.value ?: return@launch
                    val index = currentList.indexOfFirst { it.idx == idx }
                    val updatedList = ArrayList(currentList)
                    updatedList.removeAt(index)
                    _items.postValue(updatedList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

