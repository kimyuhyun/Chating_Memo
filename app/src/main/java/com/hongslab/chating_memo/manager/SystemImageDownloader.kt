package com.hongslab.chating_memo.manager

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

class SystemImageDownloader(private val context: Context) {
    fun downloadImage(url: String, fileName: String): Long {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("이미지 다운로드")
            setDescription("이미지를 다운로드 중입니다...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)
    }

    // 다운로드 상태 확인
    fun checkDownloadStatus(downloadId: Long): Int {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        return if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
        } else {
            DownloadManager.STATUS_FAILED
        }
    }
}