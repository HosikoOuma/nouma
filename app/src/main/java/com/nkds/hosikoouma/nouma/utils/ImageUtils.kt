package com.nkds.hosikoouma.nouma.utils

import android.content.Context
import android.net.Uri
import coil.request.ImageRequest
import java.io.File

fun createAvatarImageRequest(context: Context, avatarUri: Uri): ImageRequest {
    val path = avatarUri.path
    val builder = ImageRequest.Builder(context).data(avatarUri)
    if (path != null) {
        val file = File(path)
        if (file.exists()) {
            val cacheKey = "${avatarUri.path}:${file.lastModified()}"
            builder.memoryCacheKey(cacheKey)
            builder.diskCacheKey(cacheKey)
        }
    }
    return builder.build()
}
