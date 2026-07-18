package com.t8rin.embeddedpicker.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

enum class MediaAccessLevel {
    FULL,
    PARTIAL,
    NONE;

    val isGranted: Boolean
        get() = this != NONE
}

object MediaAccessPermissions {

    fun accessLevel(context: Context): MediaAccessLevel {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                isGranted(context, Manifest.permission.READ_MEDIA_IMAGES) -> MediaAccessLevel.FULL

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                isGranted(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> MediaAccessLevel.PARTIAL

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> MediaAccessLevel.NONE

            isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE) -> MediaAccessLevel.FULL
            else -> MediaAccessLevel.NONE
        }
    }

    fun permissionsForRequest(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES
            )

            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun isGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }
}
