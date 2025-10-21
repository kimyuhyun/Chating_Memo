package com.hongslab.chating_memo.manager

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.hongslab.chating_memo.BuildConfig
import com.hongslab.chating_memo.utils.MyUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class CloudinaryUploader(private val activity: ComponentActivity) {

    companion object {
        private var CLOUD_NAME = MyUtils.decrypt(BuildConfig.CLOUD_NAME)
        private const val UPLOAD_PRESET = BuildConfig.UPLOAD_PRESET
        private const val FOLDER = "memo_app"
        private const val MAX_IMAGES = 10
    }

    private val currentImageUrls = mutableListOf<String>()
    private var isUploading = false
    private var onUploadResult: ((UploadResult) -> Unit)? = null
    private var onUploadProgress: ((Boolean) -> Unit)? = null

    // 이미지 선택 런처
    private val imagePickerLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                performUpload(uris)
            }
        }

    /**
     * 이미지 선택 및 업로드 시작
     */
    fun selectAndUploadImages(
        onResult: (UploadResult) -> Unit,
        onProgress: (Boolean) -> Unit = {}
    ) {
        if (isUploading) {
            onResult(UploadResult.Error("이미지 업로드 중입니다. 잠시만 기다려주세요."))
            return
        }

        this.onUploadResult = onResult
        this.onUploadProgress = onProgress

        imagePickerLauncher.launch("image/*")
    }

    /**
     * 현재 이미지 URL 목록 설정
     */
    fun setCurrentImageUrls(urls: List<String>) {
        currentImageUrls.clear()
        currentImageUrls.addAll(urls)
    }

    /**
     * 현재 이미지 URL 목록 가져오기
     */
    fun getCurrentImageUrls(): List<String> = currentImageUrls.toList()

    private fun performUpload(imageUris: List<Uri>) {
        if (isUploading) return

        isUploading = true
        onUploadProgress?.invoke(true)

        activity.lifecycleScope.launch {
            try {
                val result = uploadImages(imageUris, currentImageUrls, activity)

                when (result) {
                    is UploadResult.Success -> {
                        currentImageUrls.clear()
                        currentImageUrls.addAll(result.imageUrls)
                    }

                    is UploadResult.Error -> {
                        // 에러 시 현재 URL 목록 유지
                    }
                }

                onUploadResult?.invoke(result)

            } catch (e: Exception) {
                onUploadResult?.invoke(UploadResult.Error("업로드 중 오류가 발생했습니다: ${e.message}"))
            } finally {
                isUploading = false
                onUploadProgress?.invoke(false)
            }
        }
    }

    private suspend fun uploadImages(
        imageUris: List<Uri>,
        currentImageUrls: List<String>,
        context: Context
    ): UploadResult {
        // 개수 체크
        val totalCount = currentImageUrls.size + imageUris.size
        if (totalCount > MAX_IMAGES) {
            val remainingCount = MAX_IMAGES - currentImageUrls.size
            return UploadResult.Error("이미지는 10개까지만 업로드 가능합니다.\n추가로 ${remainingCount}장 업로드 가능합니다.")
        }

        return try {
            val uploadedUrls = mutableListOf<String>()

            // 병렬 업로드
            val jobs = imageUris.map { uri ->
                CoroutineScope(Dispatchers.IO).async {
                    uploadSingleImage(uri, context)
                }
            }

            // 모든 업로드 완료까지 대기
            jobs.forEach { job ->
                val url = job.await()
                if (url != null) {
                    uploadedUrls.add(url)
                }
            }

            val newImageUrls = currentImageUrls + uploadedUrls
            UploadResult.Success(newImageUrls, uploadedUrls.size)

        } catch (e: Exception) {
            UploadResult.Error("업로드 실패: ${e.message}")
        }
    }

    private suspend fun uploadSingleImage(uri: Uri, context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) return@withContext null

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        "image.jpg",
                        bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    )
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("folder", FOLDER)
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody ?: "")
                    jsonObject.getString("secure_url")
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("CloudinaryUpload", "Upload failed", e)
                null
            }
        }
    }
}