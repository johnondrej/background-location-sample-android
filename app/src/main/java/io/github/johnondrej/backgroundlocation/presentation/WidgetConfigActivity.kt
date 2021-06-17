package io.github.johnondrej.backgroundlocation.presentation

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import io.github.johnondrej.backgroundlocation.R
import io.github.johnondrej.backgroundlocation.data.WidgetUpdateService
import io.github.johnondrej.backgroundlocation.databinding.ActivityWidgetConfigBinding
import io.github.johnondrej.backgroundlocation.ktx.areLocationPermissionsGranted

class WidgetConfigActivity : AppCompatActivity(), MessageDialog.DialogConfirmationListener {

    private val appWidgetId: Int
        get() = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var viewBinding: ActivityWidgetConfigBinding

    private val permissionsRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (areLocationPermissionsGranted(requireBackground = true)) {
            onPermissionsGranted()
        } else {
            onPermissionsRejected()
        }
    }

    private val foregroundPermissionsRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (areLocationPermissionsGranted(requireBackground = false)) {
            viewBinding.layoutAndroid11Permissions.isVisible = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityWidgetConfigBinding.inflate(layoutInflater)

        with(viewBinding) {
            txtPrivacyNote.movementMethod = LinkMovementMethod.getInstance()
            btnAdd.setOnClickListener {
                onAddWidget()
            }
            layoutAndroid11Permissions.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && !areLocationPermissionsGranted(requireBackground = false)
            btnGrantPermissions.setOnClickListener {
                foregroundPermissionsRequest.launch(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))
            }
            setContentView(root)
        }
    }

    override fun onDialogConfirmed(requestCode: Int) {
        when (requestCode) {
            REQUEST_GRANT_BG_PERMISSIONS -> {
                val permissions = arrayOf(
                    ACCESS_COARSE_LOCATION,
                    ACCESS_FINE_LOCATION
                ) + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    arrayOf(ACCESS_BACKGROUND_LOCATION)
                } else {
                    emptyArray()
                }

                permissionsRequest.launch(permissions)
            }
        }
    }

    private fun onAddWidget() {
        if (!areLocationPermissionsGranted(requireBackground = true)) {
            MessageDialog.newInstance(messageRes = R.string.widget_config_permissions_consent, requestCode = REQUEST_GRANT_BG_PERMISSIONS)
                .show(supportFragmentManager, MessageDialog::class.java.name)
        } else {
            onPermissionsGranted()
        }
    }

    private fun onPermissionsGranted() {
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            WidgetUpdateService.enqueueWork(context = this, appWidgetId)

            setResult(Activity.RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
            finish()
        }
    }

    private fun onPermissionsRejected() {
        MessageDialog.newInstance(messageRes = R.string.widget_config_permissions_not_granted, requestCode = REQUEST_REJECTED_PERMISSIONS)
            .show(supportFragmentManager, MessageDialog::class.java.name)
    }

    companion object {

        private const val REQUEST_GRANT_BG_PERMISSIONS = 1
        private const val REQUEST_REJECTED_PERMISSIONS = 2
    }
}