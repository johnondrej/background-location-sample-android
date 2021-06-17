package io.github.johnondrej.backgroundlocation.ktx

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

// Android Context extensions

fun Context.arePermissionsGranted(requireAll: Boolean = true, vararg permissions: String): Boolean {
    val permissionChecker = { permission: String ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    return if (requireAll) permissions.all(permissionChecker) else permissions.any(permissionChecker)
}

fun Context.areLocationPermissionsGranted(requireBackground: Boolean): Boolean {
    val foregroundPermissionsGranted = arePermissionsGranted(requireAll = false, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
    val backgroundPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arePermissionsGranted(requireAll = true, ACCESS_BACKGROUND_LOCATION)
    } else {
        true
    }

    return if (!requireBackground) foregroundPermissionsGranted else foregroundPermissionsGranted && backgroundPermissionGranted
}