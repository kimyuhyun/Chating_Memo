package com.hongslab.chating_memo.manager

object CloudinaryUtils {
    fun getResizedImageUrl(originalUrl: String, width: Int? = null, height: Int? = null, quality: Int? = null): String {
        val transformations = mutableListOf<String>()

        width?.let { transformations.add("w_$it") }
        height?.let { transformations.add("h_$it") }
        quality?.let { transformations.add("q_$it") }

        if (width != null && height == null) {
            transformations.add("c_scale") // 비율 유지
        }

        val transformString = transformations.joinToString(",")
        return originalUrl.replace("/upload/", "/upload/$transformString/")
    }

    fun getThumbnailUrl(originalUrl: String, size: Int = 150): String {
        return originalUrl.replace("/upload/", "/upload/w_$size,h_$size,c_fill/")
    }

    fun getSmallImageUrl(originalUrl: String, width: Int = 800): String {
        return originalUrl.replace("/upload/", "/upload/w_$width,c_scale,q_80/")
    }
}