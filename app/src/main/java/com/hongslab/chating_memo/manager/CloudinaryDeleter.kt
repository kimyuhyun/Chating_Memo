package com.hongslab.chating_memo.manager

import com.hongslab.chating_memo.BuildConfig
import com.hongslab.chating_memo.BuildConfig.API_KEY
import com.hongslab.chating_memo.BuildConfig.API_SECRET
import com.hongslab.chating_memo.utils.Dlog
import com.hongslab.chating_memo.utils.MyUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class CloudinaryDeleter private constructor() {
    private val failedDeletions = mutableSetOf<String>()

    companion object {
        @Volatile
        private var INSTANCE: CloudinaryDeleter? = null

        fun getInstance(): CloudinaryDeleter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CloudinaryDeleter().also { INSTANCE = it }
            }
        }

        private var CLOUD_NAME = MyUtils.decrypt(BuildConfig.CLOUD_NAME)
        private val API_KEY = MyUtils.decrypt(BuildConfig.API_KEY)
        private var API_SECRET = MyUtils.decrypt(BuildConfig.API_SECRET)
        private var DELETE_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/destroy"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 단일 이미지 삭제
     */
    suspend fun deleteImage(imageUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val publicId = extractPublicId(imageUrl)
                if (publicId.isEmpty()) {
                    Dlog.e("Invalid image URL: $imageUrl")
                    return@withContext false
                }

                Dlog.d("Deleting image with publicId: $publicId")

                val timestamp = System.currentTimeMillis() / 1000
                val signature = generateSignature(publicId, timestamp)

                val formBody = FormBody.Builder()
                    .add("public_id", publicId)
                    .add("timestamp", timestamp.toString())
                    .add("api_key", API_KEY)
                    .add("signature", signature)
                    .build()

                val request = Request.Builder()
                    .url(DELETE_URL)
                    .post(formBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Dlog.d("Delete response: $responseBody")

                response.isSuccessful

            } catch (e: Exception) {
                Dlog.e("Failed to delete image: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * 여러 이미지 삭제
     */
    suspend fun deleteImages(imageUrls: List<String>, maxRetries: Int = 3): Int {
        return withContext(Dispatchers.IO) {
            var successCount = 0
            val failedUrls = mutableListOf<String>()

            // 첫 번째 시도
            imageUrls.forEach { url ->
                if (deleteImageWithDelay(url)) {
                    successCount++
                } else {
                    failedUrls.add(url)
                }
            }

            // 재시도
            var retryCount = 1
            while (failedUrls.isNotEmpty() && retryCount <= maxRetries) {
                Dlog.d("Retry attempt $retryCount for ${failedUrls.size} failed images")
                delay(retryCount * 1000L) // 점진적 지연

                val iterator = failedUrls.iterator()
                while (iterator.hasNext()) {
                    val url = iterator.next()
                    if (deleteImageWithDelay(url)) {
                        successCount++
                        iterator.remove()
                    }
                }
                retryCount++
            }

            // 영구적으로 실패한 이미지들 저장
            failedDeletions.addAll(failedUrls)

            Dlog.d("Final result: $successCount out of ${imageUrls.size} images deleted")
            if (failedUrls.isNotEmpty()) {
                Dlog.d("Permanently failed: $failedUrls")
            }

            successCount
        }
    }

    private suspend fun deleteImageWithDelay(url: String): Boolean {
        delay(300) // 요청 간격
        return deleteImage(url)
    }

    // 실패한 이미지들 나중에 재시도
    suspend fun retryFailedDeletions(): Int {
        val urlsToRetry = failedDeletions.toList()
        failedDeletions.clear()

        return deleteImages(urlsToRetry, 2)
    }

    /**
     * URL에서 public_id 추출
     */
    private fun extractPublicId(imageUrl: String): String {
        return try {
            // URL 예시: https://res.cloudinary.com/dse98rb39/image/upload/v1754373260/memo_app/rkcoiehcgrtoagsrsit2.jpg
            // public_id: memo_app/rkcoiehcgrtoagsrsit2

            val parts = imageUrl.split("/")
            val uploadIndex = parts.indexOf("upload")

            if (uploadIndex != -1 && uploadIndex + 2 < parts.size) {
                // upload 다음부터 파일명까지
                val pathParts = parts.subList(uploadIndex + 2, parts.size)

                // 버전 정보 제거 (v1754373260 같은 부분)
                val filteredParts = pathParts.filter { !it.startsWith("v") || !it.matches(Regex("v\\d+")) }

                // 마지막 파일의 확장자 제거
                val fileName = filteredParts.last().substringBeforeLast(".")
                val folderParts = filteredParts.dropLast(1)

                (folderParts + fileName).joinToString("/")
            } else {
                ""
            }
        } catch (e: Exception) {
            Dlog.e("Failed to extract public_id from: $imageUrl")
            ""
        }
    }

    /**
     * API 시그니처 생성
     */
    private fun generateSignature(publicId: String, timestamp: Long): String {
        return try {
            val toSign = "public_id=$publicId&timestamp=$timestamp$API_SECRET"
            val digest = MessageDigest.getInstance("SHA-1")
            val hash = digest.digest(toSign.toByteArray())

            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Dlog.e("Failed to generate signature: ${e.message}")
            ""
        }
    }

    /**
     * URL이 Cloudinary 이미지인지 확인
     */
    fun isCloudinaryUrl(url: String): Boolean {
        return url.contains("res.cloudinary.com") && url.contains(CLOUD_NAME)
    }
}