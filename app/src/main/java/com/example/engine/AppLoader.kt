package com.example.engine

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.UserManager
import android.provider.Settings
import com.example.model.AppCategory
import com.example.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppLoader {

    suspend fun loadInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        val appList = mutableListOf<AppInfo>()
        val pm = context.packageManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
                val userProfiles = userManager?.userProfiles ?: listOf(Process.myUserHandle())

                if (launcherApps != null) {
                    for (profile in userProfiles) {
                        val activities = launcherApps.getActivityList(null, profile)
                        for (activity in activities) {
                            val pkg = activity.applicationInfo.packageName
                            // Don't show our own launcher in the app drawer
                            if (pkg == context.packageName) continue

                            val label = activity.label.toString()
                            val category = categorizeApp(activity.applicationInfo, label, pkg)
                            val installTime = runCatching {
                                pm.getPackageInfo(pkg, 0).firstInstallTime
                            }.getOrDefault(0L)

                            appList.add(
                                AppInfo(
                                    packageName = pkg,
                                    activityName = activity.componentName.className,
                                    label = label,
                                    category = category,
                                    installTime = installTime
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback or complement with PackageManager query
        if (appList.isEmpty()) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            for (resolveInfo in resolveInfos) {
                val pkg = resolveInfo.activityInfo.packageName
                if (pkg == context.packageName) continue

                val label = resolveInfo.loadLabel(pm).toString()
                val category = categorizeApp(resolveInfo.activityInfo.applicationInfo, label, pkg)
                val installTime = runCatching {
                    pm.getPackageInfo(pkg, 0).firstInstallTime
                }.getOrDefault(0L)

                appList.add(
                    AppInfo(
                        packageName = pkg,
                        activityName = resolveInfo.activityInfo.name,
                        label = label,
                        category = category,
                        installTime = installTime
                    )
                )
            }
        }

        // Distinct by packageName
        val distinctApps = appList.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
        distinctApps
    }

    private fun categorizeApp(appInfo: ApplicationInfo, label: String, packageName: String): AppCategory {
        val lowerPkg = packageName.lowercase()
        val lowerLabel = label.lowercase()

        // 1. Android standard categories
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.SOCIAL
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.PRODUCTIVITY
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
                ApplicationInfo.CATEGORY_MAPS,
                ApplicationInfo.CATEGORY_NEWS -> return AppCategory.TOOLS
            }
        }

        // 2. Keyword heuristics
        if (lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") || lowerPkg.contains("twitter") ||
            lowerPkg.contains("discord") || lowerPkg.contains("instagram") || lowerPkg.contains("facebook") ||
            lowerPkg.contains("reddit") || lowerPkg.contains("messenger") || lowerPkg.contains("signal") ||
            lowerLabel.contains("chat") || lowerLabel.contains("message") || lowerLabel.contains("social")
        ) {
            return AppCategory.SOCIAL
        }

        if (lowerPkg.contains("youtube") || lowerPkg.contains("spotify") || lowerPkg.contains("music") ||
            lowerPkg.contains("camera") || lowerPkg.contains("gallery") || lowerPkg.contains("photos") ||
            lowerPkg.contains("netflix") || lowerPkg.contains("player") || lowerPkg.contains("video") ||
            lowerLabel.contains("music") || lowerLabel.contains("camera") || lowerLabel.contains("photo")
        ) {
            return AppCategory.MEDIA
        }

        if (lowerPkg.contains("mail") || lowerPkg.contains("gmail") || lowerPkg.contains("docs") ||
            lowerPkg.contains("sheets") || lowerPkg.contains("slides") || lowerPkg.contains("calendar") ||
            lowerPkg.contains("notes") || lowerPkg.contains("keep") || lowerPkg.contains("slack") ||
            lowerPkg.contains("office") || lowerPkg.contains("task") || lowerPkg.contains("todo") ||
            lowerPkg.contains("drive") || lowerLabel.contains("calendar") || lowerLabel.contains("note")
        ) {
            return AppCategory.PRODUCTIVITY
        }

        if (lowerPkg.contains("game") || lowerPkg.contains("play.games") || lowerLabel.contains("game") ||
            lowerLabel.contains("puzzle") || lowerLabel.contains("arcade")
        ) {
            return AppCategory.GAMES
        }

        return AppCategory.TOOLS
    }

    fun launchApp(context: Context, packageName: String, activityName: String? = null) {
        try {
            val pm = context.packageManager
            var intent: Intent? = null
            if (!activityName.isNullOrBlank()) {
                intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setClassName(packageName, activityName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }
            }

            if (intent == null || pm.resolveActivity(intent, 0) == null) {
                intent = pm.getLaunchIntentForPackage(packageName)?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }
            }

            if (intent != null) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openAppInfo(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun requestUninstall(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun expandNotificationShade(context: Context) {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val expand = statusBarManager.getMethod("expandNotificationsPanel")
            expand.invoke(statusBarService)
        } catch (e: Exception) {
            // Fallback: open Settings
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }
}
