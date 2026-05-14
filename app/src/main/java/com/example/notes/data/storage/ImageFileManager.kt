package com.example.notes.data.storage

import android.content.Context
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ImageFileManager(
    private val context: Context
) {
    private val imagesDir: File = context.filesDir

    suspend fun copyImageToInternalStorage(url: String): String {
        val fileName = "IMG_${UUID.randomUUID()}.jpg"
        val file = File(imagesDir, fileName)

        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(url.toUri())?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        return file.absolutePath
    }

    suspend fun deleteImage(url: String) {
        withContext(Dispatchers.IO) {
            val file = File(url)
            if (file.exists() && isInternal(file.absolutePath)) {
                file.delete()
            }
        }
    }

    fun isInternal(url: String): Boolean {
        return url.startsWith(imagesDir.absolutePath)
    }
}