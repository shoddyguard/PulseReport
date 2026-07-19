package dev.pulsereport.feature.sources

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Resolves a Health Connect data origin's package name to a human-readable app name. */
interface AppLabelResolver {
    fun labelFor(packageName: String): String
}

class PackageManagerAppLabelResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppLabelResolver {

    override fun labelFor(packageName: String): String =
        try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
}
