package io.github.johnondrej.backgroundlocation.data

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import androidx.core.app.JobIntentService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import io.github.johnondrej.backgroundlocation.R
import io.github.johnondrej.backgroundlocation.ktx.areLocationPermissionsGranted
import io.github.johnondrej.backgroundlocation.presentation.LocationWidget
import java.time.LocalDateTime

/**
 * Service that updates widget content.
 */
class WidgetUpdateService : JobIntentService() {

    @SuppressLint("MissingPermission") // checked by areLocationPermissionsGranted extension
    override fun onHandleWork(intent: Intent) {
        val appWidgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val locationProvider = LocationServices.getFusedLocationProviderClient(this)

            if (areLocationPermissionsGranted(requireBackground = true)) {
                try {
                    val location = Tasks.await(locationProvider.lastLocation)
                    if (location != null) {
                        val locationGeocoded = try {
                            Geocoder(this)
                                .getFromLocation(location.latitude, location.longitude, 1)
                                .firstOrNull()
                        } catch (e: Exception) {
                            null
                        }

                        LocationWidget.updateWidget(
                            context = this,
                            appWidgetId = appWidgetId,
                            updateData = WidgetUpdateData.Loaded(
                                updateTime = LocalDateTime.now(),
                                location = location,
                                locationName = locationGeocoded?.let {
                                    it.locality ?: it.adminArea ?: it.countryCode
                                } ?: getString(R.string.widget_location_name_unknown)
                            )
                        )
                    } else {
                        LocationWidget.updateWidget(context = this, appWidgetId, WidgetUpdateData.Error(NoDataException()))
                    }
                } catch (exception: Exception) {
                    LocationWidget.updateWidget(context = this, appWidgetId, WidgetUpdateData.Error(exception))
                }
            } else {
                LocationWidget.updateWidget(context = this, appWidgetId, WidgetUpdateData.Error(MissingPermissionsException()))
            }
        }
    }

    companion object {

        private const val JOB_ID = 1

        fun enqueueWork(context: Context, appWidgetId: Int) {
            val intent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            enqueueWork(context, WidgetUpdateService::class.java, JOB_ID, intent)
        }
    }
}