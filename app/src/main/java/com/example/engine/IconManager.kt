package com.example.engine

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object IconManager {
    private val iconCache = LruCache<String, Bitmap>(200)

    fun getAppIcon(context: Context, packageName: String): Bitmap {
        iconCache.get(packageName)?.let { return it }

        val pm = context.packageManager
        val drawable = try {
            pm.getApplicationIcon(packageName)
        } catch (e: Exception) {
            pm.defaultActivityIcon
        }

        val bitmap = drawableToBitmap(drawable)
        iconCache.put(packageName, bitmap)
        return bitmap
    }

    fun getAppImageBitmap(context: Context, packageName: String): ImageBitmap {
        return getAppIcon(context, packageName).asImageBitmap()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun clearCache() {
        iconCache.evictAll()
    }
}
