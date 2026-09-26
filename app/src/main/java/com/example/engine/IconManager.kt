package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object IconManager {
    // Keyed by "packageName|iconPackPackage" so switching icon packs doesn't serve a
    // stale bitmap cached under the plain packageName key.
    private val iconCache = LruCache<String, Bitmap>(300)

    private fun cacheKey(packageName: String, iconPackPackage: String?) =
        if (iconPackPackage.isNullOrBlank()) packageName else "$packageName|$iconPackPackage"

    /** Non-blocking. Decodes/caches off the main thread - never call from composition directly. */
    suspend fun getAppIconAsync(
        context: Context,
        packageName: String,
        activityName: String? = null,
        iconPackPackage: String? = null
    ): Bitmap = withContext(Dispatchers.IO) {
        getAppIcon(context, packageName, activityName, iconPackPackage)
    }

    /** Returns a cached bitmap immediately if we already have one, without touching disk/IO. */
    fun peekCached(packageName: String, iconPackPackage: String? = null): Bitmap? =
        iconCache.get(cacheKey(packageName, iconPackPackage))

    /**
     * Blocking icon lookup - only ever call this from a background dispatcher
     * ([getAppIconAsync] does that for you). Kept internal-ish but not private so
     * call sites that are already confirmed to be on a background thread can use it
     * directly without a redundant withContext hop.
     */
    fun getAppIcon(
        context: Context,
        packageName: String,
        activityName: String? = null,
        iconPackPackage: String? = null
    ): Bitmap {
        val key = cacheKey(packageName, iconPackPackage)
        iconCache.get(key)?.let { return it }

        val pm = context.packageManager

        val themedDrawable: Drawable? = if (!iconPackPackage.isNullOrBlank() && !activityName.isNullOrBlank()) {
            val componentKey = IconPackManager.componentKeyFor(packageName, activityName)
            IconPackManager.getThemedIcon(context, iconPackPackage, componentKey)
        } else null

        val drawable = themedDrawable ?: try {
            pm.getApplicationIcon(packageName)
        } catch (e: Exception) {
            pm.defaultActivityIcon
        }

        val bitmap = drawableToBitmap(drawable)
        iconCache.put(key, bitmap)
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
