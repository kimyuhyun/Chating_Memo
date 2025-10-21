package com.hongslab.chating_memo.manager

sealed class UploadResult {
    data class Success(
        val imageUrls: List<String> = emptyList(),
        val uploadedCount: Int = 0
    ) : UploadResult()

    data class Error(
        val message: String = ""
    ) : UploadResult()
}