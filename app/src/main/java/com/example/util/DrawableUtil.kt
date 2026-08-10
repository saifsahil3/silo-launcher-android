package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.min

fun Drawable.toImageBitmapSafe(): ImageBitmap? {
    return try {
        if (this is BitmapDrawable && this.bitmap != null && !this.bitmap.isRecycled) {
            this.bitmap.asImageBitmap()
        } else {
            val rawW = if (intrinsicWidth > 0) intrinsicWidth else 96
            val rawH = if (intrinsicHeight > 0) intrinsicHeight else 96
            val canvasSize = maxOf(rawW, rawH).coerceIn(1, 256)
            val bitmap = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val scale = min(canvasSize.toFloat() / rawW, canvasSize.toFloat() / rawH)
            val targetW = (rawW * scale).toInt()
            val targetH = (rawH * scale).toInt()
            val left = (canvasSize - targetW) / 2
            val top = (canvasSize - targetH) / 2
            setBounds(left, top, left + targetW, top + targetH)
            draw(canvas)
            bitmap.asImageBitmap()
        }
    } catch (e: Throwable) {
        null
    }
}
