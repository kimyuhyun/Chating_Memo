package com.hongslab.chating_memo.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hongslab.chating_memo.models.DevVO

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class DevViewModel @Inject constructor() : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _items = MutableLiveData<ArrayList<DevVO>>()
    val items: LiveData<ArrayList<DevVO>> = _items


    fun fetch() {
        viewModelScope.launch {
            try {

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {

            }
        }
    }

}

