package com.nkds.hosikoouma.nouma.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.util.UUID

/**
 * Копирует файл из временного URI (например, от галереи) в приватное хранилище приложения,
 * сохраняя при этом правильное расширение файла.
 * @return URI нового, сохраненного файла.
 */
fun copyUriToInternalStorage(context: Context, uri: Uri): Uri? {
    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

        // Создаем имя файла с правильным расширением
        val fileName = "media_${UUID.randomUUID()}.$extension"
        val file = File(context.filesDir, fileName)

        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val outputStream = file.outputStream()

        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()

        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@SuppressLint("Range")
fun getFileNameFromUri(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result ?: "file"
}
