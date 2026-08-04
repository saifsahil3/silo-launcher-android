package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

fun Drawable.toImageBitmapSafe(): ImageBitmap? {
    return try {
        if (this is BitmapDrawable && this.bitmap != null && !this.bitmap.isRecycled) {
            this.bitmap.asImageBitmap()
        } else {
            val rawW = if (intrinsicWidth > 0) intrinsicWidth else 96
            val rawH = if (intrinsicHeight > 0) intrinsicHeight else 96
            val width = rawW.coerceIn(1, 256)
            val height = rawH.coerceIn(1, 256)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            bitmap.asImageBitmap()
        }
    } catch (e: Throwable) {
        null
    }
}

