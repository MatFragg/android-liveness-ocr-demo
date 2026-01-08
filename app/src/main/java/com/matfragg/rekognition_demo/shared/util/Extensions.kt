package com.matfragg.rekognition_demo.shared.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.core.graphics.scale

fun Bitmap.toBase64(quality: Int = 40): String {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}

fun File.toOptimizedBitmap(maxWidth: Int = 640): Bitmap {
    val bitmap = BitmapFactory.decodeFile(absolutePath)
    val height = (bitmap.height * (maxWidth.toFloat() / bitmap.width)).toInt()
    return bitmap.scale(maxWidth, height)
}

fun String.toPercentage(): String = "${this}%"