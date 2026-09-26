package com.example.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser

data class IconPackInfo(
    val packageName: String,
    val label: String
)

/**
 * Lightweight icon-pack support (Phase 2 of the design doc). Icon packs are regular
 * APKs that ship an "appfilter.xml" resource mapping launcher activity component
 * names to a themed drawable name, e.g.:
 *
 *   <item component="ComponentInfo{com.whatsapp/.Main}" drawable="whatsapp_icon" />
 *
 * This is the same discovery/format convention used by ADW, ago and most other
 * launchers, so it works with the large existing ecosystem of icon pack apps
 * without needing a bespoke format of our own.
 *
 * Every public function here is defensive: a malformed or missing appfilter.xml
 * degrades to "no themed icon for this app" rather than crashing the home screen -
 * consistent with the design doc's "never crash on home screen" priority.
 */
object IconPackManager {

    // One appfilter mapping cached per icon pack package for the process lifetime.
    private val appFilterCache = LruCache<String, Map<String, String>>(4)

    /** Icon packs installed on the device, detected via the de-facto ADW theme intent. */
    suspend fun getInstalledIconPacks(context: Context): List<IconPackInfo> = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val intent = Intent("org.adw.launcher.THEMES")
            val resolveInfos = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            resolveInfos
                .mapNotNull { info ->
                    val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                    val label = runCatching { info.loadLabel(pm).toString() }.getOrDefault(pkg)
                    IconPackInfo(packageName = pkg, label = label)
                }
                .distinctBy { it.packageName }
                .sortedBy { it.label.lowercase() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Returns a themed icon [Drawable] for [componentKey] (format:
     * "ComponentInfo{package/activity}") from [iconPackPackage], or null if the icon
     * pack has no mapping for that app (falls back to the app's own icon).
     */
    fun getThemedIcon(context: Context, iconPackPackage: String, componentKey: String): Drawable? {
        return try {
            val mapping = getOrParseAppFilter(context, iconPackPackage) ?: return null
            val drawableName = mapping[componentKey] ?: return null
            val packRes = context.packageManager.getResourcesForApplication(iconPackPackage)
            val resId = packRes.getIdentifier(drawableName, "drawable", iconPackPackage)
            if (resId == 0) return null
            androidx.core.content.res.ResourcesCompat.getDrawable(packRes, resId, null)
        } catch (e: Exception) {
            null
        }
    }

    private fun getOrParseAppFilter(context: Context, iconPackPackage: String): Map<String, String>? {
        appFilterCache.get(iconPackPackage)?.let { return it }

        return try {
            val pm = context.packageManager
            val res = pm.getResourcesForApplication(iconPackPackage)
            val xmlId = res.getIdentifier("appfilter", "xml", iconPackPackage)
            if (xmlId == 0) return null

            val parser: XmlResourceParser = res.getXml(xmlId)
            val map = mutableMapOf<String, String>()
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component")
                    val drawable = parser.getAttributeValue(null, "drawable")
                    if (component != null && drawable != null) {
                        map[component] = drawable
                    }
                }
                eventType = parser.next()
            }
            parser.close()

            val result = map.toMap()
            appFilterCache.put(iconPackPackage, result)
            result
        } catch (e: Exception) {
            null
        }
    }

    /** Builds the "ComponentInfo{pkg/activity}" key format icon packs' appfilter.xml uses. */
    fun componentKeyFor(packageName: String, activityName: String): String {
        return "ComponentInfo{$packageName/$activityName}"
    }

    fun clearCache() {
        appFilterCache.evictAll()
    }
}
